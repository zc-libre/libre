package com.libre.framework.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.*;
import com.libre.boot.exception.BusinessException;
import com.libre.framework.blog.common.CacheConstants;
import com.libre.framework.blog.enums.ArticleType;
import com.libre.framework.blog.mapper.ArticleMapper;
import com.libre.framework.blog.pojo.Article;
import com.libre.framework.blog.pojo.ArticleTag;
import com.libre.framework.blog.pojo.Category;
import com.libre.framework.blog.pojo.Tag;
import com.libre.framework.blog.pojo.dto.*;
import com.libre.framework.blog.pojo.vo.*;
import com.libre.framework.blog.search.ElasticSearchHandler;
import com.libre.framework.blog.service.*;
import com.libre.framework.blog.service.mapstruct.ArticleMapping;
import com.libre.framework.common.constant.LibreConstants;
import com.libre.mybatis.util.PageUtil;
import com.libre.toolkit.core.StringUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

	private final ArticleTagService articleTagService;

	private final CategoryService categoryService;

	private final TagService tagService;

	private final BlogUserService blogUserService;

	private final ElasticSearchHandler elasticSearchHandler;

	private final ElasticsearchOperations elasticsearchOperations;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addOrUpdate(ArticleDTO articleDTO) {
		ArticleMapping mapping = ArticleMapping.INSTANCE;
		Article article = mapping.sourceToTarget(articleDTO);
		List<Long> tagIds = articleDTO.getTagIds();
		Long categoryId = articleDTO.getCategoryId();
		Category category = categoryService.getById(categoryId);
		if (Objects.isNull(category)) {
			throw new BusinessException("类型不存在：" + categoryId);
		}
		List<Tag> tags = tagService.listByIds(tagIds);
		if (CollectionUtils.isEmpty(tags)) {
			throw new BusinessException("标签不存在");
		}
		this.saveOrUpdate(article);
		List<ArticleTag> articleTagList = Lists.newArrayList();
		for (Tag tag : tags) {
			ArticleTag articleTag = new ArticleTag();
			articleTag.setTagId(tag.getId());
			articleTag.setArticleId(article.getId());
			articleTagList.add(articleTag);
		}
		articleTagService.deleteByArticleIds(ImmutableList.of(article.getId()));
		articleTagService.saveBatch(articleTagList);

	}

	@Override
	public void edit(ArticleDTO articleDTO) {
		ArticleMapping mapping = ArticleMapping.INSTANCE;
		Article article = mapping.sourceToTarget(articleDTO);
		this.updateById(article);
	}

	@Override
	public ArticleVO getArticleById(Long id) {
		Article article = baseMapper.selectById(id);
		if (Objects.isNull(article)) {
			return new ArticleVO();
		}
		Article nextArticle = baseMapper.selectOne(
				buildGetOneWrapper(ArticleType.BLOG.getType()).gt(Article::getGmtCreate, article.getGmtCreate()));
		Article preArticle = baseMapper.selectOne(
				buildGetOneWrapper(ArticleType.BLOG.getType()).lt(Article::getGmtCreate, article.getGmtCreate()));
		List<Article> articleList = Lists.newArrayList();
		articleList.add(article);
		Optional.ofNullable(preArticle).ifPresent(articleList::add);
		Optional.ofNullable(nextArticle).ifPresent(articleList::add);
		List<ArticleVO> voList = buildVoList(articleList);
		if (CollectionUtils.isEmpty(voList)) {
			return new ArticleVO();
		}

		Map<Long, ArticleVO> articleMap = voList.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(ArticleVO::getId, Function.identity()));
		ArticleVO articleVO = articleMap.get(article.getId());

		if (Objects.nonNull(preArticle) && articleMap.containsKey(preArticle.getId())) {
			articleVO.setPreArticle(articleMap.get(preArticle.getId()));
		}
		if (Objects.nonNull(nextArticle) && articleMap.containsKey(nextArticle.getId())) {
			articleVO.setNextArticle(articleMap.get(nextArticle.getId()));
		}
		return articleVO;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return;
		}
		articleTagService.deleteByArticleIds(ids);
		this.removeByIds(ids);
	}

	@Override
	public PageDTO<ArticleVO> findByPage(PageDTO<Article> page, ArticleCriteria criteria) {
		criteria.setArticleType(ArticleType.BLOG.getType());
		PageDTO<Article> res = this.page(page, buildQueryWrapper(criteria));
		List<Article> records = res.getRecords();
		if (CollectionUtils.isEmpty(records)) {
			return new PageDTO<>();
		}
		List<ArticleVO> voList = buildVoList(records);
		return PageUtil.toPage(res, voList);
	}

	@Override
	public List<ArticleIndex> search(PageDTO<Article> page, ArticleCriteria criteria) {
		return elasticSearchHandler.search(page, criteria);
	}

	@Override
	public List<Article> findList(ArticleCriteria criteria) {
		return this.list(buildQueryWrapper(criteria));
	}

	@Override
	public TopAndFeaturedArticleVO findTopAndFeaturedArticles() {
		List<Article> articleList = this.list(Wrappers.<Article>lambdaQuery()
			.eq(Article::getFeatured, LibreConstants.ENABLE)
			.or(wrapper -> wrapper.eq(Article::getTop, LibreConstants.ENABLE))
			.eq(Article::getArticleType, ArticleType.BLOG.getType())
			.eq(Article::getStatus, LibreConstants.ENABLE));
		if (CollectionUtils.isEmpty(articleList)) {
			return new TopAndFeaturedArticleVO();
		}
		List<ArticleVO> voList = buildVoList(articleList);
		TopAndFeaturedArticleVO topAndFeaturedArticle = new TopAndFeaturedArticleVO();
		List<ArticleVO> featureList = Lists.newArrayList();
		for (ArticleVO articleVO : voList) {
			if (ObjectUtils.nullSafeEquals(LibreConstants.ENABLE, articleVO.getTop())) {
				topAndFeaturedArticle.setTopArticle(articleVO);
			}
			if (ObjectUtils.nullSafeEquals(LibreConstants.ENABLE, articleVO.getFeatured())) {
				featureList.add(articleVO);
			}
		}
		topAndFeaturedArticle.setFeatures(featureList);
		return topAndFeaturedArticle;
	}

	@Override
	public PageDTO<Archive> findArchives(PageDTO<Article> page, ArticleCriteria criteria) {
		criteria.setArticleType(ArticleType.BLOG.getType());
		criteria.setStatus(LibreConstants.ENABLE);
		PageDTO<Article> result = this.page(page, buildQueryWrapper(criteria));
		List<Article> records = result.getRecords();
		if (CollectionUtils.isEmpty(records)) {
			return PageUtil.toPage(result, Lists.newArrayList());
		}
		ArticleMapping mapping = ArticleMapping.INSTANCE;
		List<Archive> archives = mapping.convertToArchiveList(records);
		for (Archive archive : archives) {
			if (Objects.isNull(archive.getGmtCreate())) {
				continue;
			}
			CreateDate date = buildCreateDate(archive.getGmtCreate());
			archive.setDate(date);
		}
		return PageUtil.toPage(result, archives);
	}

	@Override
	public About findAboutMe() {
		ArticleCriteria criteria = ArticleCriteria.builder().articleType(ArticleType.ABOUT.getType()).build();
		Article article = this.getOne(buildQueryWrapper(criteria));
		About about = new About();
		if (Objects.isNull(article)) {
			return about;
		}
		String content = article.getContent();
		ArticleCount count = buildArticleCount(content);
		about.setCount(count);
		about.setId(article.getId());
		about.setContent(article.getContent());
		about.setCreateTime(article.getGmtCreate());
		about.setUpdateTime(article.getGmtModified());
		return about;
	}

	@Override
	public PageDTO<ArticleVO> findPageByTagId(PageDTO<ArticleVO> page, ArticleCriteria criteria) {
		criteria.setArticleType(ArticleType.BLOG.getType());
		PageDTO<ArticleVO> result = baseMapper.findPageByTagId(page, criteria);
		List<ArticleVO> voList = result.getRecords();
		if (CollectionUtils.isEmpty(voList)) {
			return PageUtil.toPage(page, Lists.newArrayList());
		}
		setArticleInfo(voList);
		return PageUtil.toPage(page, voList);
	}

	@Override
	public MessageBoard messageBoard() {
		MessageBoard messageBoard = new MessageBoard();
		ArticleCriteria criteria = ArticleCriteria.builder().articleType(ArticleType.COMMENT.getType()).build();
		Article article = this.getOne(buildQueryWrapper(criteria));
		if (Objects.isNull(article)) {
			return messageBoard;
		}
		messageBoard.setId(article.getId());
		messageBoard.setContent(article.getContent());
		messageBoard.setCreateTime(article.getGmtCreate());
		ArticleCount count = buildArticleCount(article.getContent());
		messageBoard.setCount(count);
		return messageBoard;
	}

	@Override
	public void syncElasticsearch() {
		ArticleCriteria criteria = new ArticleCriteria();
		criteria.setArticleType(ArticleType.BLOG.getType());
		criteria.setStatus(LibreConstants.ENABLE);
		List<Article> articleList = this.findList(criteria);
		if (CollectionUtils.isNotEmpty(articleList)) {
			for (Article article : articleList) {
				article.setContent(Jsoup.clean(article.getContent(), Safelist.none()));
			}
			ArticleMapping mapping = ArticleMapping.INSTANCE;
			List<ArticleIndex> articleIndices = mapping.convertToArticleIndexList(articleList);
			elasticsearchOperations.save(articleIndices);
		}
	}

	@Transactional(rollbackFor = Throwable.class)
	@Override
	public void changeTop(Article article) {
		Article dbArticle = baseMapper.selectById(article.getId());
		if (Objects.isNull(dbArticle)) {
			throw new BusinessException("文章不存在");
		}
		if (ObjectUtils.nullSafeEquals(article.getTop(), dbArticle.getTop())) {
			return;
		}
		Optional.ofNullable(article.getTop()).ifPresent(top -> {
			Long topCount = baseMapper
				.selectCount(Wrappers.<Article>lambdaQuery().eq(Article::getTop, LibreConstants.YES));
			if (Objects.nonNull(topCount) && topCount != 0
					&& !ObjectUtils.nullSafeEquals(dbArticle.getTop(), LibreConstants.YES)) {
				throw new BusinessException("已存在置顶文章");
			}
			baseMapper.update(Wrappers.<Article>lambdaUpdate()
				.set(Article::getTop, article.getTop())
				.eq(Article::getId, article.getId()));
		});
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void changeStatus(Article article) {
		this.changeTop(article);
		if (Objects.nonNull(article.getFeatured())) {
			baseMapper.update(Wrappers.<Article>lambdaUpdate()
				.set(Article::getFeatured, article.getFeatured())
				.eq(Article::getId, article.getId()));
		}

		if (Objects.nonNull(article.getStatus())) {
			baseMapper.update(Wrappers.<Article>lambdaUpdate()
				.set(Article::getStatus, article.getStatus())
				.eq(Article::getId, article.getId()));
		}
	}

	private static ArticleCount buildArticleCount(String content) {
		String cleanText = Jsoup.clean(content, Safelist.none());
		ArticleCount count = new ArticleCount();
		if (StringUtil.isBlank(cleanText)) {
			return count;
		}
		double length = cleanText.length();
		count.setSymbolsCount(Math.round(length / 100D) / 10 + "k");
		count.setSymbolsTime(Math.round(length / 40D) / 10 + "mins");
		return count;
	}

	private List<ArticleVO> buildVoList(List<Article> records) {
		ArticleMapping mapping = ArticleMapping.INSTANCE;
		List<ArticleVO> voList = mapping.convertToVoList(records);
		setArticleInfo(voList);
		return voList;
	}

	private void setArticleInfo(List<ArticleVO> voList) {
		Set<Long> articleIds = voList.stream().map(ArticleVO::getId).collect(Collectors.toSet());
		List<ArticleTag> articleTagList = articleTagService.findByArticleIds(articleIds);
		Multimap<Long, Long> articleTagMap = LinkedHashMultimap.create();
		for (ArticleTag articleTag : articleTagList) {
			articleTagMap.put(articleTag.getArticleId(), articleTag.getTagId());
		}
		Set<Long> tagIds = articleTagList.stream().map(ArticleTag::getTagId).collect(Collectors.toSet());
		Set<Long> categoryIds = voList.stream().map(ArticleVO::getCategoryId).collect(Collectors.toSet());
		Map<Long, Tag> tagMap = findTagsAsMap(tagIds);
		Map<Long, Category> categoryMap = findCategoryAsMap(categoryIds);
		Author author = blogUserService.getBlogAuthor(CacheConstants.BLOG_USER_AUTHOR_KEY);
		for (ArticleVO articleVO : voList) {
			if (!categoryMap.containsKey(articleVO.getCategoryId())) {
				continue;
			}
			Category category = categoryMap.get(articleVO.getCategoryId());
			articleVO.setCategory(category);

			List<Tag> tags = Lists.newArrayList();
			if (!articleTagMap.containsKey(articleVO.getId())) {
				continue;
			}
			Collection<Long> tagIdList = articleTagMap.get(articleVO.getId());
			for (Long tagId : tagIdList) {
				if (!tagMap.containsKey(tagId)) {
					continue;
				}
				Tag tag = tagMap.get(tagId);
				tags.add(tag);
			}

			ArticleCount count = buildArticleCount(articleVO.getContent());
			articleVO.setCount(count);
			articleVO.setTags(tags);
			articleVO.setAuthor(author);
			CreateDate date = buildCreateDate(articleVO.getGmtCreate());
			articleVO.setDate(date);
		}
	}

	private static CreateDate buildCreateDate(LocalDateTime dateTime) {
		CreateDate date = new CreateDate();
		if (Objects.isNull(dateTime)) {
			return date;
		}
		date.setYear(dateTime.getYear());
		Month month = dateTime.getMonth();
		date.setMonth(month.getDisplayName(TextStyle.SHORT, Locale.CHINA));
		date.setDay(dateTime.getDayOfMonth());
		return date;
	}

	private Map<Long, Category> findCategoryAsMap(Set<Long> categoryIds) {
		Map<Long, Category> categoryMap = Maps.newHashMap();
		List<Category> categories = categoryService
			.findList(CategoryCriteria.builder().categoryIds(categoryIds).build());
		if (CollectionUtils.isNotEmpty(categories)) {
			for (Category category : categories) {
				categoryMap.put(category.getId(), category);
			}
		}
		return categoryMap;
	}

	private Map<Long, Tag> findTagsAsMap(Set<Long> tagIds) {
		List<Tag> tagList = tagService.findList(TagCriteria.builder().tagIds(tagIds).build());
		Map<Long, Tag> tagMap = Maps.newHashMap();
		if (CollectionUtils.isNotEmpty(tagList)) {
			for (Tag tag : tagList) {
				tagMap.put(tag.getId(), tag);
			}
		}
		return tagMap;
	}

	public LambdaQueryWrapper<Article> buildQueryWrapper(ArticleCriteria criteria) {
		LambdaQueryWrapper<Article> wrapper = Wrappers.<Article>lambdaQuery()
			.nested(criteria.isBlurryQuery(), k -> k.like(Article::getArticleName, criteria.getBlurry()))
			.eq(Objects.nonNull(criteria.getCategoryId()), Article::getCategoryId, criteria.getCategoryId())
			.eq(Objects.nonNull(criteria.getTop()), Article::getTop, criteria.getTop())
			.eq(Objects.nonNull(criteria.getFeatured()), Article::getFeatured, criteria.getFeatured())
			.eq(Objects.nonNull(criteria.getArticleType()), Article::getArticleType, criteria.getArticleType())
			.eq(Objects.nonNull(criteria.getStatus()), Article::getStatus, criteria.getStatus())
			.in(CollectionUtils.isNotEmpty(criteria.getArticleIds()), Article::getId, criteria.getArticleIds());
		if (criteria.haveTime()) {
			wrapper.between(Article::getGmtCreate, criteria.getStartTime(), criteria.getEndTime());
		}
		wrapper.orderByDesc(Article::getGmtCreate);
		return wrapper;
	}

	private static LambdaQueryWrapper<Article> buildGetOneWrapper(Integer articleType) {
		LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(Article::getArticleType, articleType);
		queryWrapper.orderByDesc(Article::getGmtCreate);
		queryWrapper.last("LIMIT 1");
		return queryWrapper;
	}

}

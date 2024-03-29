package com.libre.framework.toolkit.moudle.social.message;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AbstractMessage implements Message {

	private Long messageId;

	private Long socialUserId;

	private Object message;

	private LocalDate sendTime;

}

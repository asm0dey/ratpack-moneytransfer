package com.github.asm0dey.moneytransfer.exceptions;

/**
 * Created by finkel on 16.06.16.
 */
public class UserDoesNotExistException extends Exception {
	private Long from;

	public UserDoesNotExistException(Long from) {
		this.from = from;
	}

	public Long getFrom() {
		return from;
	}
}

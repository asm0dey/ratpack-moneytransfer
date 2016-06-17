package com.github.asm0dey.moneytransfer.services;

import ratpack.path.PathTokens;

import java.math.BigDecimal;

public class TransferOperation {
	private long fromUserId;
	private long toUserId;
	private BigDecimal moneyAmount;

	private TransferOperation(Long fromUserId, Long toUserId, BigDecimal moneyAmount) {

		this.fromUserId = fromUserId;
		this.toUserId = toUserId;
		this.moneyAmount = moneyAmount;
	}

	public static TransferOperation of(PathTokens tokens) {
		String from = tokens.get("from");
		String to = tokens.get("to");
		String amount = tokens.get("amount");
		return new TransferOperation(Long.valueOf(from), Long.valueOf(to), new BigDecimal(amount));
	}

	public long getFromUserId() {
		return fromUserId;
	}

	public long getToUserId() {
		return toUserId;
	}

	public BigDecimal getMoneyAmount() {
		return moneyAmount;
	}

	public TransferOperation invoke() throws NumberFormatException {
		return this;
	}
}

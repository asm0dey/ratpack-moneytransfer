package com.github.asm0dey.moneytransfer.dto;

import com.google.common.base.MoreObjects;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by finkel on 07.06.16.
 */
public class User implements Comparable<User>{
	private final long id;
	private BigDecimal moneyAmount = new BigDecimal(0);
	private final transient ReentrantLock lock = new ReentrantLock();

	public User(long id) {
		this.id = id;
	}

	public User(long id, BigDecimal moneyAmount) {
		this.id = id;
		this.moneyAmount = moneyAmount;
	}

	public void lock() {
		lock.lock();
	}

	public void unlock() {
		lock.unlock();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof User)) return false;
		User user = (User) o;
		return Objects.equals(id, user.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public long getId() {
		return id;
	}

	public BigDecimal getMoneyAmount() {
		return moneyAmount;
	}

	public User setMoneyAmount(BigDecimal moneyAmount) {
		this.moneyAmount = moneyAmount;
		return this;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("moneyAmount", moneyAmount)
				.add("lock", lock)
				.toString();
	}

	@Override
	public int compareTo(User o) {
		return o == null ? 1 : Long.compare(this.id, o.id);
	}

}

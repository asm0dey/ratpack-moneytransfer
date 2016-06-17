package com.github.asm0dey.moneytransfer.services;

import com.github.asm0dey.moneytransfer.dto.User;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by finkel on 07.06.16.
 */
public class UserDAO {
	public static final UserDAO INSTANCE = new UserDAO();
	private static final AtomicLong idGenerator = new AtomicLong(0);
	private final Map<Long, User> users = new ConcurrentHashMap<>();

	private UserDAO() {
	}



	public User addUser(BigDecimal moneyAmount) {
		User user = new User(idGenerator.incrementAndGet(), moneyAmount);
		users.put(user.getId(), user);
		return user;
	}

	public User getUser(Long id) {
		return users.get(id);
	}



	public Collection<User> users() {
		return users.values();
	}

	public Map<Long, User> getUsers() {
		return users;
	}
}

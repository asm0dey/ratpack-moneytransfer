package com.github.asm0dey.moneytransfer.services;

import com.github.asm0dey.moneytransfer.exceptions.NegativeTransferException;
import com.github.asm0dey.moneytransfer.exceptions.UserDoesNotExistException;
import com.github.asm0dey.moneytransfer.dto.User;
import com.github.asm0dey.moneytransfer.exceptions.NotEnoughFundsException;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.handling.internal.ContentTypeHandler;
import ratpack.http.Status;
import ratpack.path.PathTokens;

import java.math.BigDecimal;

import static ratpack.http.Status.OK;

/**
 * Created by finkel on 16.06.16.
 */
public class TransferService implements Handler {
	private static final UserDAO USER_DAO = UserDAO.INSTANCE;
	private static final Status BAD_REQUEST = Status.of(400);
	private static final Status NOT_FOUND = Status.of(404);
	private static final Status NOT_PAID = Status.of(402);

	/**
	 * complex locking logick to avoid deadlocks on simultaneous between same accounts in reverse orders (to prevent thread deadlocking)
	 *
	 * @param userFrom
	 * @param userTo
	 */
	private static void lockUsersForTransaction(User userFrom, User userTo) {
		int comparisonOrder = Long.compare(userFrom.getId(), userTo.getId());
		if (comparisonOrder > 0) {
			userTo.lock();
			userFrom.lock();
		} else {
			userFrom.lock();
			userTo.lock();
		}
	}

	private static void internalTransferMoney(User userFrom, User userTo, BigDecimal amount) {
		userFrom.setMoneyAmount(userFrom.getMoneyAmount().subtract(amount));
		userTo.setMoneyAmount(userTo.getMoneyAmount().add(amount));
	}

	private static void transferMoney(TransferOperation transferOperation) throws NegativeTransferException, UserDoesNotExistException, NotEnoughFundsException {
		long from = transferOperation.getFromUserId();
		long to = transferOperation.getToUserId();
		BigDecimal amount = transferOperation.getMoneyAmount();
		User userFrom = USER_DAO.getUsers().get(from);
		User userTo = USER_DAO.getUsers().get(to);
		if (userFrom == null) throw new UserDoesNotExistException(from);
		if (userTo == null) throw new UserDoesNotExistException(to);
		try {
			lockUsersForTransaction(userFrom, userTo);
			if (userFrom.getMoneyAmount().compareTo(amount) < 0) throw new NotEnoughFundsException();
			if (amount.compareTo(BigDecimal.ZERO) < 0) throw new NegativeTransferException();
			internalTransferMoney(userFrom, userTo, amount);
		} finally {
			userFrom.unlock();
			userTo.unlock();
		}
	}

	@Override
	public void handle(Context ctx) {
		PathTokens tokens = ctx.getPathTokens();
		long fromUserId = 0;
		try {
			TransferOperation transferOperation = TransferOperation.of(tokens);
			fromUserId = transferOperation.getFromUserId();
			transferMoney(transferOperation);
			ctx.getResponse().status(OK).contentType("text/plain").send();
		} catch (NumberFormatException e) {
			ctx.getResponse().status(BAD_REQUEST).send(e.getMessage() == null ? "Illegal amount to transfer" : e.getMessage());
		} catch (UserDoesNotExistException e) {
			ctx.getResponse().status(NOT_FOUND).send("User " + e.getFrom() + " doesn't exist");
		} catch (NotEnoughFundsException e) {
			ctx.getResponse().status(NOT_PAID).send("User " + fromUserId + " doesn't have enough money for transfer");
		} catch (NegativeTransferException e) {
			ctx.getResponse().status(BAD_REQUEST).send("Negative transfer amounts are prohibited");
		}
	}


}

package com.github.asm0dey.moneytransfer;

import com.github.asm0dey.moneytransfer.services.TransferService;
import com.github.asm0dey.moneytransfer.services.UserDAO;
import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.handling.Context;
import ratpack.http.Status;
import ratpack.registry.RegistrySpec;
import ratpack.server.RatpackServer;

import java.math.BigDecimal;

import static java.text.MessageFormat.format;
import static ratpack.jackson.Jackson.json;

public class Main {

	private static final UserDAO USER_DAO = UserDAO.INSTANCE;

	public static void main(String... args) throws Exception {
		RatpackServer

				.of(spec -> spec
						.registryOf(registry())
						.handlers(handlers()))
				.start();

	}

	public static Action<RegistrySpec> registry() {
		return registrySpec -> registrySpec
				.add(TransferService.class, new TransferService());
	}

	public static Action<Chain> handlers() {
		return chain -> chain
				.prefix("person", personPrefix())
				.prefix("transfer", transferPrefix());
	}

	public static Action<Chain> transferPrefix() {
		return transferChain -> transferChain
				.put(":from/:to/:amount", TransferService.class);
	}

	public static Action<Chain> personPrefix() {
		return personChain -> personChain
				.get(ctx -> ctx.render(json(USER_DAO.users())))
				.post(":amount", Main::createUser);
	}

	private static void createUser(Context ctx) {
		String amount = ctx.getPathTokens().get("amount");
		try {
			BigDecimal moneyAmount = new BigDecimal(amount);
			if (moneyAmount.compareTo(BigDecimal.ZERO) < 0) {
				ctx.getResponse().status(400).send(format("Money amount should not be negative, but is \"{0}\"", amount));
				return;
			}
			ctx.render(json(USER_DAO.addUser(moneyAmount)));
		} catch (NumberFormatException e) {
			ctx.getResponse().status(Status.of(400)).send(format("\"{0}\" is not valid amount of money", amount));
		}
	}
}

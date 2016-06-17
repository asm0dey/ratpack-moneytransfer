package com.github.asm0dey.moneytransfer;

import com.github.asm0dey.moneytransfer.services.UserDAO;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ResponseBody;
import org.junit.*;
import ratpack.test.embed.EmbeddedApp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;


/**
 * Created by finkel on 16.06.16.
 */

public class BehaviourTest {

	private static EmbeddedApp app;

	@BeforeClass
	public static void setUp() throws Exception {
		app = EmbeddedApp.of(s -> s
				.registryOf(Main.registry())
				.handlers(Main.handlers()));
	}

	@Before
	public void clearDB(){
		UserDAO.INSTANCE.getUsers().clear();
	}

	@AfterClass
	public static void tearDown() {
		app.close();
	}

	@Test
	public void createUser() throws Exception {
		post(base() + "person/1").then().assertThat().statusCode(200);
	}

	@Test
	public void createdUserHasMoneyAmountFromPath() {
		post(base() + "person/1").then().assertThat().body("moneyAmount", equalTo(1));
	}

	@Test
	public void eachTimeIdIsDifferent() {
		post(base() + "person/1").then().assertThat().body("id", not(equalTo(post(base() + "person/1").getBody().jsonPath().getLong("id"))));
	}

	@Test
	public void weCannotCreatePersonWithNegativeAmountOfMoneyAndGet400() {
		post(base() + "person/-1").then().assertThat().statusCode(400);
	}

	@Test
	public void weCanGetListOfCurrentlyAvailableUsers() {
		List<ResponseBody<?>> responses = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			responses.add(post(base() + "person/{amount}", i).getBody());
		}
		assertEquals(responses.size(), get(base() + "person").getBody().jsonPath().getList(".").size());
	}

	@Test
	public void weCanTransferMoneyFromOnePersonToAnother() {
		long id1 = post(base() + "person/1").getBody().jsonPath().getLong("id");
		long id2 = post(base() + "person/1").getBody().jsonPath().getLong("id");
		put(base() + "transfer/{from}/{to}/0.5", id1, id2).then().statusCode(200);
	}

	@Test
	public void weCantTransferNegativeAmountOfMoneyFromOnePersonToAnotherAndGet400() {
		long id1 = post(base() + "person/1").getBody().jsonPath().getLong("id");
		long id2 = post(base() + "person/1").getBody().jsonPath().getLong("id");
		put(base() + "transfer/{from}/{to}/-0.5", id1, id2).then().statusCode(400);
	}

	@Test
	public void weCantTransferMoneyFromUnexistentPersonToAnotherAndGet404() {
		long id1 = post(base() + "person/1").getBody().jsonPath().getLong("id");
		put(base() + "transfer/{from}/{to}/-0.5", 500, id1).then().statusCode(404);
	}

	@Test
	public void weCantTransferMoneyToUnexistentPersonAndGet404() {
		long id1 = post(base() + "person/1").getBody().jsonPath().getLong("id");
		put(base() + "transfer/{from}/{to}/-0.5", id1, 500).then().statusCode(404);
	}

	@Test
	public void weCantTransferMoneyWhenThereAreNotEnoughMoneyAndGet402Code() {
		long id1 = post(base() + "person/1").getBody().jsonPath().getLong("id");
		long id2 = post(base() + "person/1").getBody().jsonPath().getLong("id");
		put(base() + "transfer/{from}/{to}/2", id1, id2).then().statusCode(402);
	}

	@Test
	public void whenWeCreatePersonWithVeryLongMantissaItIsPersistedInStore() {
		BigDecimal longAmount = new BigDecimal("1.1111111111111111111111111111111111111111111111111111111111111111111111111");
		JsonPath path = post(base() + "person/{amount}", longAmount).getBody().jsonPath();
		long id = path.getLong("id");
		double moneyAmount = path.getDouble("moneyAmount");
		assertEquals(longAmount.doubleValue(), moneyAmount, 0.0000001);
		assertEquals(longAmount, UserDAO.INSTANCE.getUsers().get(id).getMoneyAmount());
	}

	@Test
	public void weCantCreateUserWithNonDecimalAmountOfMoney() {
		post(base() + "person/1s").then().statusCode(400);
	}

	@Test
	public void weCantTransferNonDecimalAmountOfMoneyFromOneUserToAnother() {
		long id1 = post(base() + "person/1").getBody().jsonPath().getLong("id");
		long id2 = post(base() + "person/1").getBody().jsonPath().getLong("id");
		put(base() + "transfer/{from}/{to}/1s", id1, id2).then().statusCode(400);
	}

	@Test
	public void negativeTransfersAreProhibited() {
		long id1 = post(base() + "person/1").getBody().jsonPath().getLong("id");
		long id2 = post(base() + "person/1").getBody().jsonPath().getLong("id");
		put(base() + "transfer/{from}/{to}/-1", id1, id2).then().statusCode(400);
	}
	@Test
	public void weCanTransferAvailableAmountOfMoney() {
		long id1 = post(base() + "person/1").getBody().jsonPath().getLong("id");
		long id2 = post(base() + "person/1").getBody().jsonPath().getLong("id");
		put(base() + "transfer/{from}/{to}/1", id1, id2).then().statusCode(200);
		assertEquals(BigDecimal.ZERO, UserDAO.INSTANCE.getUser(id1).getMoneyAmount());
		assertEquals(new BigDecimal(2), UserDAO.INSTANCE.getUser(id2).getMoneyAmount());
	}

	private static String base() {
		return app.getAddress().toString();
	}

}

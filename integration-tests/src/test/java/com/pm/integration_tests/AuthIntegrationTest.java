package com.pm.integration_tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class AuthIntegrationTest {
	
	@BeforeAll
	static void setUp() {
		RestAssured.baseURI = "http://localhost:4004";
	}
	
	@Test
	public void shouldReturnOKWithValidToken() {
		
		// happy path test
		// the name of the method is written basedon the test convention 
		// starts with should, then returns which status, then with what it returns
		//1. arrange
		//2. act
		//3. assert
		
		// arrange - setting up the initial data for the test 
		String loginPayload = """
				{
				"email" : "testuser@test.com",
				"password": "password123"
				}
				""";
		
		//act - write the code that actually runs the tests
		
		// the given(), when(), then() syntax is used
		Response response = given()
				.contentType("application/json")
				.body(loginPayload)
				.when()
				.post("/auth/login")
				.then()
				.statusCode(200)
				.body("token", notNullValue())
				.extract()
				.response();
		
		System.out.println("Generated tokem : " + response.jsonPath().getString("token"));
				
		
		
		
	}
	
	
	@Test
	public void shouldReturnUnauthorizedOnInvalidLogin() {
		
		// negative test case
		// the name of the method is written basedon the test convention 
		
		String loginPayload = """
				{
				"email" : "invalid_test@test.com",
				"password": "wrongpassword"
				}
				""";
		
				given()
				.contentType("application/json")
				.body(loginPayload)
				.when()
				.post("/auth/login")
				.then()
				.statusCode(401);
	}

}

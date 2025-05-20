package com.pm.integration_tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class PatientIntegrationTest {
	
	@BeforeAll
	static void setUp() {
		RestAssured.baseURI = "http://localhost:4004";
		
		
	}
	
	@Test
	public void shouldReturnPatientsWithValidToken() {
		
		// arrange - setting up the initial data for the test 
		String loginPayload = """
				{
				"email" : "testuser@test.com",
				"password": "password123"
				}
				""";
		
		//act - write the code that actually runs the tests
		
		// the given(), when(), then() syntax is used
		String token = given()
				.contentType("application/json")
				.body(loginPayload)
				.when()
				.post("/auth/login")
				.then()
				.statusCode(200)
				.extract()
				.jsonPath()
				.get("token");
		
		System.out.println("token is ::"+token);
		
	 given()
		.header("Authorization", "Bearer " + token)
		.when()
		.get("/api/patients/")
		.then()
		.statusCode(200)
		.body("patients", notNullValue());

		

	}

}

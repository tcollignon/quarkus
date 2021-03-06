package io.quarkus.it.main;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
public class ServletTestCase {

    @Test
    public void testServlet() {
        RestAssured.when().get("/testservlet").then()
                .body(is("A message"));
    }

    @Test
    public void testFilter() {
        RestAssured.when().get("/filter").then()
                .body(is("A Filter"));
    }

    @Test
    public void testStaticResource() {
        RestAssured.when().get("/filter").then()
                .body(containsString("A Filter"));
    }

    @Test
    public void testWelcomeFile() {
        RestAssured.when().get("/").then()
                .body(containsString("A HTML page"));
    }

    // Basic @ServletSecurity test
    @Test()
    public void testSecureAccessFailure() {
        RestAssured.when().get("/secure-test").then()
                .statusCode(401);
    }

    // Basic @ServletSecurity test
    @Test()
    public void testSecureAccessSuccess() {
        RestAssured.given().auth().preemptive().basic("stuart", "test")
                .when().get("/secure-test").then()
                .statusCode(200);
    }

    @Test
    public void testWebjars() {
        RestAssured
                .when().get("webjars/bootstrap/3.1.0/css/bootstrap.min.css").then()
                .statusCode(200);
    }
}

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginTest {

    @BeforeAll
    static void setup() throws IOException {
        RestAssured.baseURI = "http://localhost:8080/rest";

        // clean database
        URL url = new URL("http://localhost:8081/reset");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.getResponseCode();

        // place user
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "user1@fct",
                                "password": "pwd",
                                "confirmation": "pwd",
                                "phone": "1234",
                                "address": "street",
                                "role": "USER"
                              }
                            }
                        """)
                .when()
                .post("/createaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));
    }

    @Test
    @Order(1)
    void login_invalidInput() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "",
                                "password": "pwd"
                              }
                            }
                        """)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("status", equalTo("9906"));
    }

    @Test
    @Order(2)
    void login_userNotFound() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "user2@fct",
                                "password": "pwd"
                              }
                            }
                        """)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("status", equalTo("9902"));
    }

    @Test
    @Order(3)
    void login_invalidCredentials() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "user1@fct",
                                "password": "asd"
                              }
                            }
                        """)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("status", equalTo("9900"));
    }

    @Test
    @Order(4)
    void login_success() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "user1@fct",
                                "password": "pwd"
                              }
                            }
                        """)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("data.token.username", equalTo("user1@fct"))
                .body("data.token.role", equalTo("USER"));
    }

    @Test
    @Order(5)
    void login_alreadyLoggedIn() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "user1@fct",
                                "password": "pwd"
                              }
                            }
                        """)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));
    }
}

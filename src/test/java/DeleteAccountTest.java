import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.net.HttpURLConnection;
import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class DeleteAccountTest {

    private static String userTokenId;
    private static String adminTokenId;
    private static String bofficerTokenId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080/rest";
    }

    @BeforeEach
    void setupTest() throws Exception {
        // clean database
        URL url = new URL("http://localhost:8081/reset");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.getResponseCode();

        // place users
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

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "admin1@fct",
                                "password": "pwd",
                                "confirmation": "pwd",
                                "phone": "1234",
                                "address": "street",
                                "role": "ADMIN"
                              }
                            }
                        """)
                .when()
                .post("/createaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "bofficer1@fct",
                                "password": "pwd",
                                "confirmation": "pwd",
                                "phone": "1234",
                                "address": "street",
                                "role": "BOFFICER"
                              }
                            }
                        """)
                .when()
                .post("/createaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        // login
        userTokenId = given()
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
                .extract().path("data.token.tokenId");

        adminTokenId = given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "admin1@fct",
                                "password": "pwd"
                              }
                            }
                        """)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .extract().path("data.token.tokenId");

        bofficerTokenId = given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "bofficer1@fct",
                                "password": "pwd"
                              }
                            }
                        """)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .extract().path("data.token.tokenId");
    }

    @Test
    void deleteAccount_byUser() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "user1@fct"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(userTokenId))
                .when()
                .post("/deleteaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("9905"));
    }

    @Test
    void deleteAccount_byBofficer() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "user1@fct"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(bofficerTokenId))
                .when()
                .post("/deleteaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("9905"));
    }

    @Test
    void deleteAccount_byAdmin_userNotFound() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "user5@fct"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/deleteaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("9902"));
    }

    @Test
    void deleteAccount_byAdmin() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/showusers")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("data.users", hasSize(3))
                .body("data.users.username", hasItems("user1@fct", "bofficer1@fct", "admin1@fct"));;

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "user1@fct"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/deleteaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        // test if user was deleted
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/showusers")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("data.users", hasSize(2))
                .body("data.users.username", hasItems("bofficer1@fct", "admin1@fct"));

        // test if user token was deleted
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/showauthsessions")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("data.sessions", hasSize(2))
                .body("data.sessions.username", hasItems("bofficer1@fct", "admin1@fct"));
    }
}

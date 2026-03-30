import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ShowUserRoleTest {

    private static String userTokenId;
    private static String adminTokenId;
    private static String bofficerTokenId;

    @BeforeAll
    static void setup() throws IOException {
        RestAssured.baseURI = "http://localhost:8080/rest";

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
    @Order(1)
    void showUserRole_byUser() {
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
                .post("/showuserrole")
                .then()
                .statusCode(200)
                .body("status", equalTo("9905"));
    }

    @Test
    @Order(2)
    void showUserRole_byBofficer_toUser() {
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
                .post("/showuserrole")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("data.username", equalTo("user1@fct"))
                .body("data.role", equalTo("USER"));
    }

    @Test
    @Order(3)
    void showUserRole_byAdmin_userNotFound() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "user2@fct"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/showuserrole")
                .then()
                .statusCode(200)
                .body("status", equalTo("9902"));
    }

    @Test
    @Order(4)
    void showUserRole_byAdmin() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "bofficer1@fct"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/showuserrole")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("data.username", equalTo("bofficer1@fct"))
                .body("data.role", equalTo("BOFFICER"));
    }

    @Test
    @Order(5)
    void showUserRole_byAdmin_toHimself() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "admin1@fct"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/showuserrole")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("data.username", equalTo("admin1@fct"))
                .body("data.role", equalTo("ADMIN"));
    }
}

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.net.HttpURLConnection;
import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class LogoutTest {

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
    void logout_byUser_forbidden() {
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
                        """.formatted(userTokenId))
                .when()
                .post("/logout")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));

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
                        """.formatted(userTokenId))
                .when()
                .post("/logout")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));
    }

    @Test
    void logout_byBofficer_forbidden() {
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
                .post("/logout")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));

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
                        """.formatted(bofficerTokenId))
                .when()
                .post("/logout")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));
    }

    @Test
    void logout_user_multipleSessions() {
        String tempUserTokenId = given()
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
                .post("/logout")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        // test if only userTokenId was deleted
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
                .body("data.sessions", hasSize(3))
                .body("data.sessions.tokenId", hasItems(tempUserTokenId, bofficerTokenId, adminTokenId));
    }

    @Test
    void logout_bofficer_multipleSessions() {
        String tempBofficerTokenId = given()
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
                        """.formatted(tempBofficerTokenId))
                .when()
                .post("/logout")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        // test if only bofficerTokenId was deleted
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
                .body("data.sessions", hasSize(3))
                .body("data.sessions.tokenId", hasItems(userTokenId, adminTokenId, bofficerTokenId));
    }

    @Test
    void logout_admin_multipleSessions() {
        String tempAdminTokenId = given()
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
                        """.formatted(tempAdminTokenId))
                .when()
                .post("/logout")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        // test if only adminTokenId was deleted
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
                .body("data.sessions", hasSize(3))
                .body("data.sessions.tokenId", hasItems(userTokenId, bofficerTokenId, adminTokenId));
    }

    @Test
    void logout_user_byAdmin() {
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
                .extract().path("data.token.tokenId");

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
                .post("/logout")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        // test if all tokens were deleted
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
                .body("data.sessions.tokenId", hasItems(adminTokenId, bofficerTokenId));
    }

    @Test
    void logout_bofficer_byAdmin() {
        given()
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
                .post("/logout")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        // test if all tokens were deleted
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
                .body("data.sessions.tokenId", hasItems(userTokenId, adminTokenId));
    }

    @Test
    void logout_admin_byAdmin() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "admin2@fct",
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
                                "username": "admin2@fct",
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

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "admin2@fct",
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

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "admin2@fct"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/logout")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        // test if all tokens were deleted
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
                .body("data.sessions", hasSize(3))
                .body("data.sessions.tokenId", hasItems(userTokenId, adminTokenId, bofficerTokenId));
    }
}

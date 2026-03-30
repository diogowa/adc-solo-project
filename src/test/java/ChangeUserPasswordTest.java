import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.net.HttpURLConnection;
import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ChangeUserPasswordTest {

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
    void changeUserPassword_invalidCredentials() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "user1@fct",
                                "oldPassword": "asd",
                                "newPassword": "pwd"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(userTokenId))
                .when()
                .post("/changeuserpwd")
                .then()
                .statusCode(200)
                .body("status", equalTo("9900"));
    }

    @Test
    void changeUserPassword_byUser_forbidden() {
        // place new user2 to test if user1 can change another user password
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "user2@fct",
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
                                "username": "user2@fct",
                                "oldPassword": "pwd",
                                "newPassword": "pwd"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(userTokenId))
                .when()
                .post("/changeuserpwd")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "bofficer1@fct",
                                "oldPassword": "pwd",
                                "newPassword": "pwd"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(userTokenId))
                .when()
                .post("/changeuserpwd")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "admin1@fct",
                                "oldPassword": "pwd",
                                "newPassword": "pwd"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(userTokenId))
                .when()
                .post("/changeuserpwd")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));
    }

    @Test
    void changeUserPassword_byBofficer_forbidden() {
        // place new bofficer2 to test if bofficer1 can change another bofficer password
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "bofficer2@fct",
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
                                "username": "user1@fct",
                                "oldPassword": "pwd",
                                "newPassword": "pwd"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(bofficerTokenId))
                .when()
                .post("/changeuserpwd")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "bofficer2@fct",
                                "oldPassword": "pwd",
                                "newPassword": "pwd"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(bofficerTokenId))
                .when()
                .post("/changeuserpwd")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "admin1@fct",
                                "oldPassword": "pwd",
                                "newPassword": "pwd"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(bofficerTokenId))
                .when()
                .post("/changeuserpwd")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));
    }

    @Test
    void changeUserPassword_byAdmin_forbidden() {
        // place new admin2 to test if admin1 can change another admin password
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
                                "username": "user1@fct",
                                "oldPassword": "pwd",
                                "newPassword": "pwd"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/changeuserpwd")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "bofficer1@fct",
                                "oldPassword": "pwd",
                                "newPassword": "pwd"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/changeuserpwd")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "admin2@fct",
                                "oldPassword": "pwd",
                                "newPassword": "pwd"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/changeuserpwd")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));
    }

    @Test
    void changeUserPassword_success() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "user1@fct",
                                "oldPassword": "pwd",
                                "newPassword": "asd"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(userTokenId))
                .when()
                .post("/changeuserpwd")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "bofficer1@fct",
                                "oldPassword": "pwd",
                                "newPassword": "asd"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(bofficerTokenId))
                .when()
                .post("/changeuserpwd")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                "username": "admin1@fct",
                                "oldPassword": "pwd",
                                "newPassword": "asd"
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/changeuserpwd")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));
    }
}

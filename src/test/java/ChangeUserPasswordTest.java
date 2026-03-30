import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import io.restassured.RestAssured;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.*;

import java.net.HttpURLConnection;
import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangeUserPasswordTest {

    private static String userTokenId;
    private static String adminTokenId;
    private static String bofficerTokenId;

    private static Datastore datastore;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080/rest";

        datastore = DatastoreOptions.newBuilder()
                .setProjectId("test-project")
                .setHost("http://localhost:8081")
                .build()
                .getService();
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
                                "role": "BOFFICER"
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

        Key userKey = datastore.newKeyFactory().setKind("User").newKey("user1@fct");
        Entity userEntity = datastore.get(userKey);
        assertEquals(DigestUtils.sha512Hex("asd"), userEntity.getString("password"));

        Key bofficerKey = datastore.newKeyFactory().setKind("User").newKey("bofficer1@fct");
        Entity bofficerEntity = datastore.get(bofficerKey);
        assertEquals(DigestUtils.sha512Hex("asd"), bofficerEntity.getString("password"));

        Key admin1Key = datastore.newKeyFactory().setKind("User").newKey("admin1@fct");
        Entity admin1Entity = datastore.get(admin1Key);
        assertEquals(DigestUtils.sha512Hex("asd"), admin1Entity.getString("password"));
    }

    @Test
    void changeUserPassword_logoutOfSessions_success() {
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
                .body("status", equalTo("9903"));

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
                        """.formatted(tempAdminTokenId))
                .when()
                .post("/showauthsessions")
                .then()
                .statusCode(200)
                .body("status", equalTo("9903"));
    }
}

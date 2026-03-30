import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.net.HttpURLConnection;
import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModifyAccountAttributesTest {

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
    void modifyAccountAttributes_byUser_forbidden() {
        // place new user2 to test
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
                                  "attributes": {
                                    "phone": "6789",
                                    "address": "new street"
                                  }
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(userTokenId))
                .when()
                .post("/modaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                  "username": "bofficer1@fct",
                                  "attributes": {
                                    "phone": "6789",
                                    "address": "new street"
                                  }
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(userTokenId))
                .when()
                .post("/modaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                  "username": "admin1@fct",
                                  "attributes": {
                                    "phone": "6789",
                                    "address": "new street"
                                  }
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(userTokenId))
                .when()
                .post("/modaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));
    }

    @Test
    void modifyAccountAttributes_byBofficer_forbidden() {
        // place new bofficer2 to test
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
                                  "username": "bofficer2@fct",
                                  "attributes": {
                                    "phone": "6789",
                                    "address": "new street"
                                  }
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(bofficerTokenId))
                .when()
                .post("/modaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                  "username": "admin1@fct",
                                  "attributes": {
                                    "phone": "6789",
                                    "address": "new street"
                                  }
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(bofficerTokenId))
                .when()
                .post("/modaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("9907"));
    }

    @Test
    void modifyAccountAttributes_byAdmin_userNotFound() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                  "username": "user2@fct",
                                  "attributes": {
                                    "phone": "6789",
                                    "address": "new street"
                                  }
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/modaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("9902"));
    }

    @Test
    void modifyAccountAttributes_byUser_success() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                  "username": "user1@fct",
                                  "attributes": {
                                    "phone": "6789",
                                    "address": "new street"
                                  }
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(userTokenId))
                .when()
                .post("/modaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        Key userKey = datastore.newKeyFactory().setKind("User").newKey("user1@fct");
        Entity userEntity = datastore.get(userKey);
        assertEquals("6789", userEntity.getString("phone"));
        assertEquals("new street", userEntity.getString("address"));
    }

    @Test
    void modifyAccountAttributes_byBofficer_success() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                  "username": "user1@fct",
                                  "attributes": {
                                    "phone": "6789",
                                    "address": "new street"
                                  }
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(bofficerTokenId))
                .when()
                .post("/modaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                  "username": "bofficer1@fct",
                                  "attributes": {
                                    "phone": "6789",
                                    "address": "new street"
                                  }
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(bofficerTokenId))
                .when()
                .post("/modaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        Key userKey = datastore.newKeyFactory().setKind("User").newKey("user1@fct");
        Entity userEntity = datastore.get(userKey);
        assertEquals("6789", userEntity.getString("phone"));
        assertEquals("new street", userEntity.getString("address"));

        Key bofficerKey = datastore.newKeyFactory().setKind("User").newKey("bofficer1@fct");
        Entity bofficerEntity = datastore.get(bofficerKey);
        assertEquals("6789", bofficerEntity.getString("phone"));
        assertEquals("new street", bofficerEntity.getString("address"));
    }

    @Test
    void modifyAccountAttributes_byAdmin_success() {
        // place admin2 to test
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
                                  "attributes": {
                                    "phone": "6789",
                                    "address": "new street"
                                  }
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/modaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                  "username": "bofficer1@fct",
                                  "attributes": {
                                    "phone": "6789",
                                    "address": "new street"
                                  }
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/modaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                  "username": "admin1@fct",
                                  "attributes": {
                                    "phone": "6789",
                                    "address": "new street"
                                  }
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/modaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        given()
                .contentType("application/json")
                .body("""
                            {
                              "input": {
                                  "username": "admin2@fct",
                                  "attributes": {
                                    "phone": "6789",
                                    "address": "new street"
                                  }
                              },
                              "token": {
                                "tokenId": "%s"
                              }
                            }
                        """.formatted(adminTokenId))
                .when()
                .post("/modaccount")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));

        Key userKey = datastore.newKeyFactory().setKind("User").newKey("user1@fct");
        Entity userEntity = datastore.get(userKey);
        assertEquals("6789", userEntity.getString("phone"));
        assertEquals("new street", userEntity.getString("address"));

        Key bofficerKey = datastore.newKeyFactory().setKind("User").newKey("bofficer1@fct");
        Entity bofficerEntity = datastore.get(bofficerKey);
        assertEquals("6789", bofficerEntity.getString("phone"));
        assertEquals("new street", bofficerEntity.getString("address"));

        Key admin1Key = datastore.newKeyFactory().setKind("User").newKey("admin1@fct");
        Entity admin1Entity = datastore.get(admin1Key);
        assertEquals("6789", admin1Entity.getString("phone"));
        assertEquals("new street", admin1Entity.getString("address"));

        Key admin2Key = datastore.newKeyFactory().setKind("User").newKey("admin2@fct");
        Entity admin2Entity = datastore.get(admin2Key);
        assertEquals("6789", admin2Entity.getString("phone"));
        assertEquals("new street", admin2Entity.getString("address"));
    }
}

package org.example.resources;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.util.RegisterData;
import org.example.util.ResponseHelper;

import java.time.Instant;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UsersResource {

    private static final Logger LOG = Logger.getLogger(UsersResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

    public UsersResource() {}

    @POST
    @Path("/createaccount")
    public Response createAccount(RegisterData data) {
        LOG.fine("createAccount: " + data.username);

        if (!data.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_INPUT);
        }

        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = userKeyFactory.newKey(data.username);
            Entity user = txn.get(userKey);

            if (user != null) {
                txn.rollback();
                return ResponseHelper.error(ResponseHelper.USER_ALREADY_EXISTS);
            }

            user = Entity.newBuilder(userKey)
                    .set("password", DigestUtils.sha512Hex(data.password))
                    .set("email", data.email)
                    .set("phone", data.phone)
                    .set("address", data.address)
                    .set("role", data.role)
                    .build();

            txn.put(user);
            txn.commit();

            LOG.info("Account was created: " + data.username);
            return ResponseHelper.ok(Map.of("username", data.username, "role", data.role));
        } catch (Exception e) {
            LOG.severe("Error creating account: " + e.getMessage());
            return ResponseHelper.error(ResponseHelper.FORBIDDEN);
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
}

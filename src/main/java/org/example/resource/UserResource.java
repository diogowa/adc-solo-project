package org.example.resource;

import com.google.cloud.datastore.DatastoreException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.api.RegisterRequest;
import org.example.model.UserEntity;
import org.example.persistance.UserDAO;
import org.example.util.ResponseHelper;

import java.util.Map;
import java.util.logging.Logger;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private static final Logger LOG = Logger.getLogger(UserResource.class.getName());

    private final UserDAO userDAO = new UserDAO();

    public UserResource() {}

    @POST
    @Path("/createaccount")
    public Response createAccount(RegisterRequest req) {
        LOG.fine("createAccount: " + req.username);

        if (!req.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_INPUT);
        }

        UserEntity user = new UserEntity(
                req.username,
                DigestUtils.sha512Hex(req.password),
                req.phone,
                req.address,
                req.role
        );

        try {
            boolean created = userDAO.createUser(user);
            if (created) {
                LOG.info("Account was created: " + req.username);
                return ResponseHelper.ok(Map.of("username", req.username, "role", req.role));
            } else {
                LOG.severe("User already exists: " + req.username);
                return ResponseHelper.error(ResponseHelper.USER_ALREADY_EXISTS);
            }
        } catch (DatastoreException e) {
            LOG.severe("Datastore could not create account: " + e.getMessage());
            return ResponseHelper.error(ResponseHelper.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            LOG.severe("Unexpected error creating account: " + e.getMessage());
            return ResponseHelper.error(ResponseHelper.INTERNAL_SERVER_ERROR);
        }
    }
}

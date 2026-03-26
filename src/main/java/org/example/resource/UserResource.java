package org.example.resource;

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
    public Response createAccount(RegisterRequest data) {
        LOG.fine("createAccount: " + data.username);

        if (!data.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_INPUT);
        }

        UserEntity user = new UserEntity(
                data.username,
                DigestUtils.sha512Hex(data.password),
                data.phone,
                data.address,
                data.role
        );

        boolean created = userDAO.createUser(user);
        if (!created) {
            LOG.severe("User already exists: " + data.username);
            return ResponseHelper.error(ResponseHelper.USER_ALREADY_EXISTS);
        } else {
            LOG.info("Account was created: " + data.username);
            return ResponseHelper.ok(Map.of("username", data.username, "role", data.role));
        }
    }
}

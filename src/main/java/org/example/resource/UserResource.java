package org.example.resource;

import com.google.cloud.datastore.DatastoreException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.api.*;
import org.example.model.TokenEntity;
import org.example.model.UserEntity;
import org.example.persistance.TokenDAO;
import org.example.persistance.UserDAO;
import org.example.util.ResponseHelper;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private static final Logger LOG = Logger.getLogger(UserResource.class.getName());

    private final UserDAO userDAO = new UserDAO();
    private final TokenDAO tokenDAO = new TokenDAO();

    public UserResource() {}

    @POST
    @Path("/createaccount")
    public Response createAccount(InputWrapper<CreateAccountRequest> body) {
        CreateAccountRequest req = body.input;

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
                return ResponseHelper.ok(Map.of("username", req.username, "role", req.role));
            } else {
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

    @POST
    @Path("/showusers")
    public Response showUsers(InputTokenWrapper<EmptyRequest> body) {
        LOG.fine("showUsers");

        if (!body.token.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_TOKEN);
        }

        try {
            TokenEntity token = tokenDAO.getToken(body.token.tokenId);
            if (token == null) {
                return ResponseHelper.error(ResponseHelper.INVALID_TOKEN);
            }

            if (token.isExpired()) {
                return ResponseHelper.error(ResponseHelper.TOKEN_EXPIRED);
            }

            if (! (token.role.equals(Role.ADMIN) || token.role.equals(Role.BOFFICER)) ) {
                LOG.warning("Unauthorized role: " + token.role);
                return ResponseHelper.error(ResponseHelper.UNAUTHORIZED);
            }

            List<UserEntity> users = userDAO.getUsers();
            return ResponseHelper.ok(Map.of("users", users));
        } catch (DatastoreException e) {
            LOG.severe("Datastore could not get users: " + e.getMessage());
            return ResponseHelper.error(ResponseHelper.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            LOG.severe("Unexpected error showing users: " + e.getMessage());
            return ResponseHelper.error(ResponseHelper.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/deleteaccount")
    public Response deleteAccount(InputTokenWrapper<DeleteAccountRequest> body) {
        DeleteAccountRequest req = body.input;

        LOG.fine("deleteAccount: " + req.username);

        if (!req.isValid() && !body.token.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_INPUT);
        }

        try {
            TokenEntity token = tokenDAO.getToken(body.token.tokenId);
            if (token == null) {
                return ResponseHelper.error(ResponseHelper.INVALID_TOKEN);
            }

            if (!req.username.equals(token.username)) {
                LOG.severe("Token does not belong to the user: " + req.username + " belongs to: " + token.username);
                return ResponseHelper.error(ResponseHelper.FORBIDDEN);
            }

            if (token.isExpired()) {
                return ResponseHelper.error(ResponseHelper.TOKEN_EXPIRED);
            }

            if (!token.role.equals(Role.ADMIN)) {
                LOG.warning("Unauthorized role: " + token.role);
                return ResponseHelper.error(ResponseHelper.UNAUTHORIZED);
            }

            userDAO.deleteUser(req.username);
            return ResponseHelper.ok(Map.of("message", "Account deleted successfully"));
        } catch (DatastoreException e) {
            LOG.severe("Datastore could not delete account: " + e.getMessage());
            return ResponseHelper.error(ResponseHelper.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            LOG.severe("Unexpected error when deleting an account: " + e.getMessage());
            return ResponseHelper.error(ResponseHelper.INTERNAL_SERVER_ERROR);
        }
    }
}

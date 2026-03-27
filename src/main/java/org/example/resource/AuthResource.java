package org.example.resource;

import com.google.cloud.datastore.DatastoreException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.api.LoginRequest;
import org.example.model.TokenEntity;
import org.example.model.UserEntity;
import org.example.persistance.TokenDAO;
import org.example.persistance.UserDAO;
import org.example.util.ResponseHelper;

import java.util.Map;
import java.util.logging.Logger;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    private static final Logger LOG = Logger.getLogger(AuthResource.class.getName());

    private final UserDAO userDAO = new UserDAO();
    private final TokenDAO tokenDAO = new TokenDAO();

    public AuthResource() {}

    @POST
    @Path("/login")
    public Response login(LoginRequest req) {
        LOG.fine("login: " + req.username);

        if (!req.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_INPUT);
        }

        try {
            UserEntity user = userDAO.getUser(req.username);
            if (user == null) {
                return ResponseHelper.error(ResponseHelper.USER_NOT_FOUND);
            }

            if (!user.password.equals(DigestUtils.sha512Hex(req.password))) {
                return ResponseHelper.error(ResponseHelper.INVALID_CREDENTIALS);
            }

            TokenEntity token = new TokenEntity(user.username, user.role);
            tokenDAO.saveToken(token);
            return ResponseHelper.ok(Map.of(
                    "tokenId", token.tokenId,
                    "username", token.username,
                    "role", token.role,
                    "issuedAt", token.issuedAt,
                    "expiresAt", token.expiresAt
            ));
        } catch (DatastoreException e) {
            e.printStackTrace();
            LOG.severe("Datastore could not create token: " + e.getMessage());
            return ResponseHelper.error(ResponseHelper.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            LOG.severe("Unexpected error in login: " + e.getMessage());
            return ResponseHelper.error(ResponseHelper.INTERNAL_SERVER_ERROR);
        }
    }
}

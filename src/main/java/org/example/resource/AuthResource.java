package org.example.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.api.EmptyRequest;
import org.example.api.InputTokenWrapper;
import org.example.api.InputWrapper;
import org.example.api.LoginRequest;
import org.example.model.Role;
import org.example.model.TokenEntity;
import org.example.model.UserEntity;
import org.example.persistence.TokenDAO;
import org.example.persistence.UserDAO;
import org.example.util.ResponseHelper;

import java.util.List;
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
    public Response login(InputWrapper<LoginRequest> body) {
        LoginRequest req = body.input;

        LOG.fine("login: " + req.username);

        if (!req.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_INPUT);
        }

        try {
            UserEntity user = userDAO.login(req.username, req.password);
            TokenEntity token = tokenDAO.saveToken(user.username, user.role);

            return ResponseHelper.ok(Map.of(
                    "tokenId", token.tokenId,
                    "username", token.username,
                    "role", token.role,
                    "issuedAt", token.issuedAt,
                    "expiresAt", token.expiresAt
            ));
        } catch (RuntimeException ex) {
            return ResponseHelper.error(ex.getMessage());
        } catch (Exception e) {
            LOG.severe("Unexpected error when login in: " + e.getMessage());
            return ResponseHelper.error(ResponseHelper.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/showauthsessions")
    public Response showAuthSessions(InputTokenWrapper<EmptyRequest> body) {
        LOG.fine("showAuthSessions");

        if (!body.token.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_TOKEN);
        }

        try {
            TokenEntity token = tokenDAO.validateToken(body.token.tokenId);

            if (!token.role.equals(Role.ADMIN)) {
                LOG.warning("Unauthorized role: " + token.role);
                return ResponseHelper.error(ResponseHelper.UNAUTHORIZED);
            }

            List<TokenEntity> tokens = tokenDAO.getAllTokens();
            return ResponseHelper.ok(Map.of("sessions", tokens));
        } catch (RuntimeException ex) {
            return ResponseHelper.error(ex.getMessage());
        } catch (Exception e) {
            LOG.severe("Unexpected error showing auth sessions: " + e.getMessage());
            return ResponseHelper.error(ResponseHelper.INTERNAL_SERVER_ERROR);
        }
    }
}

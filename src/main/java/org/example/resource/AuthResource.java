package org.example.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.api.*;
import org.example.model.Role;
import org.example.model.TokenEntity;
import org.example.persistence.DAO;
import org.example.util.ResponseHelper;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    private static final Logger LOG = Logger.getLogger(AuthResource.class.getName());

    private final DAO dao = new DAO();

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
            TokenEntity token = dao.login(req.username, req.password);

            return ResponseHelper.ok(Map.of("token", Map.of(
                    "tokenId", token.tokenId,
                    "username", token.username,
                    "role", token.role,
                    "issuedAt", token.issuedAt,
                    "expiresAt", token.expiresAt
            )));
        } catch (RuntimeException ex) {
            return ResponseHelper.error(ex.getMessage());
        }
    }

    @POST
    @Path("/logout")
    public Response logout(InputTokenWrapper<UsernameRequest> body) {
        UsernameRequest req = body.input;

        LOG.fine("logout: " + req.username);

        if (!req.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_INPUT);
        }
        if (!body.token.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_TOKEN);
        }

        try {
            TokenEntity token = dao.validateToken(body.token.tokenId);

            if (token.role.equals(Role.ADMIN)) {
                dao.logoutUser(req.username);
                return ResponseHelper.ok(Map.of("message", "Logout successful"));

            } else if ((token.role.equals(Role.USER) && token.username.equals(req.username))
                    || (token.role.equals(Role.BOFFICER) && token.username.equals(req.username))) {
                dao.logout(token.tokenId);
                return ResponseHelper.ok(Map.of("message", "Logout successful"));

            } else {
                return ResponseHelper.error(ResponseHelper.FORBIDDEN);
            }
        } catch (RuntimeException ex) {
            return ResponseHelper.error(ex.getMessage());
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
            TokenEntity token = dao.validateToken(body.token.tokenId);

            if (!token.role.equals(Role.ADMIN)) {
                LOG.warning("Unauthorized role: " + token.role);
                return ResponseHelper.error(ResponseHelper.UNAUTHORIZED);
            }

            List<Map<String, Object>> result = dao.getAllTokens()
                    .stream()
                    .map(t -> Map.<String, Object>of(
                            "tokenId", t.tokenId,
                            "username", t.username,
                            "role", t.role.toString(),
                            "expiresAt", t.expiresAt
                    ))
                    .toList();

            return ResponseHelper.ok(Map.of("sessions", result));
        } catch (RuntimeException ex) {
            return ResponseHelper.error(ex.getMessage());
        } catch (Exception e) {
            LOG.severe("Unexpected error showing auth sessions: " + e.getMessage());
            return ResponseHelper.error(ResponseHelper.INTERNAL_SERVER_ERROR);
        }
    }
}

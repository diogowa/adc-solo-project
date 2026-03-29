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
import org.example.model.UserEntity;
import org.example.persistence.DAO;
import org.example.util.ResponseHelper;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private static final Logger LOG = Logger.getLogger(UserResource.class.getName());

    private final DAO dao = new DAO();

    public UserResource() {}

    @POST
    @Path("/createaccount")
    public Response createAccount(InputWrapper<CreateAccountRequest> body) {
        CreateAccountRequest req = body.input;

        LOG.fine("createAccount: " + req.username);

        if (!req.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_INPUT);
        }

        try {
            dao.createUser(req.username, req.password, req.phone, req.address, req.role);
            return ResponseHelper.ok(Map.of("username", req.username, "role", req.role));
        } catch (RuntimeException ex) {
            return ResponseHelper.error(ex.getMessage());
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
            TokenEntity token = dao.validateToken(body.token.tokenId);

            if (token.role.equals(Role.USER)) {
                LOG.warning("Unauthorized role: " + token.role);
                return ResponseHelper.error(ResponseHelper.UNAUTHORIZED);
            }

            List<UserEntity> users = dao.getUsers();
            return ResponseHelper.ok(Map.of("users", users));
        } catch (RuntimeException ex) {
            return ResponseHelper.error(ex.getMessage());
        } catch (Exception e) {
            LOG.severe("Unexpected error showing users: " + e.getMessage());
            return ResponseHelper.error(ResponseHelper.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/deleteaccount")
    public Response deleteAccount(InputTokenWrapper<UsernameRequest> body) {
        UsernameRequest req = body.input;

        LOG.fine("deleteAccount: " + req.username);

        if (!req.isValid() || !body.token.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_INPUT);
        }

        try {
            TokenEntity token = dao.validateToken(body.token.tokenId);

            if (!token.role.equals(Role.ADMIN)) {
                LOG.warning("Unauthorized role: " + token.role);
                return ResponseHelper.error(ResponseHelper.UNAUTHORIZED);
            }

            dao.deleteUser(req.username);
            return ResponseHelper.ok(Map.of("message", "Account deleted successfully"));
        } catch (RuntimeException ex) {
            return ResponseHelper.error(ex.getMessage());
        }
    }

    @POST
    @Path("/modaccount")
    public Response modifyAccount(InputTokenWrapper<ModifyAccountRequest> body) {
        ModifyAccountRequest req = body.input;

        LOG.fine("modifyAccount: " + req.username);

        if (!req.isValid() || !body.token.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_INPUT);
        }

        try {
            TokenEntity token = dao.validateToken(body.token.tokenId);
            UserEntity user = dao.getUser(req.username);

            boolean canModify = token.role.equals(Role.ADMIN)
                    || (token.role.equals(Role.BOFFICER) && (user.username.equals(token.username) || user.role.equals(Role.USER)))
                    || (token.role.equals(Role.USER) && user.username.equals(token.username));

            if (!canModify) {
                LOG.warning("Unauthorized role: " + token.role);
                return ResponseHelper.error(ResponseHelper.UNAUTHORIZED);
            }

            dao.modifyUser(user.username, req.attributes.phone, req.attributes.address);
            return ResponseHelper.ok(Map.of("message", "Updated successfully"));
        } catch (RuntimeException ex) {
            return ResponseHelper.error(ex.getMessage());
        }
    }

    @POST
    @Path("/showuserrole")
    public Response showUserRole(InputTokenWrapper<UsernameRequest> body) {
        UsernameRequest req = body.input;

        LOG.fine("showUserRole: " + req.username);

        if (!req.isValid() || !body.token.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_INPUT);
        }

        try {
            TokenEntity token = dao.validateToken(body.token.tokenId);

            if (token.role.equals(Role.USER)) {
                LOG.warning("Unauthorized role: " + token.role);
                return ResponseHelper.error(ResponseHelper.UNAUTHORIZED);
            }

            UserEntity user = dao.getUser(req.username);
            return ResponseHelper.ok(Map.of("username", user.username, "role", user.role.toString()));
        } catch (RuntimeException ex) {
            return ResponseHelper.error(ex.getMessage());
        } catch (Exception e) {
            LOG.severe("Unexpected error when showing user role: " + e.getMessage());
            return ResponseHelper.error(ResponseHelper.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/changeuserrole")
    public Response changeUserRole(InputTokenWrapper<ChangeUserRoleRequest> body) {
        ChangeUserRoleRequest req = body.input;

        LOG.fine("changeUserRole: " + req.username);

        if (!req.isValid() || !body.token.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_INPUT);
        }

        try {
            TokenEntity token = dao.validateToken(body.token.tokenId);

            if (!token.role.equals(Role.ADMIN)) {
                LOG.warning("Unauthorized role: " + token.role);
                return ResponseHelper.error(ResponseHelper.UNAUTHORIZED);
            }

            dao.changeUserRole(req.username, req.newRole);
            return ResponseHelper.ok(Map.of("message", "Role updated successfully"));
        } catch (RuntimeException ex) {
            return ResponseHelper.error(ex.getMessage());
        }
    }

    @POST
    @Path("/changeuserpwd")
    public Response changeUserPassword(InputTokenWrapper<ChangeUserPasswordRequest> body) {
        ChangeUserPasswordRequest req = body.input;

        LOG.fine("changeUserPassword: " + req.username);

        if (!req.isValid() || !body.token.isValid()) {
            return ResponseHelper.error(ResponseHelper.INVALID_INPUT);
        }

        try {
            TokenEntity token = dao.validateToken(body.token.tokenId);

            if (!token.username.equals(req.username)) {
                LOG.warning("Unauthorized role: " + token.role);
                return ResponseHelper.error(ResponseHelper.UNAUTHORIZED);
            }

            dao.changeUserPassword(req.username, req.oldPassword, req.newPassword);
            return ResponseHelper.ok(Map.of("message", "Password changed successfully"));
        } catch (RuntimeException ex) {
            return ResponseHelper.error(ex.getMessage());
        }
    }
}

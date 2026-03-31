package org.example.persistence;

import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.model.Role;
import org.example.model.TokenEntity;
import org.example.model.UserEntity;
import org.example.util.ResponseHelper;

import java.util.ArrayList;
import java.util.List;

public class DAO {
    private static final Datastore datastore = createDatastore();

    private static Datastore createDatastore() {
        String emulatorHost = System.getenv("DATASTORE_EMULATOR_HOST");

        if (emulatorHost != null) {
            // use emulator
            return DatastoreOptions.newBuilder()
                    .setProjectId("test-project")
                    .setHost("http://" + emulatorHost)
                    .build()
                    .getService();
        }

        // use datastore
        return DatastoreOptions.newBuilder()
                .setProjectId("individual-project-491518")
                .build()
                .getService();
    }

    private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
    private final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");

    public TokenEntity validateToken(String tokenId) {
        Key key = tokenKeyFactory.newKey(tokenId);
        Entity tokenEntity = datastore.get(key);
        if (tokenEntity == null) {
            throw new RuntimeException(ResponseHelper.INVALID_TOKEN);
        }

        TokenEntity token = TokenEntity.fromEntity(tokenEntity);
        if (token.isExpired()) {
            throw new RuntimeException(ResponseHelper.TOKEN_EXPIRED);
        }

        Key userKey = userKeyFactory.newKey(token.username);
        Entity userEntity = datastore.get(userKey);
        if (userEntity == null) {
            throw new RuntimeException(ResponseHelper.FORBIDDEN);
        }

        return token;
    }

    public UserEntity getUser(String username) {
        Key key = userKeyFactory.newKey(username);
        Entity entity = datastore.get(key);
        if (entity == null) {
            throw new RuntimeException(ResponseHelper.USER_NOT_FOUND);
        }

        return UserEntity.fromEntity(entity);
    }

    public void createUser(String username, String password, String phone, String address, Role role) {
        UserEntity newUser = new UserEntity(
                username,
                DigestUtils.sha512Hex(password),
                phone,
                address,
                role);

        Transaction txn = datastore.newTransaction();
        try {
            Key key = userKeyFactory.newKey(username);
            Entity entity = txn.get(key);
            if (entity != null) {
                throw new RuntimeException(ResponseHelper.USER_ALREADY_EXISTS);
            }

            txn.put(newUser.toEntity(datastore));
            txn.commit();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(ResponseHelper.INTERNAL_SERVER_ERROR);
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    public TokenEntity login(String username, String password) {
        UserEntity user = getUser(username);
        if (!user.hashPassword.equals(DigestUtils.sha512Hex(password))) {
            throw new RuntimeException(ResponseHelper.INVALID_CREDENTIALS);
        }

        TokenEntity token = new TokenEntity(user.username, user.role);

        datastore.put(token.toEntity(datastore));
        return token;
    }

    public List<UserEntity> getAllUsers() {
        Query<Entity> query = Query.newEntityQueryBuilder().setKind("User").build();
        QueryResults<Entity> users = datastore.run(query);

        List<UserEntity> userList = new ArrayList<>();
        users.forEachRemaining(entity -> userList.add(UserEntity.fromEntity(entity)));

        return userList;
    }

    public void deleteUser(String username) {
        Key key = userKeyFactory.newKey(username);
        Entity entity = datastore.get(key);
        if (entity == null) {
            throw new RuntimeException(ResponseHelper.USER_NOT_FOUND);
        }

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("Token")
                .setFilter(StructuredQuery.PropertyFilter.eq("username", username))
                .build();
        QueryResults<Entity> tokens = datastore.run(query);

        List<Key> keysToDelete = new ArrayList<>();
        tokens.forEachRemaining(e -> keysToDelete.add(e.getKey()));

        if (!keysToDelete.isEmpty()) {
            datastore.delete(keysToDelete.toArray(new Key[0]));
        }

        datastore.delete(key);
    }

    public void modifyUser(String username, String newPhone, String newAddress) {
        UserEntity user = getUser(username);
        UserEntity newUser = new UserEntity(
                user.username,
                user.hashPassword,
                newPhone,
                newAddress,
                user.role
        );
        datastore.put(newUser.toEntity(datastore));
    }

    public void changeUserRole(String username, Role newRole) {
        Key key = userKeyFactory.newKey(username);
        Entity entity = datastore.get(key);
        if (entity == null) {
            throw new RuntimeException(ResponseHelper.USER_NOT_FOUND);
        }

        UserEntity user = UserEntity.fromEntity(entity);
        UserEntity newUser = new UserEntity(
                user.username,
                user.hashPassword,
                user.phone,
                user.address,
                newRole
        );

        // update user tokens with new role
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("Token")
                .setFilter(StructuredQuery.PropertyFilter.eq("username", username))
                .build();
        QueryResults<Entity> tokens = datastore.run(query);

        List<Entity> updatedTokens = new ArrayList<>();
        tokens.forEachRemaining(t -> {
            Entity updated = Entity.newBuilder(t)
                    .set("role", newRole.toString())
                    .build();
            updatedTokens.add(updated);
        });

        if (!updatedTokens.isEmpty()) {
            datastore.put(updatedTokens.toArray(new Entity[0]));
        }

        datastore.put(newUser.toEntity(datastore));
    }

    public void changeUserPassword(String username, String oldPassword, String newPassword) {
        UserEntity user = getUser(username);

        if (!user.hashPassword.equals(DigestUtils.sha512Hex(oldPassword))) {
            throw new RuntimeException(ResponseHelper.INVALID_CREDENTIALS);
        }

        UserEntity newUser = new UserEntity(
                user.username,
                DigestUtils.sha512Hex(newPassword),
                user.phone,
                user.address,
                user.role
        );

        logoutAllSessions(user.username);

        datastore.put(newUser.toEntity(datastore));
    }

    public List<TokenEntity> getAuthSessions() {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("Token")
                .setFilter(StructuredQuery.PropertyFilter.gt("expiresAt", System.currentTimeMillis()))
                .build();
        QueryResults<Entity> tokens = datastore.run(query);

        List<TokenEntity> tokenList = new ArrayList<>();
        tokens.forEachRemaining(entity -> tokenList.add(TokenEntity.fromEntity(entity)));

        return tokenList;
    }

    public void logoutAllSessions(String username) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("Token")
                .setFilter(StructuredQuery.PropertyFilter.eq("username", username))
                .build();
        QueryResults<Entity> tokens = datastore.run(query);

        List<Key> keysToDelete = new ArrayList<>();
        tokens.forEachRemaining(e -> keysToDelete.add(e.getKey()));

        datastore.delete(keysToDelete.toArray(new Key[0]));
    }

    public void logout(String tokenId) {
        Key key = tokenKeyFactory.newKey(tokenId);
        Entity entity = datastore.get(key);
        if (entity == null) {
            throw new RuntimeException(ResponseHelper.INVALID_TOKEN);
        }

        datastore.delete(key);
    }
}

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
    private static final Datastore datastore = DatastoreOptions.newBuilder()
            .setProjectId("individual-project-491518")
            .build()
            .getService();

    private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
    private final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");

    public TokenEntity validateToken(String tokenId) {
        Key key = tokenKeyFactory.newKey(tokenId);
        Entity tokeneEntity = datastore.get(key);
        if (tokeneEntity == null) {
            throw new RuntimeException(ResponseHelper.INVALID_TOKEN);
        }

        TokenEntity token = TokenEntity.fromEntity(tokeneEntity);
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

    public void createUser(String username, String password, String phone, String address, Role role) {
        UserEntity newUser = new UserEntity(username, password, phone, address, role);

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
        Key key = userKeyFactory.newKey(username);
        Entity entity = datastore.get(key);
        if (entity == null) {
            throw new RuntimeException(ResponseHelper.USER_NOT_FOUND);
        }

        UserEntity user = UserEntity.fromEntity(entity);
        if (!user.password.equals(DigestUtils.sha512Hex(password))) {
            throw new RuntimeException(ResponseHelper.INVALID_CREDENTIALS);
        }

        TokenEntity token = new TokenEntity(user.username, user.role);

        datastore.put(token.toEntity(datastore));
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

        datastore.delete(keysToDelete.toArray(new Key[0]));

        datastore.delete(key);
    }

    public void modifyUser(String username, String newPhone, String newAddress) {
        UserEntity user = getUser(username);
        UserEntity newUser = new UserEntity(
                user.username,
                user.password,
                newPhone,
                newAddress,
                user.role
        );
        datastore.put(newUser.toEntity(datastore));
    }

    public void changeUserRole(String username, Role newRole) {
        UserEntity user = getUser(username);
        UserEntity newUser = new UserEntity(
                user.username,
                user.password,
                user.phone,
                user.address,
                newRole
        );
        datastore.put(newUser.toEntity(datastore));
    }

    public void changeUserPassword(String username, String oldPassword, String newPassword) {
        UserEntity user = getUser(username);

        if (!user.password.equals(DigestUtils.sha512Hex(oldPassword))) {
            throw new RuntimeException(ResponseHelper.INVALID_CREDENTIALS);
        }

        UserEntity newUser = new UserEntity(
                user.username,
                DigestUtils.sha512Hex(newPassword),
                user.phone,
                user.address,
                user.role
        );
        datastore.put(newUser.toEntity(datastore));
    }

    public List<TokenEntity> getAllTokens() {
        Query<Entity> query = Query.newEntityQueryBuilder().setKind("Token").build();
        QueryResults<Entity> tokens = datastore.run(query);

        List<TokenEntity> tokenList = new ArrayList<>();
        tokens.forEachRemaining(entity -> tokenList.add(TokenEntity.fromEntity(entity)));

        return tokenList;
    }

    public void logout(String username, String tokenId) {
        Key tokenKey = tokenKeyFactory.newKey(tokenId);
        Entity tokenEntity = datastore.get(tokenKey);
        if (tokenEntity == null) {
            throw new RuntimeException(ResponseHelper.INVALID_TOKEN);
        }

        TokenEntity token = TokenEntity.fromEntity(tokenEntity);
        if (!token.username.equals(username)) {
            throw new RuntimeException(ResponseHelper.FORBIDDEN);
        }

        datastore.delete(tokenKey);
    }
}

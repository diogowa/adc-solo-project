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
            throw new RuntimeException(ResponseHelper.INVALID_TOKEN);
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
        Transaction txn = datastore.newTransaction();
        try {
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

            txn.put(token.toEntity(datastore));
            txn.commit();

            return token;
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

    public UserEntity getUser(String username) {
        Key key = userKeyFactory.newKey(username);
        Entity entity = datastore.get(key);
        if (entity == null) {
            throw new RuntimeException(ResponseHelper.USER_NOT_FOUND);
        }

        return UserEntity.fromEntity(entity);
    }

    public List<UserEntity> getUsers() {
        Query<Entity> query = Query.newEntityQueryBuilder().setKind("User").build();
        QueryResults<Entity> users = datastore.run(query);

        List<UserEntity> userList = new ArrayList<>();
        users.forEachRemaining(entity -> userList.add(UserEntity.fromEntity(entity)));

        return userList;
    }

    public void deleteUser(String username) {
        Transaction txn = datastore.newTransaction();
        try {
            Key key = userKeyFactory.newKey(username);
            Entity entity = txn.get(key);
            if (entity == null) {
                throw new RuntimeException(ResponseHelper.USER_NOT_FOUND);
            }

            txn.delete(key);
            txn.commit();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(ResponseHelper.INTERNAL_SERVER_ERROR);
        } finally  {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    public void modifyUser(String username, String newPhone, String newAddress) {
        Transaction txn = datastore.newTransaction();
        try {
            Key key = userKeyFactory.newKey(username);
            Entity entity = txn.get(key);
            if (entity == null) {
                throw new RuntimeException(ResponseHelper.USER_NOT_FOUND);
            }

            UserEntity user = UserEntity.fromEntity(entity);

            UserEntity newUser = new UserEntity(
                    user.username,
                    user.password,
                    newPhone,
                    newAddress,
                    user.role
            );

            txn.put(newUser.toEntity(datastore));
            txn.commit();
        }  catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(ResponseHelper.INTERNAL_SERVER_ERROR);
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    public void changeUserRole(String username, Role newRole) {
        Transaction txn = datastore.newTransaction();
        try {
            Key key = userKeyFactory.newKey(username);
            Entity entity = txn.get(key);
            if (entity == null) {
                throw new RuntimeException(ResponseHelper.USER_NOT_FOUND);
            }

            UserEntity user = UserEntity.fromEntity(entity);

            UserEntity newUser = new UserEntity(
                    user.username,
                    user.password,
                    user.phone,
                    user.address,
                    newRole
            );

            txn.put(newUser.toEntity(datastore));
            txn.commit();
        }  catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(ResponseHelper.INTERNAL_SERVER_ERROR);
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    public void changeUserPassword(String username, String oldPassword, String newPassword) {
        Transaction txn = datastore.newTransaction();
        try {
            Key key = userKeyFactory.newKey(username);
            Entity entity = txn.get(key);
            if (entity == null) {
                throw new RuntimeException(ResponseHelper.USER_NOT_FOUND);
            }

            UserEntity user = UserEntity.fromEntity(entity);
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

            txn.put(newUser.toEntity(datastore));
            txn.commit();
        }  catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(ResponseHelper.INTERNAL_SERVER_ERROR);
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    public List<TokenEntity> getAllTokens() {
        Query<Entity> query = Query.newEntityQueryBuilder().setKind("Token").build();
        QueryResults<Entity> tokens = datastore.run(query);

        List<TokenEntity> tokenList = new ArrayList<>();
        tokens.forEachRemaining(entity -> tokenList.add(TokenEntity.fromEntity(entity)));

        return tokenList;
    }
}

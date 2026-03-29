package org.example.persistence;

import com.google.cloud.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.model.Role;
import org.example.model.UserEntity;
import org.example.util.ResponseHelper;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final Datastore datastore = DatastoreOptions.newBuilder()
            .setProjectId("individual-project-491518")
            .build()
            .getService();

    private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

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

    public void updateUser(String username, String password, String phone, String address, Role role) {
        UserEntity newUser = new UserEntity(username, password, phone, address, role);

        Transaction txn = datastore.newTransaction();
        try {
            Key key = userKeyFactory.newKey(username);

            Entity entity = txn.get(key);
            if (entity == null) {
                throw new RuntimeException(ResponseHelper.USER_NOT_FOUND);
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

    public UserEntity login(String username, String password) {
        UserEntity user = getUser(username);
        if (user == null) {
            throw new RuntimeException(ResponseHelper.USER_NOT_FOUND);
        }

        if (!user.password.equals(DigestUtils.sha512Hex(password))) {
            throw new RuntimeException(ResponseHelper.INVALID_CREDENTIALS);
        }

        return user;
    }
}

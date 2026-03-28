package org.example.persistence;

import com.google.cloud.datastore.*;
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

    private final TokenDAO tokenDAO = new TokenDAO();

    public void createUser(String username, String password, String phone, String address, Role role) throws RuntimeException {
        UserEntity newUser = new UserEntity(username, password, phone, address, role);

        Transaction txn = datastore.newTransaction();
        try {
            Key key = datastore.newKeyFactory().setKind("User").newKey(username);

            Entity exists = txn.get(key);
            if (exists != null) {
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
            Key key = datastore.newKeyFactory().setKind("User").newKey(username);

            Entity exists = txn.get(key);
            if (exists == null) {
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
        Key key = datastore.newKeyFactory().setKind("User").newKey(username);

        Entity exists = datastore.get(key);
        if (exists == null) {
            throw new RuntimeException(ResponseHelper.USER_NOT_FOUND);
        }

        return UserEntity.fromEntity(exists);
    }

    public List<UserEntity> getUsers() throws RuntimeException {
        Query<Entity> query = Query.newEntityQueryBuilder().setKind("User").build();
        QueryResults<Entity> users = datastore.run(query);

        List<UserEntity> userList = new ArrayList<>();
        users.forEachRemaining(entity -> userList.add(UserEntity.fromEntity(entity)));

        return userList;
    }

    public void deleteUser(String username) throws RuntimeException {
        Transaction txn = datastore.newTransaction();
        try {
            Key key = datastore.newKeyFactory().setKind("User").newKey(username);

            Entity exists = txn.get(key);
            if (exists == null) {
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
}

package org.example.persistance;

import com.google.cloud.datastore.*;
import org.example.model.UserEntity;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final Datastore datastore = DatastoreOptions.newBuilder()
            .setProjectId("individual-project-491518")
            .build()
            .getService();

    public boolean createUser(UserEntity user) {
        Transaction txn = datastore.newTransaction();
        try {
            Key key = datastore.newKeyFactory().setKind("User").newKey(user.username);

            Entity exists = txn.get(key);
            if (exists != null) {
                txn.rollback();
                return false;
            }

            txn.put(user.toEntity(datastore));
            txn.commit();
            return true;
        } catch (Exception e) {
            txn.rollback();
            return false;
        }
    }

    public void updateUser(UserEntity user) {
        datastore.put(user.toEntity(datastore));
    }

    public UserEntity getUser(String username) {
        Key key = datastore.newKeyFactory().setKind("User").newKey(username);
        return UserEntity.fromEntity(datastore.get(key));
    }

    public List<UserEntity> getUsers() {
        Query<Entity> query = Query.newEntityQueryBuilder().setKind("User").build();
        QueryResults<Entity> users = datastore.run(query);

        List<UserEntity> userList = new ArrayList<>();
        users.forEachRemaining(entity -> userList.add(UserEntity.fromEntity(entity)));

        return userList;
    }

    public void deleteUser(String username) {
        Key key = datastore.newKeyFactory().setKind("User").newKey(username);
        datastore.delete(key);
    }
}

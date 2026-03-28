package org.example.persistence;

import com.google.cloud.datastore.*;
import org.apache.http.util.EntityUtils;
import org.example.model.TokenEntity;
import org.example.model.UserEntity;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final Datastore datastore = DatastoreOptions.newBuilder()
            .setProjectId("individual-project-491518")
            .build()
            .getService();

    private final TokenDAO tokenDAO = new TokenDAO();

    public boolean createUser(UserEntity user) {
        Transaction txn = datastore.newTransaction();
        try {
            Key key = datastore.newKeyFactory().setKind("User").newKey(user.username);

            Entity exists = txn.get(key);
            if (exists != null) {
                return false;
            }

            txn.put(user.toEntity(datastore));
            txn.commit();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    public boolean updateUser(UserEntity user) {
        Transaction txn = datastore.newTransaction();
        try {
            Key key = datastore.newKeyFactory().setKind("User").newKey(user.username);

            Entity exists = txn.get(key);
            if (exists == null) {
                return false;
            }

            txn.put(user.toEntity(datastore));
            txn.commit();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
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

    public boolean deleteUser(String username) {
        Transaction txn = datastore.newTransaction();
        try {
            Key key = datastore.newKeyFactory().setKind("User").newKey(username);

            Entity exists = txn.get(key);
            if (exists == null) {
                return false;
            }

            txn.delete(key);

            List<TokenEntity> tokens = tokenDAO.getUserTokens(username, txn);
            for (TokenEntity token : tokens) {
                tokenDAO.deleteToken(token.tokenId, txn);
            }

            txn.commit();
            return true;
        } catch (Exception e) {
            return false;
        } finally  {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
}

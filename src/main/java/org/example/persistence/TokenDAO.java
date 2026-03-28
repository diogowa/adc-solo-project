package org.example.persistence;

import com.google.cloud.datastore.*;
import org.example.model.TokenEntity;
import org.example.util.ResponseHelper;

import java.util.ArrayList;
import java.util.List;

public class TokenDAO {
    private static final Datastore datastore = DatastoreOptions.newBuilder()
            .setProjectId("individual-project-491518")
            .build()
            .getService();

    public void saveToken(TokenEntity token) {
        datastore.put(token.toEntity(datastore));
    }

    public TokenEntity validateToken(String tokenId) throws RuntimeException {
        Key key = datastore.newKeyFactory().setKind("Token").newKey(tokenId);

        Entity entity = datastore.get(key);
        if (entity == null) {
            throw new RuntimeException(ResponseHelper.INVALID_TOKEN);
        }

        TokenEntity token = TokenEntity.fromEntity(entity);
        if (token.isExpired()) {
            throw new RuntimeException(ResponseHelper.TOKEN_EXPIRED);
        }

        return token;
    }

    public TokenEntity getToken(String tokenId) {
        Key key = datastore.newKeyFactory().setKind("Token").newKey(tokenId);

        Entity exists = datastore.get(key);
        if (exists == null) {
            throw new RuntimeException(ResponseHelper.TOKEN_EXPIRED);
        }

        return TokenEntity.fromEntity(exists);
    }

    public List<TokenEntity> getUserTokens(String username) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("Token")
                .setFilter(StructuredQuery.PropertyFilter.eq("username", username))
                .build();
        QueryResults<Entity> tokens = datastore.run(query);

        List<TokenEntity> tokenList = new ArrayList<>();
        tokens.forEachRemaining(entity -> tokenList.add(TokenEntity.fromEntity(entity)));

        return tokenList;
    }

    public List<TokenEntity> getAllTokens() {
        Query<Entity> query = Query.newEntityQueryBuilder().setKind("Token").build();
        QueryResults<Entity> tokens = datastore.run(query);

        List<TokenEntity> tokenList = new ArrayList<>();
        tokens.forEachRemaining(entity -> tokenList.add(TokenEntity.fromEntity(entity)));

        return tokenList;
    }

    public void deleteToken(String tokenId) {
        Transaction txn = datastore.newTransaction();
        try {
            Key key = datastore.newKeyFactory().setKind("Token").newKey(tokenId);

            Entity exists = txn.get(key);
            if (exists == null) {
                throw new RuntimeException(ResponseHelper.TOKEN_EXPIRED);
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

    public void deleteUserTokens(String username) {
        List<TokenEntity> tokens = getUserTokens(username);
        for (TokenEntity token : tokens) {
            deleteToken(token.tokenId);
        }
    }
}

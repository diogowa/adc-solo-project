package org.example.persistence;

import com.google.cloud.datastore.*;
import org.example.model.Token;
import org.example.model.TokenEntity;

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

    public TokenEntity getToken(String tokenId) {
        Key key = datastore.newKeyFactory().setKind("Token").newKey(tokenId);
        return TokenEntity.fromEntity(datastore.get(key));
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

    public void deleteToken(String tokenId) {
        Key key = datastore.newKeyFactory().setKind("Token").newKey(tokenId);
        datastore.delete(key);
    }
}

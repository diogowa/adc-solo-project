package org.example.persistence;

import com.google.cloud.datastore.*;
import org.example.model.Role;
import org.example.model.TokenEntity;
import org.example.util.ResponseHelper;

import java.util.ArrayList;
import java.util.List;

public class TokenDAO {
    private static final Datastore datastore = DatastoreOptions.newBuilder()
            .setProjectId("individual-project-491518")
            .build()
            .getService();

    private final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");

    public TokenEntity validateToken(String tokenId) {
        Key key = tokenKeyFactory.newKey(tokenId);

        Entity entity = datastore.get(key);
        if (entity == null) {
            throw new RuntimeException(ResponseHelper.INVALID_TOKEN);
        }

        TokenEntity token = TokenEntity.fromEntity(entity);
        if (token.isExpired()) {
            throw new RuntimeException(ResponseHelper.TOKEN_EXPIRED);
        }

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(token.username);
        Entity userEntity = datastore.get(userKey);
        if (userEntity == null) {
            throw new RuntimeException(ResponseHelper.INVALID_TOKEN);
        }

        return token;
    }

    public TokenEntity saveToken(String username, Role role) {
        TokenEntity token = new TokenEntity(username, role);

        datastore.put(token.toEntity(datastore));

        return token;
    }

    public List<TokenEntity> getAllTokens() {
        Query<Entity> query = Query.newEntityQueryBuilder().setKind("Token").build();
        QueryResults<Entity> tokens = datastore.run(query);

        List<TokenEntity> tokenList = new ArrayList<>();
        tokens.forEachRemaining(entity -> tokenList.add(TokenEntity.fromEntity(entity)));

        return tokenList;
    }
}

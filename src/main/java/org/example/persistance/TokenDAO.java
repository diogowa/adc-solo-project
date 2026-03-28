package org.example.persistance;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import org.example.model.TokenEntity;

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
}

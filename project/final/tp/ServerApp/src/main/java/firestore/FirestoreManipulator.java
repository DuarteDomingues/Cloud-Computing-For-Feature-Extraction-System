package firestore;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;

import java.util.HashMap;

import static main.GlobalVars.DEFAULT_COLLECTION;

public class FirestoreManipulator {

    private final Firestore db;

    public FirestoreManipulator(GoogleCredentials credentials) {
        FirestoreOptions options = FirestoreOptions
                .newBuilder().setCredentials(credentials).build();
        db = options.getService();
    }

    public CollectionReference getCollection() {
        return db.collection(DEFAULT_COLLECTION);
    }

    public void firestoreClose() throws Exception {
        this.db.close();
    }

    public boolean placeInFirestoreDict(String hash, HashMap<String, String> newEntryToFirebase) {
        System.out.println("hash " + hash);
        DocumentReference docRef = getCollection().document(hash);
        ApiFuture<WriteResult> resultApiFuture = docRef.create(newEntryToFirebase);
        return resultApiFuture.isDone() && !resultApiFuture.isCancelled();
    }
}

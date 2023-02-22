package cn.operations;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;

import java.io.*;
import java.sql.Date;
import java.sql.SQLOutput;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Operations {

    static Firestore db;
    static String currentCollection;

    public static void init(String pathFileKeyJson, String collectionName) throws IOException {
        GoogleCredentials credentials = null;
        if (pathFileKeyJson != null) {
            InputStream serviceAccount = new FileInputStream(pathFileKeyJson);
            credentials = GoogleCredentials.fromStream(serviceAccount);
        } else {
            // use GOOGLE_APPLICATION_CREDENTIALS environment variable
            credentials = GoogleCredentials.getApplicationDefault();
        }
        FirestoreOptions options = FirestoreOptions
                .newBuilder().setCredentials(credentials).build();
        db = options.getService();
        currentCollection=collectionName;
    }

    public static void close() throws Exception {
        db.close();
    }

// Slides Operations
    // option 0
    public static void insertUserBillGates () throws Exception {
        CollectionReference colRef = db.collection("Users");
        DocumentReference docRef = colRef.document("Bill-Gates");
        HashMap<String, Object> map = new HashMap<String, Object>() {
            {
                put("first", "Bill");
                put("last", "Grates");
                put("born", 1955);
            }
        };
        ApiFuture<WriteResult> result = docRef.create(map); //Create new document
        //ApiFuture<WriteResult> result = docRef.set(map); //Overwrites the document
        // update field
        map.put("last", "Gates");
        result = docRef.update(map);
    }
    // option 1
    public static void insertDocumentAsObject(String FILE_CSV) throws Exception {
        BufferedReader reader = new BufferedReader(
                new FileReader( FILE_CSV ));
        CollectionReference colRef = db.collection(currentCollection);
        String line;
        // Insert
        while ((line = reader.readLine()) != null) {
            System.out.println("Inserting: " + line);
            String[] cols = line.split(",");
            OcupacaoTemporaria ocup = new OcupacaoTemporaria();
            ocup.ID = Integer.parseInt(cols[0]);
            ocup.location = new Localizacao();
            ocup.location.point = new GeoPoint(Double.parseDouble(cols[1]), Double.parseDouble(cols[2]));
            ocup.location.coord = new Coordenadas();
            ocup.location.coord.X = Double.parseDouble(cols[1]);
            ocup.location.coord.Y = Double.parseDouble(cols[2]);
            ocup.location.freguesia = cols[3];
            ocup.location.local = cols[4];
            ocup.event = new Evento();
            ocup.event.evtID = Integer.parseInt(cols[5]);
            ocup.event.nome = cols[6];
            ocup.event.tipo = cols[7];
            ocup.event.details = new HashMap<String, String>();
            if (!cols[8].isEmpty()) ocup.event.details.put("Participantes", cols[8]);
            if (!cols[9].isEmpty()) ocup.event.details.put("Custo", cols[9]);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            ocup.event.dtInicio = formatter.parse(cols[10]);
            ocup.event.dtFinal = formatter.parse(cols[11]);
            ocup.event.licenciamento = new Licenciamento();
            ocup.event.licenciamento.code = cols[12];
            ocup.event.licenciamento.dtLicenc = formatter.parse(cols[13]);
            DocumentReference docRef = colRef.document("DocID"+ocup.ID );
            //asynchronously write data
            ApiFuture<WriteResult> result = docRef.set(ocup);
            System.out.println("Update time : " + result.get().getUpdateTime());
        }
    }
    // option 2
    public static void listAllDocuments() throws ExecutionException, InterruptedException {
        CollectionReference cref = db.collection(currentCollection);
        Iterable<DocumentReference> allDocs = cref.listDocuments();
        for (DocumentReference docref : allDocs) {
            ApiFuture<DocumentSnapshot> docfut = docref.get();
            DocumentSnapshot doc = docfut.get();
            System.out.println(doc.getData().toString());
        }
    }
    // option 3
    public static void readDocumentByID(String ID) throws Exception {
        //String ID = "1111";
        DocumentReference docRef = db.collection(currentCollection).document(ID);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        System.out.println(document.getData().toString());
        //ler campo
        GeoPoint coord = document.getGeoPoint("location.point");
        System.out.println("COORD:"+coord.toString());
        //ler objeto: obtém campos do documento para campos com o mesmo nome na classe
        OcupacaoTemporaria ocup = document.toObject(OcupacaoTemporaria.class);
        System.out.println("POINT:"+ocup.location.point.toString());
    }
    // option 4
    public static void deleteField(String docID, String fieldName) throws Exception {
        DocumentReference docRef =
                db.document(currentCollection+"/"+docID);
        // apagar campo
        Map<String, Object> updates = new HashMap<>();
        updates.put(fieldName, FieldValue.delete());
        ApiFuture<WriteResult> writeResult = docRef.update(updates);
        System.out.println("Update time : " + writeResult.get());
    }
    // option 5
    public static void deleteDocument(String docID) throws Exception {
        DocumentReference docRef =
                db.document(currentCollection+"/"+docID);
        // apagar documento
        ApiFuture<WriteResult> resFuture = docRef.delete();
        WriteResult res = resFuture.get();
    }
    // option 6
    public static void querySimpleByID(String docID) throws Exception {
        // Single query
        Query query = db
                .collection(currentCollection)
                .whereGreaterThan("ID", Integer.parseInt(docID));
        // retrieve  query results asynchronously using query.get()
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            System.out.print("Doc id: " + doc.getId());
            System.out.println(" Freguesia: " + doc.get("location.freguesia"));
        }

    }


    public static void queryArrays(String collection, String hobby) throws ExecutionException, InterruptedException {
        Query query = db
                .collection(collection).whereArrayContains("hobbies",hobby);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            System.out.print("Doc id: " + doc.getId());
            System.out.println(" Nome: " + doc.get("first"));
        }
    }

    // option 7
    public static void queryComposedFields() throws ExecutionException, InterruptedException {
        FieldPath fp = FieldPath.of("location", "freguesia");
        Query query = db.collection(currentCollection).whereEqualTo(fp, "Belém");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot doc: querySnapshot.get().getDocuments()) {
            System.out.println("Doc id: " + doc.getId() + " @ "+ doc.get("location"));
        }

        System.out.println("\nQuery by Location coordinates");
        fp = FieldPath.of("location","coord");
        HashMap<String,Double> cor = new HashMap<String, Double>() {
            { put("X",-9.143645364765742); put("Y",38.753404328752353); }
        };
        query = db.collection(currentCollection).whereEqualTo(fp, cor);
        querySnapshot = query.get();
        for (DocumentSnapshot doc: querySnapshot.get().getDocuments()) {
            System.out.print("Doc id: " + doc.getId() + " @ "+ doc.get("location"));
        }
    }
    // option 8
    public static void composeQueryWithIndex() throws Exception {
        // Composed query needs an index
        FieldPath fpath = FieldPath.of("location", "freguesia");

        Query query = db.collection(currentCollection)
                .whereEqualTo(fpath, "Misericórdia")
                .whereLessThan("ID", 2100);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            System.out.println(doc.getId() + ":Doc:" + doc.getData());
        }
    }
}

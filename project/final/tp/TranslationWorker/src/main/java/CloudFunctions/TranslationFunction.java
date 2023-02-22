package CloudFunctions;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class TranslationFunction implements BackgroundFunction<TranslationFunction.Message> {
    private static final Logger logger = Logger.getLogger(TranslationFunction.class.getName());
    // when it's instanced it gets the firestore automatically
    private static final Firestore firestore = FirestoreOptions.getDefaultInstance().getService();

    private final Translate translate = TranslateOptions.getDefaultInstance().getService();

    @Override
    public void accept(Message message, Context context) {
        String data = new String(Base64.getDecoder().decode(message.data));
        logger.info("Decode " + data);
        var keys = message.attributes.keySet();
        logger.info("MAP ----\n" + message.attributes);
        var traducoes = new ArrayList<String>();

        for (var key : keys) {
            var tr = translateString(key);
            System.out.println(tr);
            traducoes.add(tr.toLowerCase());
        }
        logger.info("Traducoes : " + traducoes);
        var dict = new HashMap<String, Object>();
        dict.put("labels-pt", traducoes);

        try {
            placeInFirestoreDict(data, dict);
        } catch (IOException | ExecutionException |
                InterruptedException exception) {
            exception.printStackTrace();
            logger.warning(exception.getMessage());
        }
    }

    private String translateString(String original) {
        Translation translation =
                translate.translate(
                        original,
                        Translate.TranslateOption.sourceLanguage("en"),
                        Translate.TranslateOption.targetLanguage("pt"),
                        // Use "base" for standard edition, "nmt" for the premium model.
                        Translate.TranslateOption.model("base"));
        return translation.getTranslatedText();
    }


    public boolean placeInFirestoreDict(String hash, HashMap<String, Object> newEntryToFirebase) throws IOException, ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("final_cn_g14")
                .document(hash);
        ApiFuture<WriteResult> resultApiFuture = docRef.update(newEntryToFirebase);
        resultApiFuture.get();//Impede o programa de terminar até que seja enviado para o firestore
        return resultApiFuture.isDone() && !resultApiFuture.isCancelled();
    }


    public class Message {
        String data;
        HashMap<String, String> attributes;
        String messageId;
        String publishTime;
    }
}

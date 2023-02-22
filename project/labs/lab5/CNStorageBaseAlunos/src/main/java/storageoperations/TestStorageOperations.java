package storageoperations;

import com.google.auth.oauth2.ComputeEngineCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class TestStorageOperations {

    private static Scanner scan = new Scanner(System.in);

    static int Menu() {
        int option;
        do {
            System.out.println("######## MENU ##########");
            System.out.println("Options for Google Storage Operations:");
            System.out.println(" 0: List Buckets in Project");
            System.out.println(" 1: Create a new Bucket");
            System.out.println(" 2: Upload Blob to Bucket");
            System.out.println(" 3: Download Blob from Bucket");
            System.out.println(" 4: Delete a Bucket");
            System.out.println(" 5: Make a blob public");
            System.out.println("..........");
            System.out.println("99: Exit");
            System.out.print("Enter an Option:");
            option = scan.nextInt();
        } while (!((option >= 0 && option <= 4) || option == 99));
        return option;
    }

    public static void main(String[] args) throws Exception {
        // case extfile args[0] - absolutepath do Accountkey.json
        // Assumes the environment variable
        // set GOOGLE_APPLICATION_CREDENTIALS= < ServiceAccountroject.json>
        //Storage storage = StorageOptions.getDefaultInstance().getService();
        Storage storage = null;
        GoogleCredentials credentials = null;
        StorageOptions storageOptions = null;


        if (args.length == 0) {
            storageOptions = StorageOptions.getDefaultInstance();
        } else {
            System.out.println("File " + args[0]);
            credentials = GoogleCredentials.fromStream(new FileInputStream(args[0]));
            storageOptions = StorageOptions.newBuilder().setCredentials(credentials).build();
        }
        storage = storageOptions.getService();
        String projID = storageOptions.getProjectId();
        if (projID != null) System.out.println("Current Project ID:" + projID);
        else {
            System.out.println("The environment variable GOOGLE_APPLICATION_CREDENTIALS isn't well defined!!");
            System.exit(-1);
        }
        StorageOperations soper = new StorageOperations(storage);
        boolean end = false;
        while (!end) {
            try {
                int option = Menu();
                switch (option) {
                    case 0:
                        soper.listBuckets(projID);
                        break;
                    case 1:
                        soper.CreateBucket();
                        break;
                    case 2:
                        soper.uploadBlobToBucket();
                        break;
                    case 3:
                        soper.downloadBlobFromBucket();
                        break;
                    case 4:
                        soper.deleteBucket();
                        break;
                    // Other Operations
                    case 5:
                        soper.makePublicBlob();
                        break;
                    case 99:
                        end = true;
                        System.exit(0);
                }
            } catch (Exception ex) {
                System.out.println("Erro executing operations!");
                ex.printStackTrace();
                end = true;
            }
        }
    }
}

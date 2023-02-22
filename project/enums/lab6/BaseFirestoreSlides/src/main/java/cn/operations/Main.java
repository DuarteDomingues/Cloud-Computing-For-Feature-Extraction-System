package cn.operations;

import java.io.IOException;
import java.util.Scanner;


public class Main {
    static String FILE_CSV = "OcupacaoEspacosPublicos.csv";
    static String currentCollection = "ocupa-espacos";

    public static void menu() {
        System.out.println("Options:");
        System.out.println("0. Insert Bill Gates as document in <users> collection");
        System.out.println("1. Load CSV file to an object and insert in collection");
        System.out.println("2. List all Collection documents");
        System.out.println("3. Read document by ID");
        System.out.println("4. Delete a field document");
        System.out.println("5. Delete Document");
        System.out.println("6. Simple query ID greater than");
        System.out.println("7. Query with composed fields");
        System.out.println("8. Composed query with index");
        System.out.println("99. Exit");
    }

    public static void main(String[] args) throws IOException {
        try {
            // args[0] can or cannot pass the pathname of account key KEY_JSON
            String docID;
            Scanner input = new Scanner(System.in);
            currentCollection = read("Enter name for collection: ", input);
            Operations.init(args.length == 0 ?null:args[0], currentCollection);

            // assumindo que na coleção Users dos slides, cada documento (user) tem um field Array com os hobbies
            Operations.queryArrays("Users", "futebol");

            while (true) {
                menu();
                String option = input.nextLine();
                switch (option) {
                    case "0":
                        Operations.insertUserBillGates();
                        //Operations.listAllDocuments_v2();
                        break;
                    case "1":
                        Operations.insertDocumentAsObject(FILE_CSV);
                        break;
                    case "2":
                        Operations.listAllDocuments();
                        break;
                    case "3":
                        docID = read("Enter doc id to read:", input);
                        Operations.readDocumentByID(docID);
                        break;
                    case "4":
                        docID = read("Enter doc id to delete field:", input);
                        String fieldName = read("Enter field name:", input);
                        Operations.deleteField(docID, fieldName);
                        break;
                    case "5":
                        docID = read("Enter doc id to delete:", input);
                        Operations.deleteDocument(docID);
                        break;
                    case "6":
                        docID = read("Enter doc id to query ID greater than :", input);
                        Operations.querySimpleByID(docID);
                        break;
                    case "7":
                        Operations.queryComposedFields();
                        break;
                    case "8":
                        Operations.composeQueryWithIndex();
                        break;
                    default:
                        System.out.println("Option error: try again");
                        break;
                    case "99":
                        Operations.close();
                        System.exit(0);
                        break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String read(String msg, Scanner input) {
        System.out.println(msg);
        return input.nextLine();
    }
}

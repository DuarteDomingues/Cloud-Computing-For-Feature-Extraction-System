package visiontranslate;


import java.util.List;
import java.util.Scanner;

public class AppMain  {

    static String bucketName;
    static String blobName;

    public static void main(String[] args) {
        try {
            bucketName=read("Qual o nome do Bucket?");
            while (true) {
                blobName=read("Qual o nome do Blob (exit to finish)?");
                if (blobName.compareTo("exit") == 0) System.exit(0);
                List<String> labels = DetectTranslateAPIs.detectLabels(bucketName, blobName);
                List<String> labelsTranslated = DetectTranslateAPIs.TranslateLabels(labels);
                System.out.println("** Original labels **");
                for (String lab : labels)
                    System.out.print(lab+", ");
                System.out.println("\n** Translated to portuguese **");
                for (String labt : labelsTranslated)
                    System.out.print(labt+", ");
                System.out.println();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static String read(String txt) {
        Scanner scanner =new Scanner(System.in);
        System.out.println(txt);
        return scanner.nextLine();
    }

}

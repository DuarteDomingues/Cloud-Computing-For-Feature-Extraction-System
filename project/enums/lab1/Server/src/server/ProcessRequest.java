package server;

public class ProcessRequest {
    public static String processar(String request) throws InterruptedException {
        String[] tmp = request.split(" ");
        String bigWord = "";
        for (String s : tmp) {
            if (s.length() > bigWord.length()) {
                bigWord = s;
            }
        }
        // simula tempo de processamento
        Thread.sleep(10*1000);
        // ...
        System.out.println("Processing complete: " + request + " *** Response: " + bigWord);
        return bigWord;
    }

}

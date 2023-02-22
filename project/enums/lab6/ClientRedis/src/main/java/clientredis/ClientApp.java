package clientredis;
/*
https://tool.oschina.net/uploads/apidocs/jedis-2.1.0/redis/clients/jedis/Jedis.html
*/
// 10.220.164.179
// 10.187.58.107


import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

public class ClientApp {


    static int Menu() {
        Scanner scan = new Scanner(System.in);
        int option;
        do {
            System.out.println("######## MENU ##########");
            System.out.println("Redis Operations:");
            System.out.println(" 1: Insert (Key, Value)");
            System.out.println(" 2: Retrieve Value from Key");
            System.out.println(" 3: Store data: Colour list");
            System.out.println(" 4: Retrieve colour list");
            System.out.println(" 5: Store a value with a hash key");
            System.out.println("..........");
            System.out.println("99: Exit");
            System.out.print("Enter an Option:");
            option = scan.nextInt();
        } while (!((option >= 1 && option <= 5) || option == 99));
        return option;
    }

    private static String read(String msg, Scanner input) {
        System.out.println(msg);
        return input.nextLine();
    }

    public static void main(String[] args) {
        try {
             String JedisIP="localhost";
             if (args.length > 0) JedisIP=args[0];
            //Connecting to Redis server on host ip=args[0]
            Jedis jedis = new Jedis(JedisIP, 6379);
            System.out.println("Redis server is running on " + args[0] + " port=6379 " + jedis.ping());
            Scanner input = new Scanner(System.in);
            String key;
            String value;
            while (true) {
                switch (Menu()) {
                    case 1:
                        key = read("Qual a Chave?", input);
                        value = read("Qual o Valor?", input);
                        System.out.println(jedis.set(key, value));
                        break;
                    case 2:
                        key = read("Qual a Chave para obter value?", input);
                        value = jedis.get(key);
                        if (value == null)
                            System.out.println("Key " + key + " inexistent");
                        else System.out.println("Value of " + key + ": " + value);
                        break;
                    case 3:
                        jedis.lpush("colourList", "green");
                        jedis.lpush("colourList", "blue");
                        jedis.lpush("colourList", "red");
                        jedis.lpush("colourList", "black");
                        break;
                    case 4:

                        long size = jedis.llen("colourList");
                        //System.out.println(size);
                        List<String> list = jedis.lrange("colourList", 0, size);
                        System.out.println("ColourList:");
                        for (String s : list)
                            System.out.println("   " + s);
                        break;
                    case 5:
                        value = read("Qual o Valor?", input);
                        MessageDigest funcDigest = MessageDigest.getInstance("SHA-256");
                        byte[] arr=funcDigest.digest(value.getBytes());
                        key=Base64.getEncoder().withoutPadding().encodeToString(arr);
                        System.out.println(jedis.set(key, value)+" key="+key);
                        break;
                    default:
                        System.out.println("Option error: try again");
                        break;
                    case 99:
                        jedis.close();
                        System.exit(0);
                        break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

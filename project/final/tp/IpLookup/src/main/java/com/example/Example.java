package com.example;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Example implements HttpFunction {
    private static GoogleCredentials credentials = getComputeEngineCredentials();
    private static final Logger logger = Logger.getLogger(Example.class.getName());

    public static GoogleCredentials getComputeEngineCredentials() {
        try {
            return GoogleCredentials.getApplicationDefault();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        String instanceName = httpRequest.getFirstQueryParameter("server").orElse("instance-group-server");
        ArrayList<String> ipAddresses = listVMInstances("robust-seat-309417", "europe-west2-c", instanceName);

        logger.info("Request with parameters " + httpRequest.getQueryParameters());
        logger.info("REQUEST " + httpRequest);

        if (ipAddresses != null) {
            String result = "";
            for (int i = 0; i < ipAddresses.size(); i++) {
                result += ipAddresses.get(i) + ";";
            }
            try {

                httpResponse.getOutputStream().write(result.getBytes());
                httpResponse.setStatusCode(200, result);
            } catch (IOException e) {
                e.printStackTrace();
                logger.info("Error sending the content to the client!");
                httpResponse.setStatusCode(500, result);
            }
        } else
            httpResponse.setStatusCode(503);

    }

    // to show the VM must be running
    public static ArrayList<String> listVMInstances(String project, String zone, String instanceName) {
        if (credentials == null) {
            logger.info("Error connecting to Compute Engine. Exiting function");
            throw new RuntimeException("Error connecting to Compute Engine");
        }

        try {
            ArrayList<String> ipAddresses = new ArrayList<>();
            try (InstancesClient client = InstancesClient.create()) {
                for (Instance e : client.list(project, zone).iterateAll()) {
                    if (e.getStatus() == Instance.Status.RUNNING) {
                        if (e.getName().startsWith(instanceName)) {
                            String ip = e.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                            ipAddresses.add(ip);
                            logger.info("Name : " + e.getName() + "; IP: " + ip);
                        }
                    }
                }
            }
            return ipAddresses;
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("Error accessing the Compute Engine!");
        }
        return null;
    }
}

package de.imi.fhir;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ReadFromServer {

    public JSONObject readFromUrl(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == 200) {
                StringBuilder contentString = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while (scanner.hasNextLine()) {
                    contentString.append(scanner.nextLine());
                    contentString.append("\n");
                }
                scanner.close();
                return new JSONObject(contentString.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

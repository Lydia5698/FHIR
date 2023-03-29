package de.imi.fhir;

import org.hl7.fhir.r4.model.Observation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MyConceptMap {

    private final JSONObject conceptMap;

    public MyConceptMap(String url) {
        conceptMap = readFromUrl(url);
    }

    public Observation.ObservationStatus getObservationStatusStatusFor(String statusValue) {
        if (statusValue == null) {
            return Observation.ObservationStatus.NULL;
        }
        if (conceptMap != null) {
            String codeString = mapStatusValue(statusValue);
            return Observation.ObservationStatus.fromCode(codeString);
        }
        return Observation.ObservationStatus.NULL;
    }

    private String mapStatusValue(String statusValue) {
        JSONArray group = conceptMap.getJSONArray("group");
        JSONObject foo = (JSONObject) group.get(0);
        JSONArray elements = foo.getJSONArray("element");
        for (int i=0; i<elements.length(); i++) {
            JSONObject element = elements.getJSONObject(i);
            if (element.getString("code").equals(statusValue)) {
                JSONArray target = element.getJSONArray("target");
                JSONObject targetElement = (JSONObject) target.get(0);
                return targetElement.getString("code");
            }
        }
        return null;
    }

    private JSONObject readFromUrl(String url) {
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

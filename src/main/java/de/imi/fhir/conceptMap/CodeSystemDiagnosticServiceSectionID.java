package de.imi.fhir.conceptMap;

import de.imi.fhir.ReadFromServer;
import org.json.JSONArray;
import org.json.JSONObject;

public class CodeSystemDiagnosticServiceSectionID {

    private final JSONObject conceptMap;
    ReadFromServer readFromServer = new ReadFromServer();

    public CodeSystemDiagnosticServiceSectionID(String url) {
        conceptMap = readFromServer.readFromUrl(url);
    }

    public String getDiagnosticDisplayFor(String serviceID) {
        if (serviceID != null) {
            return mapStatusValue(serviceID).getString("display");
        }
        return null;
    }


    private JSONObject mapStatusValue(String value) {
        JSONArray concept = conceptMap.getJSONArray("concept");
        for (int i = 0; i < concept.length(); i++) {
            JSONObject element = concept.getJSONObject(i);
            if (element.getString("code").equals(value)) {
                return element;
            }
        }
        return null;
    }
}

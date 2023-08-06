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

    public String getDiagnosticIDFor(String abnormalFlag) {
        if (abnormalFlag != null) {
            return mapStatusValue(abnormalFlag);
        }
        return null;
    }

    private String mapStatusValue(String statusValue) {
        JSONArray concept = conceptMap.getJSONArray("concept");
        for (int i=0; i<concept.length(); i++) {
            JSONObject element = concept.getJSONObject(i);
            if (element.getString("code").equals(statusValue)) {
                return element.getString("display");
            }
        }
        return null;
    }
}

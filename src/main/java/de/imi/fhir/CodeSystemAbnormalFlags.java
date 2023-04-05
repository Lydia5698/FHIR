package de.imi.fhir;

import org.json.JSONArray;
import org.json.JSONObject;

public class CodeSystemAbnormalFlags {
    private final JSONObject conceptMap;
    ReadFromServer readFromServer = new ReadFromServer();

    public CodeSystemAbnormalFlags(String url) {
        conceptMap = readFromServer.readFromUrl(url);
    }

    public String getAbnormalFlagFor(String abnormalFlag) {
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

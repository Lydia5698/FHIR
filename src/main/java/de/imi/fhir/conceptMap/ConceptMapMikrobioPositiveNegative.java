package de.imi.fhir.conceptMap;

import de.imi.fhir.ReadFromServer;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConceptMapMikrobioPositiveNegative {
    private final JSONObject conceptMap;
    ReadFromServer readFromServer = new ReadFromServer();

    public ConceptMapMikrobioPositiveNegative(String url){
        conceptMap = readFromServer.readFromUrl(url);

    }
    public String getTargetCode(String code){
        if (code != null){
            return mapStatusValue(code).getString("code");
        }
        return null;
    }

    public String getTargetDisplay(String code){
        if (code != null){
            return mapStatusValue(code).getString("display");
        }
        return null;
    }

    private JSONObject mapStatusValue(String value) {
        JSONArray group = conceptMap.getJSONArray("group");
        JSONObject foo = (JSONObject) group.get(0);
        JSONArray elements = foo.getJSONArray("element");
        for (int i=0; i<elements.length(); i++) {
            JSONObject element = elements.getJSONObject(i);
            if (element.getString("code").equals(value)) {
                JSONArray target = element.getJSONArray("target");
                return (JSONObject) target.get(0);
            }
        }
        return null;
    }

}

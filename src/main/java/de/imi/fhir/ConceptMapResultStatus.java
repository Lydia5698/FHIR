package de.imi.fhir;

import org.hl7.fhir.r4.model.Observation;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConceptMapResultStatus {

    private final JSONObject conceptMap;
    ReadFromServer readFromServer = new ReadFromServer();

    public ConceptMapResultStatus(String url) {
        conceptMap = readFromServer.readFromUrl(url);
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


}

package de.imi.fhir;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class V2 {
    JSONObject observationJSON;
    ReadFromServer readFromServer = new ReadFromServer();
    public V2(JSONObject observationJSON) throws IOException, DataTypeException {
        this.observationJSON = observationJSON;
        getV2Message();

    }

    private void getV2Message() throws DataTypeException {
        LinkedList<JSONObject> contentJsonDatei = getCoding();
        ADT_A01 adtMessage = new ADT_A01();
        MSH mshSegment = adtMessage.getMSH();
        mshSegment.getFieldSeparator().setValue("|");
        mshSegment.getEncodingCharacters().setValue("^~\\&");
        //mshSegment.getDateTimeOfMessage().getTimeOfAnEvent().setValue(currentDateTimeString);
        mshSegment.getVersionID().getVersionID().setValue("2.4");
        OBX obx = adtMessage.getOBX();
        for (int x = 0; x < contentJsonDatei.size(); x++){
            String system = contentJsonDatei.get(0).getString("system");
            switch (system){
                case "Observation Identifier":

                    // hier HL7 Feld
                case "Observation Values":

                case "Units":

                case "Abnormal Flags":


            }
        }
    }

    LinkedList<JSONObject> getCoding() {
        LinkedList<JSONObject> contentJsonDatei = new LinkedList<JSONObject>();
        JSONArray component = observationJSON.getJSONArray("component");
        for (int i=0; i<component.length(); i++) {
            if (!component.getJSONObject(i).isNull("code")) {
                JSONObject code = component.getJSONObject(i).getJSONObject("code");
                JSONArray coding = code.getJSONArray("coding");
                for (int j=0; j<coding.length(); j++) {
                    contentJsonDatei.add((JSONObject) coding.get(j));
                }
            }
            if (!component.getJSONObject(i).isNull("valueCodeableConcept")) {
                JSONObject valueCodeableConcept = component.getJSONObject(i).getJSONObject("valueCodeableConcept");
                JSONArray coding = valueCodeableConcept.getJSONArray("coding");
                for (int j=0; j<coding.length(); j++){
                    contentJsonDatei.add((JSONObject) coding.get(j));
                }
            }
            if (!component.getJSONObject(i).isNull("interpretation")) {
                JSONArray interpretation = component.getJSONObject(i).getJSONArray("interpretation");
                JSONArray coding = interpretation.getJSONObject(0).getJSONArray("coding");
                for (int j=0; j<coding.length(); j++){
                    contentJsonDatei.add((JSONObject) coding.get(j));
                }
            }
        }
        return contentJsonDatei;
    }

}



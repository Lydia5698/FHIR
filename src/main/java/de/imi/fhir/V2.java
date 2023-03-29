package de.imi.fhir;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileReader;
import java.io.IOException;

public class V2 {




    public V2() throws IOException {
        // System.out.print(JsonPath.read(object, "$.component[0]").toString());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        ObservationJSON observation = objectMapper.readValue(new FileReader("src/main/resources/outputs/Directory1/child0.json"), ObservationJSON.class);

        // HashMap<String, String> map = (HashMap<String, String>) objectMapper.readValue(json, new TypeReference<Map<String, String>>(){});

        System.out.println(observation.getComponent().get(1).getCode().getCoding().get(0).getSystem()); //TODO FINAL etc. muss groß geschrieben werden sonst nicht erkannt

        for (int x = 0; x < 10; x++){
            String system = observation.getComponent().get(x).getCode().getCoding().get(0).getSystem();
            switch (system){
                case "Observation Identifier":
                    // hier HL7 Feld
                case "Observation Values":

                case "Units":

                case "Abnormal Flags":


            }
        }

    }
}



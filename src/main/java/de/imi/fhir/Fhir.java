package de.imi.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.datatype.ST;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.parser.Parser;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.r4.model.*;
import org.json.JSONObject;
import org.json.JSONArray;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class Fhir {


// TODO: 16.02.23 baccor^Prevotella corporis^keim^5380 5380 lokaler Code
// TODO: 29.03.23 Datum OBX.12 mit einfügen?

// TODO: 13.02.23 Paths überprüfen

    HapiContext context = new DefaultHapiContext();
    Parser p = context.getPipeParser();
    private MyConceptMap conceptMap = new MyConceptMap("http://localhost:8888/fhir/ConceptMap/1?_format=application/fhir+json");
    public void ausgabe() throws HL7Exception, IOException  {
        Path path = Path.of("src/main/resources/MiBi/multiple868");
        String hl7String = Files.readString(path, StandardCharsets.ISO_8859_1);
        Message message = p.parse(hl7String);
        ORU_R01 oruR01 = (ORU_R01) p.parse(message.encode());
        saveToFhir(oruR01, "/Users/lydia/Desktop/Uni/6 Semester/BA Script/src/main/resources/outputs/Directory1");
    }
    public void saveToFhir(ORU_R01 oruR01, String path) throws IOException, HL7Exception {
        File directory = new File(path);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        parser.setPrettyPrint(true);

        int orderObservationCount = 0;
        ORU_R01_ORDER_OBSERVATION orderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(orderObservationCount);

        Observation parentObservation = new Observation();
        // Kulturdiagnostik
        // String profile = "https://highmed.org/fhir/StructureDefinition/ic/Kulturdiagnostik&scope=MedizininformatikInitiative-HiGHmed-IC@current";
        // Directory
        // String profileDirectory = "src/main/resources/Profiles/MII/medizininformatikinitiative-highmed-ic/ressourcen-profile";
        parentObservation.getMeta().addProfile("https://highmed.org/fhir/StructureDefinition/ic/Kulturdiagnostik&scope=MedizininformatikInitiative-HiGHmed-IC@current");

        while (!orderObservation.isEmpty()) {
            File childFile = new File(path + "/child" + orderObservationCount + ".json");
            FileWriter fileWriter = null;
            try {
                Observation childObservation;

                // Profil der HiGHmed
                // Beispiel für den Status, OBX.11 getObservationResultStatus()
                childObservation = antibiogramm(orderObservation);

                //System.out.println(parser.encodeResourceToString(childObservation));
                // String encode = parser.encodeResourceToString(childObservation);

                fileWriter = new FileWriter(childFile);
                fileWriter.write(parser.encodeResourceToString(childObservation));

                Reference parentReference = new Reference();
                parentReference.setReference(path + "/child" + orderObservationCount + ".json");
                parentObservation.addHasMember(parentReference);

                orderObservationCount++;
                orderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(orderObservationCount);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                try {
                    if (fileWriter != null){
                        fileWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Get the file
        File parentFile = new File(path+"/parent.json");
        FileWriter fr = null;

        try {
            fr = new FileWriter(parentFile);
            fr.write(parser.encodeResourceToString(parentObservation));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // todo Validator mit HTTP Put an den Marshal
        // http://localhost:8888/fhir/ConceptMap/1?_format=application/fhir+json


    }


    public Observation antibiogramm(ORU_R01_ORDER_OBSERVATION orderObservation) throws HL7Exception, IOException {
        Observation childObservation = new Observation();
        int rowCount = 0;
        // Verschiedene unter Observations also OBX|2, OBX|3 ...
        OBX obx = orderObservation.getOBSERVATION(rowCount).getOBX();
        while(!obx.isEmpty()) {
            // Profil der HiGHmed
            // Beispiel für den Status, OBX.11 getObservationResultStatus()
            childObservation.addComponent();
            ID status = obx.getObservationResultStatus();
            Observation.ObservationStatus observationStatus = conceptMap.getObservationStatusStatusFor(status.getValue());
            childObservation.setStatus(observationStatus);

            String observationValues = "";
            for (int n = 0; n < obx.getObservationValue().length; n++) {
                Varies observationValueVaries = obx.getObservationValue(n);
                Type data = observationValueVaries.getData();
                String encoded;
                //noinspection SwitchStatementWithTooFewBranches
                switch (obx.getValueType().getValue()) {
                    case "ST":
                        encoded = ((ST) data).getValue();
                        break;
                    default:
                        encoded = obx.getObservationValue(n).encode();
                        break;
                }
                observationValues = observationValues.concat(encoded);
            }
            CodeableConcept container = new CodeableConcept();
            container.addCoding().setSystem("Observation Values").setCode("Code einfügen").setDisplay(observationValues);
            container.addCoding().setSystem("Units").setCode("Code einfügen").setDisplay(obx.getUnits().encode()); // Units OBX.6
            childObservation.getComponent().get(rowCount).setValue(container); // OBX 5 Observation Value and Units OBX.6

            CodeableConcept interpretation = new CodeableConcept();
            String[] stringArray = new String[obx.getAbnormalFlags().length];
            for (int i = 0; i < obx.getAbnormalFlags().length; i++) {
                stringArray[i] = obx.getAbnormalFlags(i).encode();
            }
            // OBX.8 Abnormal Flags "https://r4.ontoserver.csiro.au/fhir/CodeSystem/v2-0078?_format=application/fhir+json"
            if (!ArrayUtils.isEmpty(stringArray)) {
                interpretation.addCoding().setSystem("Abnormal Flags").setCode(stringArray[0]).setDisplay(checkWithOntoserver("abnormal", stringArray[0]));
            }
            childObservation.getComponent().get(rowCount).addInterpretation(interpretation);
            CodeableConcept code = new CodeableConcept();
            code.addCoding().setSystem("Observation Identifier").setCode("Code einfügen").setDisplay(obx.getObservationIdentifier().encode()); // Observation Identifier OBX.3
            childObservation.getComponent().get(rowCount).setCode(code); // OBX.3

            rowCount++;
            obx = orderObservation.getOBSERVATION(rowCount).getOBX();
        }
        return childObservation;

    }


    /**
     * Checks whether the content of the OBX segment can be found in the corresponding table.
     * @param toCheck The Name of the OBX segment
     * @param checkLetter The content of the OBX segment
     * @return Returns either all codes from the specific table (Example for Abnormal Flags: N,R,S,...) or all contents of the table for a specific code (Example for Abnormal Flags and Letter N: N,	Normal (applies to non-numeric results))
     * @throws IOException If no HTML connection is established
     */
    public String checkWithOntoserver(String toCheck, String checkLetter) throws IOException {
        String fieldId = null; // TODO: 06.02.23 Vielleicht einen standard einbauen wegen null exception?
        switch (toCheck){
            case "abnormal":
                fieldId = "v2-0078";
                break;
            case "observationStatus":
                fieldId = "v2-0085";
                break;
            case "orderControl":
                fieldId = "v2-0119";
                break;
            case "orderStatus":
                fieldId = "v2-0038";
        }
        HttpURLConnection connection = (HttpURLConnection) new URL("https://r4.ontoserver.csiro.au/fhir/CodeSystem/" + fieldId + "?_format=application/fhir+json").openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        // System.out.println("HTML Response Code: "+responseCode);
        /**
         * read the json document from the URL
         */
        JSONArray code;
        if (responseCode == 200) {
            StringBuilder response = new StringBuilder();
            Scanner scanner = new Scanner(connection.getInputStream());
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
                response.append("\n");
            }
            scanner.close();
            JSONObject jsonObject = new JSONObject(response.toString());

            // All codes from Result Status OBR
            // gets Display for Abnormal Flag OBX.8 where the code equals the gives Letter, Example Letter = N than Display would be = Normal
            if (toCheck.equals("abnormal")) {
                code = jsonObject.getJSONArray("concept");
                for (int i = 0; i < code.length(); i++) {
                    JSONObject element = code.getJSONObject(i);
                    if (element.getString("code").equals(checkLetter)) {
                        return element.getString("display");
                    }
                }
            }


        }
        return null;

    }
}

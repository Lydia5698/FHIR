import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v25.datatype.ST;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.r4.model.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


public class Fhir {

// TODO: 13.02.23 OBX 1,2,3 jedes mal eine neue Componente erstellen? Namen der Componenten z.b OBX 1,2 gibt es dort Namenskonventionen
//  TODO: 04.02.23 Wie Enum erweitern von Result Status
// todo: Validator herunterladen und nutzen?
// TODO: 16.02.23  Wird in dem Value alles vom Observation Value bzw. Observation Identifier gespeichert außer dem Code? Also z.b. baccor^Prevotella corporis^keim^5380 ohne 5380?

// TODO: 13.02.23 Paths überprüfen
    public void saveToFhir(ORU_R01 oruR01) throws IOException, HL7Exception {
        ORU_R01_ORDER_OBSERVATION orderObservation;
        OBX obx;
        try {

            // Get the file
            File f = new File("D:src/main/resources/outputs/example.json");

            // Create new file
            // Check if it does not exist
            if (f.createNewFile())
                System.out.println("File created");
            else
                System.out.println("File already exists");
        }
        catch (Exception ignored) {

        }
        Observation parentObservation = new Observation();
        // Kulturdiagnostik
        // String profile = "https://highmed.org/fhir/StructureDefinition/ic/Kulturdiagnostik&scope=MedizininformatikInitiative-HiGHmed-IC@current";
        // Directory
        // String profileDirectory = "src/main/resources/Profiles/MII/medizininformatikinitiative-highmed-ic/ressourcen-profile";
        parentObservation.getMeta().addProfile("https://highmed.org/fhir/StructureDefinition/ic/Kulturdiagnostik&scope=MedizininformatikInitiative-HiGHmed-IC@current");

        for (int j = 0; j<8; j++) { // todo max anzahl bestimmen
           if (!oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j).isEmpty()) {
               orderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j);
               obx = orderObservation.getOBSERVATION().getOBX(); // get getOBSERVATION(1) etc. für OBX|2| // TODO: 03.03.23 unterobservations durchgehen und sinnvoll speichern

               Observation childObservation = new Observation();

               // Profil der HiGHmed
               // Beispiel für den Status, OBX.11 getObservationResultStatus()
               // fillObservation(j, parentObservation, obx); Parent observation bekommt nur Meta etc?

               FhirContext ctx = FhirContext.forR4();
               IParser parser = ctx.newJsonParser();
               // TODO: 07.02.23 in FHIR Dokument speichern und nur updaten bzw. neue Componente hinzufügen

               // Beispiel für Has Member für die Antibios
               // parentObservation.addHasMember().setReference("/Users/lydia/Desktop/Uni/6 Semester/BA Script/src/main/resources/MiBi/multiple1");
               childObservation = antibiogramm(oruR01,j);
               parser.setPrettyPrint(true);
               System.out.println(parser.encodeResourceToString(childObservation));
           }
       }

        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        parser.setPrettyPrint(true);

        String encode = parser.encodeResourceToString(parentObservation);
        //System.out.println(encode);

        FileWriter file = new FileWriter("src/main/resources/outputs/example.json"); //todo Überschreibt noch die alte eingabe
        file.write(parser.encodeResourceToString(parentObservation));
        file.close();
        //// TODO: 03.03.23 Referencen herraus finden und mit parent observation verlinken

        // todo: Wo wird der Value gespeichert? Wird immer im Observation value gespeichert


        // todo Validator herunterladen und nutzen?

    }

    public Observation antibiogramm(ORU_R01 oruR01, int j) throws HL7Exception, IOException {
        Observation childObservation = new Observation();
        ORU_R01_ORDER_OBSERVATION orderObservation;
        OBX obx;
        orderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j);
        for (int x = 0; x < 20; x++){
            if (!orderObservation.getOBSERVATION(x).isEmpty()){
                obx = orderObservation.getOBSERVATION(x).getOBX();
                fillObservation(x, childObservation, obx);
            }
            /*obx = orderObservation.getOBSERVATION(x).getOBX(); // get getOBSERVATION(1) etc. für OBX|2| // TODO: 03.03.23 unterobservations durchgehen und sinnvoll speichern
            if (!obx.isEmpty()){
                fillObservation(j, childObservation, obx);
            }*/

        }
        return childObservation;

    }

    private void fillObservation(int j, Observation observation, OBX obx) throws HL7Exception, IOException {
        // Profil der HiGHmed
        // Beispiel für den Status, OBX.11 getObservationResultStatus()
        observation.setStatus(Observation.ObservationStatus.FINAL); // TODO: 04.02.23 Wie Enum erweitern

        // StatusKulturdiagnostik statusKulturdiagnostik = new StatusKulturdiagnostik(observation);
        // OBX.? Value, OBX.6 Unit and OBX.5
        // parentObservation.setValue(new Quantity().setValue(123.4).setUnit(obx.getUnits().encode()));
        CodeableConcept observationValue = new CodeableConcept();

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

        //observationValue.addCoding().setSystem("Snap2Snomed").setCode().setDisplay(obx.getObservationValue(2).toString());
        CodeableConcept code = new CodeableConcept();
        // OBX.3 Observation Identifier

        CodeableConcept interpretation = new CodeableConcept();
        String[] stringArray = new String[obx.getAbnormalFlags().length];
        for (int i = 0; i < obx.getAbnormalFlags().length; i++) {
            stringArray[i] = obx.getAbnormalFlags(i).encode();
        }
        // OBX.8 Abnormal Flags
        if (!ArrayUtils.isEmpty(stringArray)) {
            interpretation.addCoding().setSystem("https://r4.ontoserver.csiro.au/fhir/CodeSystem/v2-0078?_format=application/fhir+json").setCode(stringArray[0]).setDisplay(checkOntoserver("abnormal", stringArray[0]).toString().replaceAll("[^a-zA-Z]", ""));
        }
        //parentObservation.addInterpretation(interpretation);
        observation.addComponent();
        //parentObservation.addComponent().setCode(new CodeableConcept().setText("OBX "+j)); // Statt Observation Value welches OBX?
        observation.getComponent().get(j).addInterpretation(interpretation);
        observationValue.addCoding().setSystem("Observation Values").setCode("Code einfügen").setDisplay(observationValues); // Observation Identifier OBX.3
        observationValue.addCoding().setSystem("Units").setCode("Code einfügen").setDisplay(obx.getUnits().encode()); // Units OBX.6
        observation.getComponent().get(j).setValue(observationValue); // OBX 5
        // code.addCoding().setSystem("URL einfügen").setCode("Code einfügen").setDisplay(observationValues);
        code.addCoding().setSystem("Observation identifier").setCode("Code einfügen").setDisplay(obx.getObservationIdentifier().encode());
        observation.getComponent().get(j).setCode(code); // OBX.3
    }

    /**
     * Checks whether the content of the OBX segment can be found in the corresponding table.
     * @param toCheck The Name of the OBX segment
     * @param checkLetter The content of the OBX segment
     * @return Returns either all codes from the specific table (Example for Abnormal Flags: N,R,S,...) or all contents of the table for a specific code (Example for Abnormal Flags and Letter N: N,	Normal (applies to non-numeric results))
     * @throws IOException If no HTML connection is established
     */
    public JSONArray checkOntoserver(String toCheck, String checkLetter) throws IOException {
        JSONArray code = null;
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
        if (responseCode == 200) {
            StringBuilder response = new StringBuilder();
            Scanner scanner = new Scanner(connection.getInputStream());
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
                response.append("\n");
            }
            scanner.close();
            JSONParser parser = new JSONParser();
            try {
                Object object = parser
                        .parse(response.toString());

                //convert Object to JSONObject
                JSONObject jsonObject = (JSONObject) object;
                // All codes from Result Status OBR
                switch (toCheck){
                    // gets Display for Abnormal Flag OBX.8 where the code equals the gives Letter, Example Letter = N than Display would be = Normal
                    case "abnormal":
                        code = JsonPath.read(jsonObject, "$.concept[?(@.code =='" + checkLetter + "')].display");
                        break;
                    // gets all Codes
                    default:
                        code = JsonPath.read(jsonObject, "$.concept[*].code");
                        break;
                }


            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return code;

    }
}

// JSON FHIR Patient
//System.out.println(patient.getName()); //human.name
       /* FhirContext ctx = FhirContext.forDstu3();
        //IGenericClient client = ctx.newRestfulGenericClient("https://hapi.fhir.org/baseR4");
        Patient patient = new Patient();
        patient.addName().setFamily(pid.getPatientName(0).getFamilyName().getSurname().getValue()).addGiven(pid.getPatientName(0).getGivenName().getValue());
        patient.setBirthDate(pid.getDateTimeOfBirth().getTime().getValueAsDate());
        //patient.setGender(Enumerations.AdministrativeGender.valueOf(pid.getAdministrativeSex().getValue()));

        IParser parser = ctx.newJsonParser();
        parser.setPrettyPrint(true);

        String encode = parser.encodeResourceToString(patient);
        System.out.println(encode);*/
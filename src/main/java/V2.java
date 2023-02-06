import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Hl7InputStreamMessageIterator;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;


public class V2 {

    HapiContext context = new DefaultHapiContext();
    Parser p = context.getPipeParser();
    Message hapiMsg;
    int anhang = 1;
    Fhir fhir = new Fhir();
       
    public void ausgabe() throws HL7Exception, IOException  {
        for (int i = 0; i< 1; i++){
            // todo Only absolute Path possible ??
            //InputStream inputStream = new FileInputStream("/Users/lydia/Desktop/Uni/6 Semester/BA Script/src/main/resources/KC/ORU"+ anhang);
            //Hl7InputStreamMessageIterator streamMessageIterator = new Hl7InputStreamMessageIterator(inputStream);
            Path path = Path.of("/Users/lydia/Desktop/Uni/6 Semester/BA Script/src/main/resources/MiBi/multiple3");
            String hl7String = Files.readString(path, StandardCharsets.ISO_8859_1);
            Message message = p.parse(hl7String);
            //Message message = streamMessageIterator.next();
            ORU_R01 oruR01 = (ORU_R01) p.parse(message.encode());
            ORU_R01_ORDER_OBSERVATION orderObservation;
            // When there are more than one OBX,OBR,ORC
           /* for (int j = 0; j<2; j++){
                if(!oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j).isEmpty()){
                    orderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j);
                    obx(orderObservation);

                }
            }*/
            fhir.saveToFhir(oruR01);

            anhang+=1;
            System.out.println(i);
        }

    }
    public void obx(ORU_R01_ORDER_OBSERVATION orderObservation) throws HL7Exception, IOException {
        //OBX obx = orderObservation.getOBSERVATION().getOBX();
        ORC orc = orderObservation.getORC();
        OBR obr = orderObservation.getOBR();
        Object orderStatus = orc.getOrderStatus();
        //String identifier = obx.getValueType().encode();
        String check = orc.getOrderStatus().encode();
        // Id from the Ontoserver for the specific Field in this case Result Status OBR v2-0123
        // Abnormal Flags v2-0078
        // Observation Result Status v2-0085
        // Order Control v2-0119
        // Order Status v2-0038
        //String fieldId = "v2-0078";


        JSONArray resultStatus = checkOntoserver("allCodes", " ");
        assert !resultStatus.isEmpty();
        if (resultStatus.contains(check)){
            System.out.println(resultStatus);
        }

    }

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
        System.out.println("HTML Response Code: "+responseCode);
        // read the json document from the URL
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
                    case "allCodes":
                        code = JsonPath.read(jsonObject, "$.concept[*].code");
                        break;
                }


            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return code;

    }


    public V2() {
    }
}


      /*
                            To Write Fields to Document
            for (int i = 0; i< 2; i++){

            InputStream inputStream = new FileInputStream("/Users/lydia/Desktop/Uni/6 Semester/BA/mibi-on-fhir/Code/src/main/resources/adt/KC/ORU"+ anhang);
            Hl7InputStreamMessageIterator streamMessageIterator = new Hl7InputStreamMessageIterator(inputStream);
            Message message = streamMessageIterator.next();
            ORU_R01 oruR01 = (ORU_R01) p.parse(message.encode());
            ORU_R01_ORDER_OBSERVATION orderObservation;
            // When there are more than one OBX,OBR,ORC
            for (int j = 0; j<2; j++){
                if(!oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j).isEmpty()){
                    orderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j);
                    obx(orderObservation);

                }
            }

            anhang+=1;
            System.out.println(i);
        }

                    OBX obx = orderObservation.getOBSERVATION().getOBX();
                    String identifier = obx.getObservationResultStatus().encode();
                    ClassLoader classLoader = getClass().getClassLoader();
                    Path path = Path.of("/Users/lydia/Desktop/Uni/6 Semester/BA/mibi-on-fhir/Code/src/main/resources/MiBi/obxMibi.txt");
                    List<String> contentList = Files.readAllLines(path, StandardCharsets.UTF_8);
                    Writer writer = new BufferedWriter(new FileWriter("obxMibi.txt", true));
                    if (!contentList.contains(identifier)) {
                        writer.write(identifier);
                        writer.write("\n" + anhang + " " + j + "\n");
                        writer.close();

                    }*/


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
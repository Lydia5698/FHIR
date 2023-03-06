import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.parser.Parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;



public class V2 {

    HapiContext context = new DefaultHapiContext();
    Parser p = context.getPipeParser();
    int anhang = 1;
    Fhir fhir = new Fhir();
       
    public void ausgabe() throws HL7Exception, IOException  {
        for (int i = 0; i< 1; i++){
            // todo Only absolute Path possible ??
            //InputStream inputStream = new FileInputStream("/Users/lydia/Desktop/Uni/6 Semester/BA Script/src/main/resources/KC/ORU"+ anhang);
            //Hl7InputStreamMessageIterator streamMessageIterator = new Hl7InputStreamMessageIterator(inputStream);
            Path path = Path.of("/Users/lydia/Desktop/Uni/6 Semester/BA Script/src/main/resources/MiBi/multiple868");
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
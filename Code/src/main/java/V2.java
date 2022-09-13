import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Hl7InputStreamMessageIterator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class V2 {


  /*  String message = "MSH|^~\\&|ORBIS|UKSH-HL|CLAR_HL|Hygieneportal|20210722164734||ADT^A01^ADT_A01|9095e934-81ab-4ac1-b76f-26631695fffb|P|2.5||**REMOVED** (msh continuation pointer)|AL|NE||8859/1\n" +
            "EVN|A01|202107221647||**REMOVED** (event reason code)|UKSH-HL|202107221507\n" +
            "PID|1||18360014||Blick8961^Nella5604^^^^^L~||19800630|F|||~|||||||||||||||||||N\n" +
            "PV1|1|H|LA018^^^LKCHI^HL^66240000|^^HL7||||~~|||||||||||253754957|||||||||||||||||||||||||202107221507|||||||A\n" +
            "ZBE|101560467^ORBIS|202107221507||INSERT";*/

    HapiContext context = new DefaultHapiContext();

    Parser p = context.getPipeParser();
    //File observationIdentifierTxt = new File("/Users/lydia/Desktop/Uni/6 Semester/BA/Patienten/src/main/resources/observationIdentifier.txt");


    Message hapiMsg;




    //PipeParser ourPipeParser = new PipeParser();

    //Message messageNeu = ourPipeParser.parse(message);

    int anhang = 1;
       
        



    //PID patient = V2Message.getPID();



    public void ausgabe() throws HL7Exception, IOException {

        //ourPipeParser.getParserConfiguration().setAllowUnknownVersions(true);
     
       /* try {
            // The parse method performs the actual parsing
            hapiMsg = p.parse(message);
        } catch (EncodingNotSupportedException e) {
            e.printStackTrace();
            return;
        } catch (HL7Exception e) {
            e.printStackTrace();
            return;
        }*/
        Message message = null;
        for (int i = 0; i< 3; i++){
            InputStream inputStream = new FileInputStream("/Users/lydia/Desktop/Uni/6 Semester/BA/mibi-on-fhir/Code/src/main/resources/MiBi/multiple"+ anhang);
            Hl7InputStreamMessageIterator streamMessageIterator = new Hl7InputStreamMessageIterator(inputStream);
            message = streamMessageIterator.next();
            ORU_R01 oruR01 = (ORU_R01) p.parse(message.encode());
            ORU_R01_ORDER_OBSERVATION orderObservation;
            for (int j = 0; j<6; j++){
                if(!oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j).isEmpty()){
                    orderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j);
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

                    }


                }




            }
            //orderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(0).; // verändern für OBX OBR wenn mehr als 1
            //OBR obr = orderObservation.getOBR();
            //ORU_R01_OBSERVATION observation = orderObservation.getOBSERVATION(0);

            //OBX obx = observation.getOBX();
            //OBR obr = orderObservation.getOBR();
            //ORC orc = orderObservation.getORC();
            //MSH msh = oruR01.getMSH();
            //PID pid = v2Message.get(P);
            //ID[] varies = msh.getCharacterSet();
            //String identifier = obr.getUniversalServiceIdentifier().encode();
            //System.out.println(oruR01.getDSC());


            //Path path = Paths.get("obrMibi.txt");
            //List<String> contentList = Files.readAllLines(path, StandardCharsets.UTF_8);
            //System.out.println(contentList);
            //Writer writer = new BufferedWriter(new FileWriter("obrMibi.txt", true));
            //writer = new FileWriter("observationIdentifier.txt");
/*
            if(varies.length == 1) {

                if (!contentList.contains(varies[0].encode())) {
                    writer.write(varies[0].encode());

                    writer.write("\n" + anhang + "\n");
                    writer.close();
                }

            }
            else if(varies.length > 1) {
                System.out.println("Flag");
            }*/


            anhang+=1;
            System.out.println(i);
        }






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


    }


    

    public V2() throws HL7Exception, IOException {
    }
}

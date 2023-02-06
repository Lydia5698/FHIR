import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v25.datatype.ST;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import org.hl7.fhir.r4.model.*;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class Fhir {
    //V2 v2 = new V2();

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
    public void saveToFhir(ORU_R01 oruR01) throws IOException, HL7Exception {
        ORU_R01_ORDER_OBSERVATION orderObservation;
        OBX obx;
        for (int j = 0; j<5; j++) {
           if (!oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j).isEmpty()) {
               orderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j);
               obx = orderObservation.getOBSERVATION().getOBX(); // get getOBSERVATION(1) etc. für OBX|2|


               FhirContext ctx = FhirContext.forR4();
               // Kulturdiagnostik
               String profile = "https://simplifier.net/resolve?canonical=https://highmed.org/fhir/StructureDefinition/ic/Kulturdiagnostik&scope=MedizininformatikInitiative-HiGHmed-IC@current";
               // Directory
               String profileDirectory = "src/main/resources/Profiles/MII/medizininformatikinitiative-highmed-ic/ressourcen-profile";

               Observation observation = new Observation();
               // Profil der HiGHmed
               observation.getMeta().addProfile("https://simplifier.net/resolve?canonical=https://highmed.org/fhir/StructureDefinition/ic/Kulturdiagnostik&scope=MedizininformatikInitiative-HiGHmed-IC@current");
               // Beispiel für den Status, OBX.11 getObservationResultStatus()
               observation.setStatus(Observation.ObservationStatus.FINAL); // TODO: 04.02.23 Switch Case? Oder Können wir final etc. direkt einsetzten
               // StatusKulturdiagnostik statusKulturdiagnostik = new StatusKulturdiagnostik(observation);
               // OBX.? Value, OBX.6 Unit and OBX.5
               observation.setValue(new Quantity().setValue(123.4).setUnit(obx.getUnits().encode()));
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
               System.out.println(observationValues);
               System.out.println(obx.getObservationIdentifier().encode());
               //observationValue.addCoding().setSystem("Snap2Snomed").setCode().setDisplay(obx.getObservationValue(2).toString());
               observation.addComponent().setValue(observationValue).setCode(new CodeableConcept().setText("Observation value"));
               CodeableConcept code = new CodeableConcept();
               // OBX.3 Observation Identifier todo brauchen wir trotz Observation Value?
               code.addCoding().setSystem("https://loinc.org").setCode("42805-2").setDisplay("Beispiel Nachweis");
               observation.setCode(code);
               CodeableConcept interpretation = new CodeableConcept();
               System.out.println(Arrays.toString(obx.getAbnormalFlags()));
               // OBX.8 Abnormal Flags
               //interpretation.addCoding().setSystem("https://r4.ontoserver.csiro.au/fhir/CodeSystem/v2-0078?_format=application/fhir+json").setCode(obx.getAbnormalFlags(0).getValue()).setDisplay(v2.checkOntoserver("abnormal", "N").get(0).toString());
               //observation.addInterpretation(interpretation);

               // When there are more than one OBX todo kann man ein Value hinzufügen, damit man weiß das wievielte OBX es ist? Vielleicht OBX-4 oder OBX-1?
               // observation.addComponent();


               // Observation Status
               // Observation Code


               IParser parser = ctx.newJsonParser();
               parser.setPrettyPrint(true);

               String encode = parser.encodeResourceToString(observation);
               //System.out.println(encode);

           }
       }


        // todo Validator herunterladen und nutzen?

    }
}

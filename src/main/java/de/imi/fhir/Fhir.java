package de.imi.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.parser.Parser;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.r4.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Fhir {


// TODO: 16.02.23 baccor^Prevotella corporis^keim^5380 5380 lokaler Code
// TODO: 29.03.23 Datum OBX.12 mit einfügen?

    HapiContext context = new DefaultHapiContext();
    Parser p = context.getPipeParser();
    private ConceptMapResultStatus conceptMapResultStatus = new ConceptMapResultStatus("http://localhost:8888/fhir/ConceptMap/1?_format=application/fhir+json");
    private CodeSystemAbnormalFlags codeSystemAbnormalFlags = new CodeSystemAbnormalFlags("http://localhost:8080/fhir/CodeSystem/52/_history/1?_pretty=true");
    private String uri = "http://snomed.info/sct";
    public void start(String startFolder, String outputFilename) throws HL7Exception, IOException  { // TODO umbauen um den startfile zu finden
        String directoryName = "ObservationDirectory"; // TODO directory ersetzen
        Path path = Path.of("src/main/resources/MiBi" +"/multiple868");
        String hl7String = Files.readString(path, StandardCharsets.ISO_8859_1);
        Message message = p.parse(hl7String);
        ORU_R01 oruR01 = (ORU_R01) p.parse(message.encode());
        saveToFhir(oruR01, "src/main/resources/outputs/"+ directoryName);
    }
    public void saveToFhir(ORU_R01 oruR01, String path) throws HL7Exception {
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
            String fileName = "observationMember";
            if (orderObservation.getOBSERVATION(0).getOBX().getObservationIdentifier().getNameOfCodingSystem().encode().contains("abio")){
                fileName = "antibiogramm";
            }
            File childFile = new File(path + "/"+ fileName + orderObservationCount + ".json");
            Writer writer = null;
            try {
                Observation childObservation;

                // Profil der HiGHmed
                // Beispiel für den Status, OBX.11 getObservationResultStatus()
                childObservation = castToFhir(orderObservation);

                //System.out.println(parser.encodeResourceToString(childObservation));
                // String encode = parser.encodeResourceToString(childObservation);

                writer = new OutputStreamWriter(new FileOutputStream(childFile), StandardCharsets.ISO_8859_1);
                writer.write(parser.encodeResourceToString(childObservation));

                Reference parentReference = new Reference();
                parentReference.setReference(path + "/"+ fileName + orderObservationCount + ".json");
                parentObservation.addHasMember(parentReference);

                orderObservationCount++;
                orderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(orderObservationCount);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                try {
                    if (writer != null){
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Get the file
        File parentFile = new File(path+"/observation.json");
        Writer parentWriter = null;

        try {
            parentWriter = new OutputStreamWriter(new FileOutputStream(parentFile), StandardCharsets.ISO_8859_1);
            parentWriter.write(parser.encodeResourceToString(parentObservation));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                if (parentWriter != null){
                    parentWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // todo Validator mit HTTP Put an den Marshal
        // http://localhost:8888/fhir/ConceptMap/1?_format=application/fhir+json


    }


    public Observation castToFhir(ORU_R01_ORDER_OBSERVATION orderObservation) throws HL7Exception, IOException {
        Observation childObservation = new Observation();
        ConceptMapHandler conceptMapHandler = new ConceptMapHandler();
        int rowCount = 0;
        // Verschiedene unter Observations also OBX|2, OBX|3 ...
        OBX obx = orderObservation.getOBSERVATION(rowCount).getOBX();
        while(!obx.isEmpty()) {
            // Profil der HiGHmed
            // Beispiel für den Status, OBX.11 getObservationResultStatus()
            childObservation.addComponent();
            ID status = obx.getObx11_ObservationResultStatus();
            Observation.ObservationStatus observationStatus = conceptMapResultStatus.getObservationStatusStatusFor(status.getValue());
            childObservation.setStatus(observationStatus);

            String sourceCode = obx.getObservationValue(0).encode().split("\\^")[0];
            String observationValues = obx.getObservationValue(0).encode();
            ConceptMap conceptMapObservationValues = conceptMapHandler.getRightConceptMap(observationValues);
            CodeableConcept observationValuesAndUnits = new CodeableConcept();
            if(conceptMapObservationValues != null){
                observationValuesAndUnits.addCoding().setSystem("Observation Values").setCode(conceptMapObservationValues.getTargetCode(sourceCode)).setDisplay(conceptMapObservationValues.getTargetDisplay(sourceCode)).setSystem(uri);
            }
            if (obx.getObx3_ObservationIdentifier().getNameOfCodingSystem().encode().contains("abio")){
                observationValuesAndUnits.addCoding().setSystem("Units").setDisplay(obx.getObservationValue(0).encode() + " " + obx.getUnits().encode()); // Units OBX.6
            }
            else {
                observationValuesAndUnits.addCoding().setSystem("Units").setDisplay(obx.getUnits().encode()); // Units OBX.6
            }
            childObservation.getComponent().get(rowCount).setValue(observationValuesAndUnits); // OBX 5 Observation Value and Units OBX.6

            CodeableConcept interpretation = new CodeableConcept();
            String[] stringArray = new String[obx.getAbnormalFlags().length];
            for (int i = 0; i < obx.getAbnormalFlags().length; i++) {
                stringArray[i] = obx.getAbnormalFlags(i).encode();
            }
            // OBX.8 Abnormal Flags "https://r4.ontoserver.csiro.au/fhir/CodeSystem/v2-0078?_format=application/fhir+json"
            if (!ArrayUtils.isEmpty(stringArray)) {
                interpretation.addCoding().setSystem("Abnormal Flags").setCode(stringArray[0]).setDisplay(codeSystemAbnormalFlags.getAbnormalFlagFor(stringArray[0]));
            }
            childObservation.getComponent().get(rowCount).addInterpretation(interpretation);

            ConceptMap conceptMapObservationIdentifier = conceptMapHandler.getRightConceptMap(obx.getObx3_ObservationIdentifier().getNameOfCodingSystem().encode());
            if (conceptMapObservationIdentifier != null){
                CodeableConcept observationIdentifier = new CodeableConcept();
                observationIdentifier.addCoding().setSystem("Observation Identifier").setCode(conceptMapObservationIdentifier.getTargetCode(obx.getObx3_ObservationIdentifier().getIdentifier().encode())).setDisplay(conceptMapObservationIdentifier.getTargetDisplay(obx.getObx3_ObservationIdentifier().getIdentifier().encode())).setSystem(uri); // Observation Identifier OBX.3
                childObservation.getComponent().get(rowCount).setCode(observationIdentifier); // OBX.3
            }

            rowCount++;
            obx = orderObservation.getOBSERVATION(rowCount).getOBX();
        }
        return childObservation;

    }
}

package de.imi.fhir.ressourcen;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import de.imi.fhir.CodeSystemAbnormalFlags;
import de.imi.fhir.conceptMap.ConceptMap;
import de.imi.fhir.conceptMap.ConceptMapHandler;
import de.imi.fhir.conceptMap.ConceptMapResultStatus;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Observation;

public class KulturDiagnostik { //Kultur -> Antibiogramm -> MRGN oder MRE (meist neue Datei)
    private String uri = "http://snomed.info/sct";
    private ConceptMapResultStatus conceptMapResultStatus = new ConceptMapResultStatus("http://localhost:8888/fhir/ConceptMap/1?_format=application/fhir+json");
    private CodeSystemAbnormalFlags codeSystemAbnormalFlags = new CodeSystemAbnormalFlags("http://localhost:8888/fhir/CodeSystem/52?_format=application/fhir+json");
    public void Antibiogramm(OBX obx) throws HL7Exception {
        // identifier -> analyseBefundCode -> type -> coding -> observationInstance V2 -> system uri code, sytem uri, Value
        // category -> coding -> lonic-observation (code), observation-category (laboratory), lonic-microbiology-studies
        // Code OBX-3
        // status OBX-11
        // subject MII-Reference
        // effective datum OBX-14
        // ? Value OBX-5 OBX-6
        Observation antibiogramm = new Observation();
        ConceptMapHandler conceptMapHandler = null;
        ConceptMap conceptMapObservationIdentifier = conceptMapHandler.getRightConceptMap(obx.getObx3_ObservationIdentifier().encode());
        if (conceptMapObservationIdentifier != null){
            CodeableConcept observationIdentifier = new CodeableConcept();
            observationIdentifier.addCoding().setSystem("Observation Identifier").setCode(conceptMapObservationIdentifier.getTargetCode(obx.getObx3_ObservationIdentifier().getIdentifier().encode())).setDisplay(conceptMapObservationIdentifier.getTargetDisplay(obx.getObx3_ObservationIdentifier().getIdentifier().encode())).setSystem(uri); // Observation Identifier OBX.3
            antibiogramm.setCode(observationIdentifier); // OBX.3
        }
        Extension extension = new Extension("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum");
        antibiogramm.addExtension(extension);
        Coding coding = new Coding();
        coding.setCode(obx.getObx14_DateTimeOfTheObservation().encode()).setSystem("?System Date Time");
        antibiogramm.getEffectiveDateTimeType().getExtensionByUrl("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum").setValue(coding);

        CodeableConcept interpretation = new CodeableConcept();
        String[] stringArray = new String[obx.getObx8_AbnormalFlags().length];
        for (int i = 0; i < obx.getObx8_AbnormalFlags().length; i++) {
            stringArray[i] = obx.getObx8_AbnormalFlags(i).encode();
        }
        // OBX.8 Abnormal Flags "https://r4.ontoserver.csiro.au/fhir/CodeSystem/v2-0078?_format=application/fhir+json"
        if (!ArrayUtils.isEmpty(stringArray)) {
            interpretation.addCoding().setSystem("Abnormal Flags").setCode(stringArray[0]).setDisplay(codeSystemAbnormalFlags.getAbnormalFlagFor(stringArray[0]));
        }
        antibiogramm.addInterpretation(interpretation);

        String sourceCode = obx.getObx5_ObservationValue(0).encode().split("\\^")[0];
        String observationValues = obx.getObx5_ObservationValue(0).encode();
        ConceptMap conceptMapObservationValues = conceptMapHandler.getRightConceptMap(observationValues);
        CodeableConcept observationValuesAndUnits = new CodeableConcept();
        if(conceptMapObservationValues != null){
            observationValuesAndUnits.addCoding().setSystem("Observation Values").setCode(conceptMapObservationValues.getTargetCode(sourceCode)).setDisplay(conceptMapObservationValues.getTargetDisplay(sourceCode)).setSystem(uri);
        }
        observationValuesAndUnits.addCoding().setSystem("Units").setDisplay(obx.getObservationValue(0).encode() + " " + obx.getUnits().encode()); // Units OBX.6
        //TODO falls Units leer
        antibiogramm.setValue(observationValuesAndUnits); // OBX 5 Observation Value and Units OBX.6

        ID status = obx.getObx11_ObservationResultStatus();
        Observation.ObservationStatus observationStatus = conceptMapResultStatus.getObservationStatusStatusFor(status.getValue());
        antibiogramm.setStatus(observationStatus);


    }

    // Empfindlichkeit 868
    // identifier -> analyseBefundCode -> type -> coding -> observationInstance V2 -> system uri code, sytem uri, Value, assigner Reference
    // category -> coding -> lonic-observation (code), observation-category (laboratory), lonic-microbiology-studies
    // Code OBX-3
    // status OBX-11
    // subject MII-Reference
    // effective datum OBX-14
    // interpretation OBX-8 EUCAST oder CLSI
    // ? Value OBX-5 OBX-6


    // MRE
    // identifier -> analyseBefundCode -> type -> coding -> observationInstance V2 -> system uri code, sytem uri, Value, assigner Reference
    // category -> coding -> lonic-observation (code), observation-category (laboratory), lonic-microbiology-studies
    // status OBX-11
    // code OBX-3 -> coding
    // subject MII-Reference
    // effective datum OBX-14

    // MRGN 678
    // identifier -> analyseBefundCode -> type -> coding -> observationInstance V2 -> system uri code, sytem uri, Value, assigner Reference
    // category -> coding -> lonic-observation (code), observation-category (laboratory), lonic-microbiology-studies
    // status OBX-11
    // code OBX-3 -> coding
    // subject MII-Reference
    // effective datum OBX-14


}

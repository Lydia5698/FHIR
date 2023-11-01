package de.imi.fhir.ressourcen;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import de.imi.fhir.conceptMap.*;
import de.imi.fhir.conceptMap.ConceptMap;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.r4.model.*;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KulturDiagnostik { //Kultur -> Antibiogramm -> MRGN oder MRE (meist neue Datei)
    private ConceptMapResultStatus conceptMapResultStatus = new ConceptMapResultStatus("http://localhost:8888/fhir/ConceptMap/1707?_format=application/fhir+json");
    private CodeSystemEUCAST codeSystemEUCAST = new CodeSystemEUCAST("http://localhost:8888/fhir/CodeSystem/3302?_format=application/fhir+json");
    private ConceptMapMRGN conceptMapMRGN = new ConceptMapMRGN("http://localhost:8888/fhir/ConceptMap/3303?_format=application/fhir+json");
    private MainRessource mainRessource = new MainRessource();
    ConceptMapHandler conceptMapHandler = new ConceptMapHandler();

    public Observation kulturNachweis(OBX obx, OBR obr) throws HL7Exception {
        // identifier -> analyseBefundCode -> type -> coding -> observationInstance V2 -> system uri code, sytem uri, Value
        // category -> coding -> lonic-observation (code), observation-category (laboratory), lonic-microbiology-studies
        // Code OBX-3
        // status OBX-11
        // subject MII-Reference
        // effective datum OBX-14
        // ? Value OBX-5 OBX-6
        Observation kulturNachweis = new Observation();
        kulturNachweis.setId(UUID.randomUUID().toString());
        kulturNachweis.setMeta(mainRessource.setMetaData("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/StructureDefinition/kultur-nachweis"));

        kulturNachweis.addIdentifier(mainRessource.getAnalyseBefundCode(obx,obr));
        ID status = obx.getObx11_ObservationResultStatus();
        Observation.ObservationStatus observationStatus = conceptMapResultStatus.getObservationStatusStatusFor(status.getValue());
        kulturNachweis.setStatus(observationStatus);
        kulturNachweis.addCategory(mainRessource.getCategory());

        CodeableConcept code = new CodeableConcept();
        code.addCoding().setCode(obx.getObx3_ObservationIdentifier().encode()).setSystem("http://hl7.org/fhir/ValueSet/observation-codes");
        kulturNachweis.setCode(code); //OBX-3 // TODO: 04.07.23 System nicht ganz richtig noch mappen Kann man nicht richtig mappen?

        //kulturNachweis.setSubject();
        if (!obx.getObx14_DateTimeOfTheObservation().isEmpty()) {
            kulturNachweis.getEffectiveDateTimeType().addExtension(new Extension("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum").setValue(mainRessource.getEffectiveDateTime(obx)));
        }
        kulturNachweis.addInterpretation(mainRessource.getAbnormalFlag(obx));

        CodeableConcept valueCodeableConcept = new CodeableConcept();
        String observationValues = obx.getObx5_ObservationValue(0).encode();
        ConceptMapHandler conceptMapHandler = new ConceptMapHandler();
        if (conceptMapHandler.getRightConceptMap(observationValues) != null){
            ConceptMap conceptMapObservationValues = conceptMapHandler.getRightConceptMap(observationValues);
            String sourceCode = obx.getObx5_ObservationValue(0).encode().split("\\^")[0];
            valueCodeableConcept.addCoding().setSystem("http://snomed.info/sct").setCode(conceptMapObservationValues.getTargetCode(sourceCode)).setDisplay(conceptMapObservationValues.getTargetDisplay(sourceCode));
            kulturNachweis.setValue(valueCodeableConcept);
        }
        else {
            Annotation note = new Annotation();
            note.setText(observationValues);
            kulturNachweis.addNote(note);
            kulturNachweis.setDataAbsentReason(mainRessource.getDataAbsentreason());
        }

        Reference patient = new Reference("src/main/resources/dummyPatient");
        kulturNachweis.setSubject(patient);

        //kulturNachweis.setMethod(); // TODO: 18.07.23 wie sollte man das Unterscheiden? Haben nur die Art

        return kulturNachweis;
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

    /**
     * This method gets one line of the message and returns the observation associated with it. This observation is filled with the elements from the line.
     * @param obx OBX Segment of the current Message
     * @param obr OBR Segment of the current Message
     * @return Observation: the observation for the current line of the message
     */
    public Observation fillEmpfindlichkeit(OBX obx, OBR obr) throws HL7Exception { // Antibiogramm
        /**
        * Generates random UUID and sets the Metadata to the Observation
         */
        Observation empfindlichkeit = new Observation();
        empfindlichkeit.setId(UUID.randomUUID().toString());
        empfindlichkeit.setMeta(mainRessource.setMetaData("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/StructureDefinition/empfindlichkeit"));

        /**
         *  The identifier is stored in the observation. This is composed of OBR-3, OBX-3, OBX-4.
         */
        empfindlichkeit.addIdentifier(mainRessource.getAnalyseBefundCode(obx,obr)); // OBX-21

        /**
         *  Here the observation is filled with the category
         */
        empfindlichkeit.addCategory(mainRessource.getCategory());

        /**
         * Here the observation is filled with the code from the OBX-3 element. In the case of an antibiogram, the antibiotics to be tested for resistance are stored here.
         */
        CodeableConcept code = new CodeableConcept();
        String sourceCode = obx.getObx3_ObservationIdentifier().getCe1_Identifier().encode();
        ConceptMap conceptMapObservationIdentifier = conceptMapHandler.getRightConceptMap(obx.getObx3_ObservationIdentifier().getCe3_NameOfCodingSystem().encode());
        if (conceptMapObservationIdentifier != null){
            code.addCoding().setCode(conceptMapObservationIdentifier.getTargetCode(sourceCode)).setDisplay(conceptMapObservationIdentifier.getTargetDisplay(sourceCode)).setSystem("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/highmed-uksh-ucic-hl7-mibibefund-antiinfektiva-lokal-loinc");
        }
        empfindlichkeit.setCode(code); //OBX-3

        /**
         * Here the status is filled with the OBX-11 element
         */
        ID status = obx.getObx11_ObservationResultStatus();
        Observation.ObservationStatus observationStatus = conceptMapResultStatus.getObservationStatusStatusFor(status.getValue());
        empfindlichkeit.setStatus(observationStatus);

        /**
         *  The date and time of the observation from the OBX-14 element is stored here.
         */
        if (!obx.getObx14_DateTimeOfTheObservation().isEmpty()) {
            empfindlichkeit.getEffectiveDateTimeType().addExtension(new Extension("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum").setValue(mainRessource.getEffectiveDateTime(obx)));
        }

        /**
         * The abnormal flag from the OBX-8 element is stored here. This flag indicates a correction of the observed value.
         */
        CodeableConcept eucast = new CodeableConcept();
        String[] stringArray = new String[obx.getObx8_AbnormalFlags().length];
        for (int i = 0; i < obx.getObx8_AbnormalFlags().length; i++) {
            stringArray[i] = obx.getObx8_AbnormalFlags(i).encode();
        }
        if (!ArrayUtils.isEmpty(stringArray)) {
            eucast.addCoding().setSystem("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/CodeSystem/mii-vs-mikrobio-eucast-eucast").setCode(stringArray[0]).setDisplay(codeSystemEUCAST.getEUCAST(stringArray[0]));
        }
        empfindlichkeit.addInterpretation(eucast);

        /**
         * The observation value from the OBX-5 is stored here. In this case, the numerical value of the antibiogram is stored here.
         */
        if (!obx.getObx5_ObservationValue(0).isEmpty()) {
            Quantity valueQuantity = new Quantity();
            String[] observationValues = obx.getObservationValue(0).encode().split("(?<=\\D)(?=\\d)");
            int value;
            if (observationValues.length < 2) {
                value = Integer.parseInt(observationValues[0]);
                valueQuantity.setValue(value).setUnit(obx.getObx6_Units().encode());
            } else {
                value = Integer.parseInt(observationValues[1]);
                String comparator = observationValues[0];
                Quantity.QuantityComparatorEnumFactory quantityComparatorEnumFactory = new Quantity.QuantityComparatorEnumFactory();
                valueQuantity.setValue(value).setComparator(quantityComparatorEnumFactory.fromCode(comparator)).setUnit(obx.getObx6_Units().encode());
            }
            empfindlichkeit.setValue(valueQuantity);
        }
        else {
            empfindlichkeit.setDataAbsentReason(mainRessource.getDataAbsentreason());
        }

        /**
         * Here the patient is referenced to the observation
         */
        Reference patient = new Reference("src/main/resources/dummyPatient");
        empfindlichkeit.setSubject(patient);

        return empfindlichkeit;

        //empfindlichkeit.setSubject();
    }




    // MRE
    // identifier -> analyseBefundCode -> type -> coding -> observationInstance V2 -> system uri code, sytem uri, Value, assigner Reference
    // category -> coding -> lonic-observation (code), observation-category (laboratory), lonic-microbiology-studies
    // status OBX-11
    // code OBX-3 -> coding
    // subject MII-Reference
    // effective datum OBX-14

    public Observation fillMRE(OBX obx, OBR obr) throws HL7Exception {
        Observation mre = new Observation();
        mre.setId(UUID.randomUUID().toString());
        mre.setMeta(mainRessource.setMetaData("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/StructureDefinition/mre-klasse"));

        mre.addIdentifier(mainRessource.getAnalyseBefundCode(obx,obr)); // OBX-21
        // option: 1) OBR-3 + OBX-3 + OBX-4 or 2) OBR-3 + OBR-4 + OBX-3 + OBX-4 or 2) some other way to uniquely ID the OBR/ORC + OBX-3 + OBX-4.

        mre.addCategory(mainRessource.getCategory());

        CodeableConcept code = new CodeableConcept();
        code.addCoding().setCode(obx.getObx3_ObservationIdentifier().encode()).setSystem("http://hl7.org/fhir/ValueSet/observation-codes");
        mre.setCode(code); //OBX-3
        ID status = obx.getObx11_ObservationResultStatus();
        Observation.ObservationStatus observationStatus = conceptMapResultStatus.getObservationStatusStatusFor(status.getValue());
        mre.setStatus(observationStatus);

        mre.addInterpretation(mainRessource.getAbnormalFlag(obx));

        CodeableConcept valueCodeableConcept = new CodeableConcept();
        String observationValues = obx.getObservationValue(0).encode();
        if (conceptMapHandler.getRightConceptMap(observationValues) != null){
            ConceptMap conceptMapObservationValues = conceptMapHandler.getRightConceptMap(observationValues);
            String sourceCodeObservationValues = obx.getObservationValue(0).encode().split("\\^")[0];
            valueCodeableConcept.addCoding().setSystem("http://snomed.info/sct").setCode(conceptMapObservationValues.getTargetCode(sourceCodeObservationValues)).setDisplay(conceptMapObservationValues.getTargetDisplay(sourceCodeObservationValues));
            mre.setValue(valueCodeableConcept);
        }
        else {
            Annotation note = new Annotation();
            note.setText(observationValues);
            mre.addNote(note);
            mre.setDataAbsentReason(mainRessource.getDataAbsentreason());
        }

        if (!obx.getObx14_DateTimeOfTheObservation().isEmpty()) {
            mre.getEffectiveDateTimeType().addExtension(new Extension("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum").setValue(mainRessource.getEffectiveDateTime(obx)));
        }

        Reference patient = new Reference("src/main/resources/dummyPatient");
        mre.setSubject(patient);

        return mre;
    }

    // MRGN 678
    // identifier -> analyseBefundCode -> type -> coding -> observationInstance V2 -> system uri code, sytem uri, Value, assigner Reference
    // category -> coding -> lonic-observation (code), observation-category (laboratory), lonic-microbiology-studies
    // status OBX-11
    // code OBX-3 -> coding
    // subject MII-Reference
    // effective datum OBX-14

    public Observation fillMRGN(OBX obx, OBR obr) throws HL7Exception {
        Observation mrgn = new Observation();
        mrgn.setId(UUID.randomUUID().toString());
        mrgn.setMeta(mainRessource.setMetaData("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/StructureDefinition/mrgn-klasse"));

        mrgn.addIdentifier(mainRessource.getAnalyseBefundCode(obx,obr)); // OBX-21
        // option: 1) OBR-3 + OBX-3 + OBX-4 or 2) OBR-3 + OBR-4 + OBX-3 + OBX-4 or 2) some other way to uniquely ID the OBR/ORC + OBX-3 + OBX-4.

        mrgn.addCategory(mainRessource.getCategory());

        CodeableConcept code = new CodeableConcept();
        code.addCoding().setCode(obx.getObx3_ObservationIdentifier().encode()).setSystem("http://hl7.org/fhir/ValueSet/observation-codes");
        mrgn.setCode(code); //OBX-3

        mrgn.addInterpretation(mainRessource.getAbnormalFlag(obx));

        ID status = obx.getObx11_ObservationResultStatus();
        Observation.ObservationStatus observationStatus = conceptMapResultStatus.getObservationStatusStatusFor(status.getValue());
        mrgn.setStatus(observationStatus);

        CodeableConcept valueCodeableConcept = new CodeableConcept();
        String observationValues = obx.getObx5_ObservationValue(0).encode();
        if (conceptMapHandler.getRightConceptMap(observationValues) != null){
            ConceptMap conceptMapObservationValues = conceptMapHandler.getRightConceptMap(observationValues);
            String sourceCodeObservationValues = obx.getObservationValue(0).encode().split("\\^")[0];
            valueCodeableConcept.addCoding().setSystem("http://snomed.info/sct").setCode(conceptMapObservationValues.getTargetCode(sourceCodeObservationValues)).setDisplay(conceptMapObservationValues.getTargetDisplay(sourceCodeObservationValues));
            String mrgnKlasse = null;
            Pattern p = Pattern.compile(".*\\((.*)\\).*");
            Matcher m = p.matcher(obx.getObservationValue(0).encode().split("\\^")[1]);
            if(m.find()){
                mrgnKlasse = m.group(1);
            }
            valueCodeableConcept.addCoding().setSystem("http://loinc.org").setCode(conceptMapMRGN.getTargetCode(mrgnKlasse)).setDisplay(conceptMapMRGN.getTargetDisplay(mrgnKlasse));
            mrgn.setValue(valueCodeableConcept);
        }
        else {
            Annotation note = new Annotation();
            note.setText(observationValues);
            mrgn.addNote(note);
            mrgn.setDataAbsentReason(mainRessource.getDataAbsentreason());
        }

        if (!obx.getObx14_DateTimeOfTheObservation().isEmpty()) {
            mrgn.getEffectiveDateTimeType().addExtension(new Extension("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum").setValue(mainRessource.getEffectiveDateTime(obx)));
        }

        Reference patient = new Reference("src/main/resources/dummyPatient");
        mrgn.setSubject(patient);

        return mrgn;
    }
}

package de.imi.fhir.ressourcen;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import de.imi.fhir.conceptMap.ConceptMap;
import de.imi.fhir.conceptMap.ConceptMapHandler;
import de.imi.fhir.conceptMap.ConceptMapResultStatus;
import org.hl7.fhir.r4.model.*;

import java.util.UUID;

public class KulturDiagnostik { //Kultur -> Antibiogramm -> MRGN oder MRE (meist neue Datei)
    private ConceptMapResultStatus conceptMapResultStatus = new ConceptMapResultStatus("http://localhost:8888/fhir/ConceptMap/1707?_format=application/fhir+json");
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
        String observationValues = obx.getObservationValue(0).encode();
        ConceptMapHandler conceptMapHandler = new ConceptMapHandler();
        if (conceptMapHandler.getRightConceptMap(observationValues) != null){
            ConceptMap conceptMapObservationValues = conceptMapHandler.getRightConceptMap(observationValues);
            String sourceCode = obx.getObservationValue(0).encode().split("\\^")[0];
            valueCodeableConcept.addCoding().setSystem("http://snomed.info/sct").setCode(conceptMapObservationValues.getTargetCode(sourceCode)).setDisplay(conceptMapObservationValues.getTargetDisplay(sourceCode));
            kulturNachweis.setValue(valueCodeableConcept);
        }
        else {
            Annotation note = new Annotation();
            note.setText(observationValues);
            kulturNachweis.addNote(note);
        }

        Reference patient = new Reference("src/main/resources/dummyPatient");
        kulturNachweis.setSubject(patient);
        
        //kulturNachweis.setDataAbsentReason(); // TODO: 18.07.23 nicht umsetzbar, da Grund im Freitext steht
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

    public Observation fillEmpfindlichkeit(OBX obx, OBR obr) throws HL7Exception { // Antibiogramm
        Observation empfindlichkeit = new Observation();
        empfindlichkeit.setId(UUID.randomUUID().toString());
        empfindlichkeit.setMeta(mainRessource.setMetaData("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/StructureDefinition/empfindlichkeit"));

        empfindlichkeit.addIdentifier(mainRessource.getAnalyseBefundCode(obx,obr)); // OBX-21
        // option: 1) OBR-3 + OBX-3 + OBX-4 or 2) OBR-3 + OBR-4 + OBX-3 + OBX-4 or 2) some other way to uniquely ID the OBR/ORC + OBX-3 + OBX-4.

        empfindlichkeit.addCategory(mainRessource.getCategory());

        CodeableConcept code = new CodeableConcept();
        String sourceCode = obx.getObx3_ObservationIdentifier().getCe1_Identifier().encode();
        ConceptMap conceptMapObservationIdentifier = conceptMapHandler.getRightConceptMap(obx.getObx3_ObservationIdentifier().getCe3_NameOfCodingSystem().encode());
        if (conceptMapObservationIdentifier != null){
            code.addCoding().setCode(conceptMapObservationIdentifier.getTargetCode(sourceCode)).setDisplay(conceptMapObservationIdentifier.getTargetDisplay(sourceCode)).setSystem("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/highmed-uksh-ucic-hl7-mibibefund-antiinfektiva-lokal-loinc");
        }
        empfindlichkeit.setCode(code); //OBX-3

        ID status = obx.getObx11_ObservationResultStatus();
        Observation.ObservationStatus observationStatus = conceptMapResultStatus.getObservationStatusStatusFor(status.getValue());
        empfindlichkeit.setStatus(observationStatus);

        if (!obx.getObx14_DateTimeOfTheObservation().isEmpty()) {
            empfindlichkeit.getEffectiveDateTimeType().addExtension(new Extension("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum").setValue(mainRessource.getEffectiveDateTime(obx)));
        }

        CodeableConcept interpretation = new CodeableConcept();
        Coding eucast = new Coding();
        CodeableConcept clsi = new CodeableConcept();
        eucast.setVersion("").setCode(obx.getObx8_AbnormalFlags(0).encode());  // OBX-8 Abnormal Flags
        clsi.addCoding(new Coding().setVersion("").setCode(obx.getObx8_AbnormalFlags(0).encode()));
        interpretation.addCoding(eucast); // oder clsi
        empfindlichkeit.addInterpretation(interpretation);

        CodeableConcept valueCodeableConcept = new CodeableConcept();
        String observationValues = obx.getObservationValue(0).encode();
        if (conceptMapHandler.getRightConceptMap(observationValues) != null){
            ConceptMap conceptMapObservationValues = conceptMapHandler.getRightConceptMap(observationValues);
            String sourceCodeObservationValues = obx.getObservationValue(0).encode().split("\\^")[0];
            valueCodeableConcept.addCoding().setSystem("http://snomed.info/sct").setCode(conceptMapObservationValues.getTargetCode(sourceCodeObservationValues)).setDisplay(conceptMapObservationValues.getTargetDisplay(sourceCodeObservationValues));
            empfindlichkeit.setValue(valueCodeableConcept);
        }
        else {
            Annotation note = new Annotation();
            note.setText(observationValues);
            empfindlichkeit.addNote(note);
        }

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
        mre.setCode(code); //OBX-3 // TODO: 04.07.23 System nicht ganz richtig noch mappen

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
        mrgn.setCode(code); //OBX-3 // TODO: 04.07.23 System nicht ganz richtig noch mappen

        mrgn.addInterpretation(mainRessource.getAbnormalFlag(obx));

        ID status = obx.getObx11_ObservationResultStatus();
        Observation.ObservationStatus observationStatus = conceptMapResultStatus.getObservationStatusStatusFor(status.getValue());
        mrgn.setStatus(observationStatus);

        CodeableConcept valueCodeableConcept = new CodeableConcept();
        String observationValues = obx.getObservationValue(0).encode();
        if (conceptMapHandler.getRightConceptMap(observationValues) != null){
            ConceptMap conceptMapObservationValues = conceptMapHandler.getRightConceptMap(observationValues);
            String sourceCodeObservationValues = obx.getObservationValue(0).encode().split("\\^")[0];
            valueCodeableConcept.addCoding().setSystem("http://snomed.info/sct").setCode(conceptMapObservationValues.getTargetCode(sourceCodeObservationValues)).setDisplay(conceptMapObservationValues.getTargetDisplay(sourceCodeObservationValues));
            mrgn.setValue(valueCodeableConcept);
        }
        else {
            Annotation note = new Annotation();
            note.setText(observationValues);
            mrgn.addNote(note);
        }

        if (!obx.getObx14_DateTimeOfTheObservation().isEmpty()) {
            mrgn.getEffectiveDateTimeType().addExtension(new Extension("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum").setValue(mainRessource.getEffectiveDateTime(obx)));
        }

        Reference patient = new Reference("src/main/resources/dummyPatient");
        mrgn.setSubject(patient);

        return mrgn;
    }







}

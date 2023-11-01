package de.imi.fhir.ressourcen;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import de.imi.fhir.conceptMap.*;
import de.imi.fhir.conceptMap.ConceptMap;
import org.hl7.fhir.r4.model.*;

import java.util.UUID;

public class MolekularDiagnostik { // Corona
    // OBX-3 cov2019so^SARS-CoV-2 RNA (Sonst.Mat.1)^^603308
    // OBX-4 0
    // OBX-5 !fehlt
    // OBX-7 negativ
    // OBX-8 Abnormal Flags
    // OBX-11 I
    // OBX-14 Zeit (nur bei Schnelltests?)

    private ConceptMapResultStatus conceptMapResultStatus = new ConceptMapResultStatus("http://localhost:8888/fhir/ConceptMap/1707?_format=application/fhir+json");
    private ConceptMapMikrobioPositiveNegative conceptMapMikrobioPositiveNegative = new ConceptMapMikrobioPositiveNegative("http://localhost:8888/fhir/ConceptMap/1?_format=json&_pretty=true");
    private MainRessource mainRessource = new MainRessource();

    public Observation fillMolekularDiagnostik(OBX obx, OBR obr) throws HL7Exception {
        Observation molekularDiagnostik = new Observation();
        molekularDiagnostik.setId(UUID.randomUUID().toString());
        molekularDiagnostik.setMeta(mainRessource.setMetaData("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/StructureDefinition/molekular-diagnostik"));

        molekularDiagnostik.addIdentifier(mainRessource.getAnalyseBefundCode(obx,obr));
        // OBX-21 option: 1) OBR-3 + OBX-3 + OBX-4 or 2) OBR-3 + OBR-4 + OBX-3 + OBX-4 or 2) some other way to uniquely ID the OBR/ORC + OBX-3 + OBX-4.

        ID status = obx.getObx11_ObservationResultStatus();
        Observation.ObservationStatus observationStatus = conceptMapResultStatus.getObservationStatusStatusFor(status.getValue());
        molekularDiagnostik.setStatus(observationStatus);

        molekularDiagnostik.addCategory(mainRessource.getCategory());

        CodeableConcept code = new CodeableConcept();
        ConceptMapHandler conceptMapHandler = new ConceptMapHandler();
        if (obx.getObx3_ObservationIdentifier() != null){
            String sourceCode = obx.getObx3_ObservationIdentifier().getCe1_Identifier().encode();// Bsp: cov2019so
            ConceptMap conceptMapObservationIdentifier = conceptMapHandler.getRightConceptMap(sourceCode);
            code.addCoding().setSystem("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/uksh-medic-ucic-virologischer-befund-virusnachweistest").setDisplay(conceptMapObservationIdentifier.getTargetDisplay(sourceCode)).setCode(conceptMapObservationIdentifier.getTargetCode(sourceCode));
        }
        molekularDiagnostik.setCode(code); //OBX-3 cov2019so^SARS-CoV-2 RNA (Sonst.Mat.1)^^603308

        //molekularDiagnostik.setSubject(); // PID-3

        if (!obx.getObx14_DateTimeOfTheObservation().isEmpty()) {
            molekularDiagnostik.getEffectiveDateTimeType().addExtension(new Extension("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum").setValue(mainRessource.getEffectiveDateTime(obx)));
        }
        //OBX-14

        if (!obx.getObx5_ObservationValue(0).encode().isEmpty()){
            CodeableConcept valueCodeableConcept = new CodeableConcept();
            valueCodeableConcept.addCoding().setSystem("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/ValueSet/mii-vs-mikrobio-positiv-negativ-snomedct").setCode(conceptMapMikrobioPositiveNegative.getTargetCode(obx.getObx5_ObservationValue(0).encode())).setDisplay(conceptMapMikrobioPositiveNegative.getTargetDisplay(obx.getObx5_ObservationValue(0).encode()));//
            molekularDiagnostik.setValue(valueCodeableConcept);
        }
        else {
            molekularDiagnostik.setDataAbsentReason(mainRessource.getDataAbsentreason());
        }


        molekularDiagnostik.addInterpretation(mainRessource.getAbnormalFlag(obx));

        molekularDiagnostik.addReferenceRange().setText(obx.getObx7_ReferencesRange().encode());

        Reference specimen = new Reference();
        specimen.setReference("src/main/resources/outputs/specimen");
        molekularDiagnostik.setSpecimen(specimen);


        return molekularDiagnostik;

    }
}

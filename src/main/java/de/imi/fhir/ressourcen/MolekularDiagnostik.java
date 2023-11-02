package de.imi.fhir.ressourcen;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import de.imi.fhir.conceptMap.*;
import de.imi.fhir.conceptMap.ConceptMap;
import org.hl7.fhir.r4.model.*;

import java.util.UUID;

/**
 * In this class are the methods for Molekular Diagnostic
 */
public class MolekularDiagnostik {
    private final ConceptMapResultStatus conceptMapResultStatus = new ConceptMapResultStatus("http://localhost:8888/fhir/ConceptMap/1707?_format=application/fhir+json");
    private final ConceptMapMikrobioPositiveNegative conceptMapMikrobioPositiveNegative = new ConceptMapMikrobioPositiveNegative("http://localhost:8888/fhir/ConceptMap/1?_format=json&_pretty=true");
    private final MainRessource mainRessource = new MainRessource();

    /**
     * @param obx  OBX Segment of the current Message
     * @param obr  OBR Segment of the current Message
     * @return Observation: the observation for the current line of the message
     */
    public Observation fillMolekularDiagnostik(OBX obx, OBR obr) throws HL7Exception {

        // Generates random UUID and sets the Metadata to the Observation
        Observation molekularDiagnostik = new Observation();
        molekularDiagnostik.setId(UUID.randomUUID().toString());
        molekularDiagnostik.setMeta(mainRessource.setMetaData("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/StructureDefinition/molekular-diagnostik"));

        // The identifier is stored in the observation. This is composed of OBR-3, OBX-3, OBX-4.
        molekularDiagnostik.addIdentifier(mainRessource.getAnalyseBefundCode(obx,obr));

        // Here the status is filled with the OBX-11 element
        ID status = obx.getObx11_ObservationResultStatus();
        Observation.ObservationStatus observationStatus = conceptMapResultStatus.getObservationStatusStatusFor(status.getValue());
        molekularDiagnostik.setStatus(observationStatus);

        // Here the observation is filled with the category
        molekularDiagnostik.addCategory(mainRessource.getCategory());

        // Here the observation is filled with the code from the OBX-3 element. In the case of Molekular Diagnostic, the type of examination is indicated here.
        CodeableConcept code = new CodeableConcept();
        ConceptMapHandler conceptMapHandler = new ConceptMapHandler();
        if (obx.getObx3_ObservationIdentifier() != null){
            String sourceCode = obx.getObx3_ObservationIdentifier().getCe1_Identifier().encode();
            ConceptMap conceptMapObservationIdentifier = conceptMapHandler.getRightConceptMap(sourceCode);
            code.addCoding().setSystem("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/uksh-medic-ucic-virologischer-befund-virusnachweistest").setDisplay(conceptMapObservationIdentifier.getTargetDisplay(sourceCode)).setCode(conceptMapObservationIdentifier.getTargetCode(sourceCode));
        }
        molekularDiagnostik.setCode(code);

        // The date and time of the observation from the OBX-14 element is stored here.
        if (!obx.getObx14_DateTimeOfTheObservation().isEmpty()) {
            molekularDiagnostik.getEffectiveDateTimeType().addExtension(new Extension("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum").setValue(mainRessource.getEffectiveDateTime(obx)));
        }

        // The observation value from the OBX-5 is stored here. In this case the result of the test is given
        if (!obx.getObx5_ObservationValue(0).encode().isEmpty()){
            CodeableConcept valueCodeableConcept = new CodeableConcept();
            valueCodeableConcept.addCoding().setSystem("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/ValueSet/mii-vs-mikrobio-positiv-negativ-snomedct").setCode(conceptMapMikrobioPositiveNegative.getTargetCode(obx.getObx5_ObservationValue(0).encode())).setDisplay(conceptMapMikrobioPositiveNegative.getTargetDisplay(obx.getObx5_ObservationValue(0).encode()));//
            molekularDiagnostik.setValue(valueCodeableConcept);
        }
        else {
            molekularDiagnostik.setDataAbsentReason(mainRessource.getDataAbsentreason());
        }


        // The abnormal flag from the OBX-8 element is stored here. This flag indicates a correction of the observed value.
        molekularDiagnostik.addInterpretation(mainRessource.getAbnormalFlag(obx));

        molekularDiagnostik.addReferenceRange().setText(obx.getObx7_ReferencesRange().encode());

        // The specimen of the examination is indicated here
        Reference specimen = new Reference();
        specimen.setReference("src/main/resources/outputs/specimen");
        molekularDiagnostik.setSpecimen(specimen);


        return molekularDiagnostik;

    }
}

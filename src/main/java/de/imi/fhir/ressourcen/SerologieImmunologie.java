package de.imi.fhir.ressourcen;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import de.imi.fhir.conceptMap.ConceptMapMikrobioPositiveNegative;
import de.imi.fhir.conceptMap.ConceptMapResultStatus;
import org.hl7.fhir.r4.model.*;

import java.util.UUID;

/**
 * In this class are the methods for serology and immunology
 */
public class SerologieImmunologie {
    private final ConceptMapResultStatus conceptMapResultStatus = new ConceptMapResultStatus("http://localhost:8888/fhir/ConceptMap/1707?_format=application/fhir+json");
    private final ConceptMapMikrobioPositiveNegative conceptMapMikrobioPositiveNegative = new ConceptMapMikrobioPositiveNegative("http://localhost:8888/fhir/ConceptMap/1?_format=json&_pretty=true");
    private final MainRessource mainRessource = new MainRessource();


    /**
     * @param obx OBX Segment of the current Message
     * @param obr OBR Segment of the current Message
     * @return Observation: the observation for the current line of the message
     */
    public Observation fillSerologieImmunologie(OBX obx, OBR obr) throws HL7Exception {

        // Generates random UUID and sets the Metadata to the Observation
        Observation serologieImmunologie = new Observation();
        serologieImmunologie.setId(UUID.randomUUID().toString());
        serologieImmunologie.setMeta(mainRessource.setMetaData("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/StructureDefinition/immunologie-serologie"));

        // The identifier is stored in the observation. This is composed of OBR-3, OBX-3, OBX-4
        serologieImmunologie.addIdentifier(mainRessource.getAnalyseBefundCode(obx,obr));

        // Here the status is filled with the OBX-11 element
        ID status = obx.getObx11_ObservationResultStatus();
        Observation.ObservationStatus observationStatus = conceptMapResultStatus.getObservationStatusStatusFor(status.getValue());
        serologieImmunologie.setStatus(observationStatus);

        // Here the observation is filled with the category
        serologieImmunologie.addCategory(mainRessource.getCategory());

        // The abnormal flag from the OBX-8 element is stored here. This flag indicates a correction of the observed value.
        serologieImmunologie.addInterpretation(mainRessource.getAbnormalFlag(obx));

        // Here the observation is filled with the code from the OBX-3 element. In this case, there are no concept maps for the type of investigation yet
        CodeableConcept code = new CodeableConcept();
        code.addCoding().setCode(obx.getObx3_ObservationIdentifier().encode()).setSystem("https://simplifier.net/medizininformatik-initiative-modul-mikrobiologie/mii-vs-mikrobio-serologie-immunologie-loinc"); 
        serologieImmunologie.setCode(code);

        // The date and time of the observation from the OBX-14 element is stored here.
        if (!obx.getObx14_DateTimeOfTheObservation().isEmpty()) {
            serologieImmunologie.getEffectiveDateTimeType().addExtension(new Extension("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum").setValue(mainRessource.getEffectiveDateTime(obx)));
        }

        // The observation value from the OBX-5 is stored here. In this case the result of the test is given
        if (!obx.getObx5_ObservationValue(0).encode().isEmpty()) {
            CodeableConcept valueCodeableConcept = new CodeableConcept();
            valueCodeableConcept.addCoding().setSystem("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/ValueSet/mii-vs-mikrobio-positiv-negativ-snomedct").setCode(conceptMapMikrobioPositiveNegative.getTargetCode(obx.getObx5_ObservationValue(0).encode())).setDisplay(conceptMapMikrobioPositiveNegative.getTargetDisplay(obx.getObx5_ObservationValue(0).encode()));//
            serologieImmunologie.setValue(valueCodeableConcept);
        }
        else {
            serologieImmunologie.setDataAbsentReason(mainRessource.getDataAbsentreason());
        }

        // The patient of the examination is referenced here
        Reference patient = new Reference("src/main/resources/dummyPatient");
        serologieImmunologie.setSubject(patient);

        serologieImmunologie.addReferenceRange().setText(obx.getObx7_ReferencesRange().encode());

        // The specimen of the examination is indicated here
        Reference specimen = new Reference();
        specimen.setReference("src/main/resources/outputs/specimen");
        serologieImmunologie.setSpecimen(specimen);

        return serologieImmunologie;
    }

}

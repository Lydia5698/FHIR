package de.imi.fhir.ressourcen;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import de.imi.fhir.conceptMap.ConceptMapResultStatus;
import org.hl7.fhir.r4.model.*;

import java.util.UUID;

public class SerologieImmunologie { // Antigene
    //OBX 1,2,
    // 3(frage^Fragestellung),
    // 4(1),
    // 5(Pathogene Darmkeime (Salmonella spp., Shigella spp., Campylobacter spp., Yersinia spp.) nicht nachgewiesen.),
    // 8, (N)
    // 11,(P)
    // 14 (202207041449)
    private ConceptMapResultStatus conceptMapResultStatus = new ConceptMapResultStatus("http://localhost:8888/fhir/ConceptMap/1707?_format=application/fhir+json");
    private MainRessource mainRessource = new MainRessource();

    // identifier -> analyseBefundCode -> type -> observationInstanceV2, System uri, Value
    // Status
    // category -> coding -> lonic-observation (code), observation-category (laboratory), lonic-microbiology-studies
    // code OBX-3 -> coding
    // subject?
    // effective -> effectiveDateTime -> extension -> QuelleKlinischesBezugsDatum
    public Observation fillSerologieImmunologie(OBX obx, OBR obr) throws HL7Exception {
        Observation serologieImmunologie = new Observation();
        serologieImmunologie.setId(UUID.randomUUID().toString());
        serologieImmunologie.setMeta(mainRessource.setMetaData("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/StructureDefinition/immunologie-serologie"));

        serologieImmunologie.addIdentifier(mainRessource.getAnalyseBefundCode(obx,obr)); // OBX-21
        // option: 1) OBR-3 + OBX-3 + OBX-4 or 2) OBR-3 + OBR-4 + OBX-3 + OBX-4 or 2) some other way to uniquely ID the OBR/ORC + OBX-3 + OBX-4.

        ID status = obx.getObx11_ObservationResultStatus();
        Observation.ObservationStatus observationStatus = conceptMapResultStatus.getObservationStatusStatusFor(status.getValue());
        serologieImmunologie.setStatus(observationStatus);

        serologieImmunologie.addCategory(mainRessource.getCategory());
        serologieImmunologie.addInterpretation(mainRessource.getAbnormalFlag(obx));

        CodeableConcept code = new CodeableConcept();
        code.addCoding().setCode(obx.getObx3_ObservationIdentifier().encode()).setSystem("https://simplifier.net/medizininformatik-initiative-modul-mikrobiologie/mii-vs-mikrobio-serologie-immunologie-loinc"); 
        serologieImmunologie.setCode(code); //OBX-3 // TODO: 04.07.23 System nicht ganz richtig noch mappen

        if (!obx.getObx14_DateTimeOfTheObservation().isEmpty()) {
            serologieImmunologie.getEffectiveDateTimeType().addExtension(new Extension("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum").setValue(mainRessource.getEffectiveDateTime(obx)));
        }

        CodeableConcept valueCodeableConcept = new CodeableConcept();
        valueCodeableConcept.setText(obx.getObx5_ObservationValue(0).encode());
        serologieImmunologie.setValue(valueCodeableConcept);

        Reference patient = new Reference("src/main/resources/dummyPatient");
        serologieImmunologie.setSubject(patient);

        serologieImmunologie.addReferenceRange().setText(obx.getObx7_ReferencesRange().encode());

        Reference specimen = new Reference();
        specimen.setReference("src/main/resources/outputs/specimen");
        serologieImmunologie.setSpecimen(specimen);

        return serologieImmunologie;
    }



}

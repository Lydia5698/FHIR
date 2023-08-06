package de.imi.fhir.ressourcen;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Specimen;

import java.util.UUID;

public class SpecimenMI { //OBR-15 Ort art der probe
    // Menge der Probe in OBX-5 als freitext
    private MainRessource mainRessource = new MainRessource();
    public Specimen specimenMI(OBR obr) throws HL7Exception {
        Specimen specimen = new Specimen();
        specimen.setId(UUID.randomUUID().toString());
        specimen.setMeta(mainRessource.setMetaData("http://hl7.org/fhir/StructureDefinition/Specimen"));
        Specimen.SpecimenCollectionComponent collection = new Specimen.SpecimenCollectionComponent();
        CodeableConcept bodySite = new CodeableConcept();
        bodySite.addCoding().setDisplay(obr.getObr15_SpecimenSource().getSps4_BodySite().encode());
        CodeableConcept method = new CodeableConcept();
        method.addCoding().setDisplay(obr.getObr15_SpecimenSource().getSps3_SpecimenCollectionMethod().encode());
        collection.setBodySite(bodySite);
        collection.setMethod(method);
        CodeableConcept type = new CodeableConcept();
        type.addCoding().setDisplay(obr.getObr15_SpecimenSource().getSps1_SpecimenSourceNameOrCode().encode());
        specimen.setType(type);
        specimen.setCollection(collection);

        return specimen;

        // 15.1 Specimen Source Name or Code
        // 15.3 Specimen Collection methode
        // 15.4 Body Site
    }
}

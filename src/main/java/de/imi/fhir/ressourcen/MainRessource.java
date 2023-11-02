package de.imi.fhir.ressourcen;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import de.imi.fhir.conceptMap.CodeSystemAbnormalFlags;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;


public class MainRessource {
    private final CodeSystemAbnormalFlags codeSystemAbnormalFlags = new CodeSystemAbnormalFlags("http://localhost:8888/fhir/CodeSystem/52?_format=application/fhir+json");

    /**
     * This method returns the time and date of the observation
     * @param obx OBX Segment of the current Message
     * @return the current Time of the Message
     */
    public Coding getEffectiveDateTime(OBX obx) throws HL7Exception {
        Coding time = new Coding();
        time.setDisplay(obx.getObx14_DateTimeOfTheObservation().encode()).setSystem("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum");
        return time;
    }

    /**
     * This method returns the time and date of the OBR-Segment
     * @param obr OBR Segment of the current Message
     * @return the current Time of the Message
     */
    public Coding getEffectiveDateTimeOBR(OBR obr) throws HL7Exception {
        Coding time = new Coding();
        time.setDisplay(obr.getObr7_ObservationDateTime().encode()).setSystem("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/ValueSet/QuelleKlinischesBezugsdatum");
        return time;
    }

    /**
     * This method returns the code from the observation.
     * @param obx OBX Segment of the current Message
     * @param obr OBR Segment of the current Message
     * @return the code from the observation
     */
    public Identifier getAnalyseBefundCode(OBX obx, OBR obr) throws HL7Exception {
        Identifier analyseBefundCode = new Identifier();
        analyseBefundCode.setSystem("URI");
        analyseBefundCode.setValue(obr.getObr3_FillerOrderNumber().encode() + obx.getObx3_ObservationIdentifier().encode() + obx.getObx4_ObservationSubID().encode()); // OBR-3, OBX-3, OBX-4
        Organization organization = new Organization();
        organization.addAddress();
        analyseBefundCode.setAssigner(new Reference(organization));
        CodeableConcept type = new CodeableConcept();
        Coding observationInstanceV2 = new Coding();
        type.addCoding(observationInstanceV2);
        observationInstanceV2.setSystem("http://terminology.hl7.org/CodeSystem/v2-0203");
        observationInstanceV2.setCode("OBI");
        analyseBefundCode.setType(type);
        return analyseBefundCode;
    }

    /**
     * This method shows the category of the observation. Here some fixed values are set
     * @return the category from the observation
     */
    public CodeableConcept getCategory(){
        Coding lonicObservation = new Coding();
        lonicObservation.setSystem("http://loinc.org");
        lonicObservation.setDisplay("Laboratory studies");
        lonicObservation.setCode("26436-6");
        Coding observationCategory = new Coding();
        observationCategory.setSystem("http://terminology.hl7.org/CodeSystem/observation-category");
        observationCategory.setCode("laboratory");
        Coding lonicMicrobiologyStudies = new Coding();
        lonicMicrobiologyStudies.setCode("microbiology studies");
        List<Coding> coding = new ArrayList<>();
        coding.add(lonicObservation);
        coding.add(observationCategory);
        coding.add(lonicMicrobiologyStudies);
        CodeableConcept category = new CodeableConcept();
        category.setCoding(coding);
        return category;
    }

    /**
     * The abnormal flag from the OBX-8 element is stored here. This flag indicates a correction of the observed value.
     * @param obx OBX Segment of the current Message
     * @return abnormal flag
     */
    public CodeableConcept getAbnormalFlag(OBX obx) throws HL7Exception {
        CodeableConcept interpretation = new CodeableConcept();
        String[] stringArray = new String[obx.getObx8_AbnormalFlags().length];
        for (int i = 0; i < obx.getObx8_AbnormalFlags().length; i++) {
            stringArray[i] = obx.getObx8_AbnormalFlags(i).encode();
        }
        // OBX.8 Abnormal Flags "https://r4.ontoserver.csiro.au/fhir/CodeSystem/v2-0078?_format=application/fhir+json"
        if (!ArrayUtils.isEmpty(stringArray)) {
            interpretation.addCoding().setSystem("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation").setVersion(codeSystemAbnormalFlags.getAbnormalFlagVersion()).setCode(stringArray[0]).setDisplay(codeSystemAbnormalFlags.getAbnormalFlagFor(stringArray[0]));
        }
        return interpretation;
    }

    /**
     * Sets the Metadata to the Observation
     * @param url the url to the fhir resource
     * @return Metadata
     */
    public Meta setMetaData(String url){
        Meta metaData = new Meta();
        metaData.setSource("https://simplifier.net/Medizininformatik-Initiative-Modul-Mikrobiologie/~introduction");
        metaData.addProfile(url);
        return metaData;
    }

    /**
     * The reason for the absence of certain data is given here
     * @return Data absent Reason
     */
    public CodeableConcept getDataAbsentreason(){
        CodeableConcept dataAbsentreason = new CodeableConcept();
        dataAbsentreason.addCoding().setCode("unsupported").setDisplay("http://terminology.hl7.org/CodeSystem/data-absent-reason");
        return dataAbsentreason;
    }
}

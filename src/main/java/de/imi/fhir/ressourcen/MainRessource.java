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
    private CodeSystemAbnormalFlags codeSystemAbnormalFlags = new CodeSystemAbnormalFlags("http://localhost:8888/fhir/CodeSystem/52?_format=application/fhir+json");
    public Coding getEffectiveDateTime(OBX obx) throws HL7Exception {
        Coding time = new Coding();
        time.setCode(obx.getObx14_DateTimeOfTheObservation().encode()).setSystem("?System Date Time");
        return time;
    }
    public Coding getEffectiveDateTimeOBR(OBR obr) throws HL7Exception {
        Coding time = new Coding();
        time.setCode(obr.getObr7_ObservationDateTime().encode()).setSystem("?System Date Time");
        return time;
    }

    public Identifier getAnalyseBefundCode(OBX obx, OBR obr) throws HL7Exception {
        Identifier analyseBefundCode = new Identifier(); // gute Idee?
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

    public CodeableConcept getAbnormalFlag(OBX obx) throws HL7Exception {
        CodeableConcept interpretation = new CodeableConcept();
        String[] stringArray = new String[obx.getObx8_AbnormalFlags().length];
        for (int i = 0; i < obx.getObx8_AbnormalFlags().length; i++) {
            stringArray[i] = obx.getObx8_AbnormalFlags(i).encode();
        }
        // OBX.8 Abnormal Flags "https://r4.ontoserver.csiro.au/fhir/CodeSystem/v2-0078?_format=application/fhir+json"
        if (!ArrayUtils.isEmpty(stringArray)) {
            interpretation.addCoding().setSystem("Abnormal Flags").setCode(stringArray[0]).setDisplay(codeSystemAbnormalFlags.getAbnormalFlagFor(stringArray[0]));
        }
        return interpretation;
    }

    public Meta setMetaData(String url){
        Meta metaData = new Meta();
        metaData.setSource("https://simplifier.net/Medizininformatik-Initiative-Modul-Mikrobiologie/~introduction");
        metaData.addProfile(url);
        return metaData;
    }
}

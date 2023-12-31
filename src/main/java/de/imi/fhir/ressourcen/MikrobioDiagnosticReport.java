package de.imi.fhir.ressourcen;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import de.imi.fhir.conceptMap.CodeSystemDiagnosticServiceSectionID;
import de.imi.fhir.conceptMap.ConceptMapResultStatus;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This is the class for the diagnostic report which is filled with the OBR segment.
 */
public class MikrobioDiagnosticReport {
    private final ConceptMapResultStatus conceptMapResultStatus = new ConceptMapResultStatus("http://localhost:8888/fhir/ConceptMap/1707?_format=application/fhir+json");
    private final MainRessource mainRessource = new MainRessource();
    private final CodeSystemDiagnosticServiceSectionID codeSystemDiagnosticServiceSectionID = new CodeSystemDiagnosticServiceSectionID("http://localhost:8888/fhir/CodeSystem/102?_format=application/fhir+json");

    /**
     * @param obr OBR Segment of the current Message
     * @param reports the references to the corresponding OBX segments
     * @return diagnostic Report
     */
    public DiagnosticReport fillDiagnosticReport(OBR obr, List<Reference> reports) throws HL7Exception {

        // Generates random UUID and sets the Metadata to the Observation
        DiagnosticReport diagnosticReport = new DiagnosticReport();
        diagnosticReport.setId(UUID.randomUUID().toString());
        diagnosticReport.setMeta(mainRessource.setMetaData("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/StructureDefinition/diagnostic-report"));

        Reference basedOn = new Reference();
        diagnosticReport.addBasedOn(basedOn);

        // Here the status is filled with the OBR-25 element
        ID status = obr.getObr25_ResultStatus();
        DiagnosticReport.DiagnosticReportStatus diagnosticReportStatus = conceptMapResultStatus.getDiagnosticReportStatusStatusFor(status.getValue());
        diagnosticReport.setStatus(diagnosticReportStatus);

        // The identifier is stored in the observation. OBR-51 for globally unique filler ID - OBR-3, For non-globally unique filler-id the flller/placer number must be combined with the universal service Id - OBR-2(if present)+OBR-3+OBR-4
        Identifier befund = new Identifier();
        CodeableConcept type = new CodeableConcept();
        type.addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203").setCode("FILL"));
        befund.setType(type);
        befund.setSystem("");
        befund.setValue(obr.getObr2_PlacerOrderNumber().encode() + obr.getObr3_FillerOrderNumber().encode() + obr.getObr4_UniversalServiceIdentifier().encode());
        diagnosticReport.addIdentifier(befund);

        // Here the observation is filled with the category
        Coding loincLab = new Coding();
        List<Coding> codings = new ArrayList<>();
        loincLab.setCode("26436-6").setSystem("http://loinc.org");
        Coding diagnosticServiceSections = new Coding(); // laboratory
        if (!obr.getObr24_DiagnosticServSectID().encode().isEmpty()){
            diagnosticServiceSections.setSystem("http://terminology.hl7.org/CodeSystem/v2-0074");  //OBR-24
            diagnosticServiceSections.setCode(obr.getObr24_DiagnosticServSectID().encode()).setDisplay(codeSystemDiagnosticServiceSectionID.getDiagnosticDisplayFor(obr.getObr24_DiagnosticServSectID().encode()));
            codings.add(diagnosticServiceSections);
        }
        Coding snomedMicrobiologyStudies = new Coding();
        snomedMicrobiologyStudies.setSystem("http://snomed.info/sct");
        snomedMicrobiologyStudies.setCode("4341000179107");
        snomedMicrobiologyStudies.setDisplay("Microbiology report (record artifact)");
        Coding loincMicrobiologySpecialization = new Coding();
        loincMicrobiologySpecialization.setSystem("https://www.medizininformatik-initiative.de/fhir/modul-mikrobiologie/ValueSet/mii-vs-mikrobio-befundtyp-loinc");
        codings.add(loincLab);
        codings.add(snomedMicrobiologyStudies);
        CodeableConcept category = new CodeableConcept();
        category.setCoding(codings);
        diagnosticReport.addCategory(category);

        // Here the patient is referenced to the observation
        Reference patient = new Reference("src/main/resources/dummyPatient");
        diagnosticReport.setSubject(patient);

        // Here the observation is filled with the code from the OBR-4 element.
        CodeableConcept coding1 = new CodeableConcept();
        Coding lonicLabReport = new Coding();
        lonicLabReport.setSystem("http://loinc.org");
        lonicLabReport.setCode("11502-2").setDisplay(obr.getObr4_UniversalServiceIdentifier().encode());
        coding1.addCoding(lonicLabReport);
        diagnosticReport.setCode(coding1);

        // The date and time of the observation from the OBR-7 element is stored here.
        if (!obr.getObr7_ObservationDateTime().isEmpty()) {
            diagnosticReport.getEffectiveDateTimeType().addExtension(new Extension("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum").setValue(mainRessource.getEffectiveDateTimeOBR(obr)));
        }

        // The date and time of the issued observation from the OBR-22 element is stored here.
        diagnosticReport.setIssued(obr.getObr22_ResultsRptStatusChngDateTime().getTime().getValueAsDate());

        // The practitioner and results interpreter are referenced here. These are dummies
        Reference practitioner = new Reference();
        practitioner.setReference("src/main/resources/dummyPractitioner");
        diagnosticReport.addResultsInterpreter(practitioner);
        diagnosticReport.addPerformer(practitioner);

        // The specimen for the examination is referenced here
        Reference specimen = new Reference();
        specimen.setReference("src/main/resources/outputs/specimen" + specimen.getId());
        diagnosticReport.addSpecimen(specimen);
        Reference obxReference = new Reference();
        obxReference.setReference("http://someserver/some-path");
        obxReference.setType("OBX");
        diagnosticReport.setResult(reports);

        return diagnosticReport;

    }
}

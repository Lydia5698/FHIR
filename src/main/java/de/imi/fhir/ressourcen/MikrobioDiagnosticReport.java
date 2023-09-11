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

public class MikrobioDiagnosticReport { //OBR
    private ConceptMapResultStatus conceptMapResultStatus = new ConceptMapResultStatus("http://localhost:8888/fhir/ConceptMap/1707?_format=application/fhir+json");
    private MainRessource mainRessource = new MainRessource();
    private CodeSystemDiagnosticServiceSectionID codeSystemDiagnosticServiceSectionID = new CodeSystemDiagnosticServiceSectionID("http://localhost:8888/fhir/CodeSystem/102?_format=application/fhir+json");

    public DiagnosticReport fillDiagnosticReport(OBR obr, List<Reference> reports) throws HL7Exception {
        // Based on?
        // Status OBR25
        // category OBR 24 http://terminology.hl7.org/CodeSystem/v2-0074 für MB aber was ist mit loinc-lab und snomed-microbiology-studies
        // Code OBR-4 Bsp. bkan^Blutkultur anaerob^unt^12035 Map to LONIC?
        // subject?
        // effective OBR-7 Date Time nicht vorhanden? Specimen CollectionTimes OBR 20?
        // issued OBR-22 Date Time wenn reviewd OBR-20? Haben nur ein Datum
        DiagnosticReport diagnosticReport = new DiagnosticReport();
        diagnosticReport.setId(UUID.randomUUID().toString());
        diagnosticReport.setMeta(mainRessource.setMetaData("https://www.medizininformatik-initiative.de/fhir/modul-mikrobio/StructureDefinition/diagnostic-report"));

        Reference basedOn = new Reference(); //// TODO: 18.07.23 ?
        diagnosticReport.addBasedOn(basedOn);

        ID status = obr.getResultStatus();
        DiagnosticReport.DiagnosticReportStatus diagnosticReportStatus = conceptMapResultStatus.getDiagnosticReportStatusStatusFor(status.getValue());
        diagnosticReport.setStatus(diagnosticReportStatus); //OBR-25

        Identifier befund = new Identifier(); // OBR-51/ for globally unique filler ID - OBR-3 , For non-globally unique filler-id the flller/placer number must be combined with the universal service Id - OBR-2(if present)+OBR-3+OBR-4
        CodeableConcept type = new CodeableConcept();
        type.addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203").setCode("FILL"));
        befund.setType(type);
        befund.setSystem("");
        befund.setValue(obr.getObr2_PlacerOrderNumber().encode() + obr.getObr3_FillerOrderNumber().encode() + obr.getObr4_UniversalServiceIdentifier().encode());
        diagnosticReport.addIdentifier(befund);
        // OBR-2 +3+4 = 28516883^LABOR + 28516883^OSMCLAR + 1^MRGN

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

        Reference patient = new Reference("src/main/resources/dummyPatient");
        diagnosticReport.setSubject(patient);

        CodeableConcept coding1 = new CodeableConcept();
        Coding lonicLabReport = new Coding();
        lonicLabReport.setSystem("http://loinc.org");
        lonicLabReport.setCode("11502-2").setDisplay(obr.getObr4_UniversalServiceIdentifier().encode());
        coding1.addCoding(lonicLabReport);
        diagnosticReport.setCode(coding1); //OBR-4

        if (!obr.getObr7_ObservationDateTime().isEmpty()) {
            diagnosticReport.getEffectiveDateTimeType().addExtension(new Extension("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum").setValue(mainRessource.getEffectiveDateTimeOBR(obr)));
        } // OBR-7
        diagnosticReport.setIssued(obr.getObr22_ResultsRptStatusChngDateTime().getTime().getValueAsDate()); //OBR-22

        Reference practitioner = new Reference();
        practitioner.setReference("src/main/resources/dummyPractitioner");
        diagnosticReport.addResultsInterpreter(practitioner);
        diagnosticReport.addPerformer(practitioner);

        Reference specimen = new Reference();
        specimen.setReference("src/main/resources/outputs/specimen" + specimen.getId());
        diagnosticReport.addSpecimen(specimen);
        Reference obxReference = new Reference();
        obxReference.setReference("http://someserver/some-path");
        obxReference.setType("OBX");
        diagnosticReport.setResult(reports);
        //diagnosticReport.setConclusion();

        return diagnosticReport;

        //OBR-14? Specimen Received Date/Time
        //OBR-15? Specimen Source
        //OBR-18  Placer Field
        //OBR-20  Filler Field


    }
}

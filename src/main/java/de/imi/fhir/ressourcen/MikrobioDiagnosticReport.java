package de.imi.fhir.ressourcen;

import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import de.imi.fhir.conceptMap.ConceptMapResultStatus;
import org.hl7.fhir.r4.model.DiagnosticReport;

public class MikrobioDiagnosticReport {
    private ConceptMapResultStatus conceptMapResultStatus = new ConceptMapResultStatus("http://localhost:8888/fhir/ConceptMap/1?_format=application/fhir+json");

    public void fillDiagnosticReport(OBR obr){
        // Based on?
        // Status OBR25
        // category OBR 24 http://terminology.hl7.org/CodeSystem/v2-0074 für MB aber was ist mit loinc-lab und snomed-microbiology-studies
        // Code OBR-4 Bsp. bkan^Blutkultur anaerob^unt^12035 Map to LONIC?
        // subject?
        // effective OBR-7 Date Time nicht vorhanden? Specimen CollectionTimes OBR 20?
        // issued OBR-22 Date Time wenn reviewd OBR-20? Haben nur ein Datum
        DiagnosticReport diagnosticReport = new DiagnosticReport();

        ID status = obr.getResultStatus();
        DiagnosticReport.DiagnosticReportStatus diagnosticReportStatus = conceptMapResultStatus.getDiagnosticReportStatusStatusFor(status.getValue());
        diagnosticReport.setStatus(diagnosticReportStatus);

    }
}

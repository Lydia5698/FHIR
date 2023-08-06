package de.imi.fhir.conceptMap;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
class CodeSystemDiagnosticServiceSectionIDTest {

    @Test
    public void testGetCodeFor() {
        CodeSystemDiagnosticServiceSectionID codeSystemDiagnosticServiceSectionID = new CodeSystemDiagnosticServiceSectionID("http://localhost:8888/fhir/CodeSystem/102?_format=application/fhir+json");
        assertThat(codeSystemDiagnosticServiceSectionID.getDiagnosticIDFor("MB"), equalTo("Microbiology"));
    }

}
package de.imi.fhir.conceptMap;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ConceptMapVirologischerBefundTest {

    @Test
    public void testGetterCode() {
        ConceptMapVirologischerBefund conceptMapVirologischerBefund = new ConceptMapVirologischerBefund("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/uksh-medic-ucic-virologischer-befund-virusnachweistest?_format=application/fhir+json");
        String code = "cov2019so";
        assertThat(conceptMapVirologischerBefund.getTargetCode(code), equalTo("94500-6"));
    }

    @Test
    public void testGetterDisplay() {
        ConceptMapVirologischerBefund conceptMapVirologischerBefund = new ConceptMapVirologischerBefund("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/uksh-medic-ucic-virologischer-befund-virusnachweistest?_format=application/fhir+json");
        String code = "cov19stso";
        assertThat(conceptMapVirologischerBefund.getTargetDisplay(code), equalTo("SARS-CoV-2 (COVID-19) RNA [Presence] in Respiratory specimen by NAA with probe detection"));
    }


}
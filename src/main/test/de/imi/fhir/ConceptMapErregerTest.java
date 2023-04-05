package de.imi.fhir;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
class ConceptMapErregerTest {

    @Test
    public void testGetterCode() {
        ConceptMapErreger conceptMapErreger = new ConceptMapErreger("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/highmed-uksh-ucic-hl7-mibibefund-erreger-lokal-snomed?_format=application/fhir+json");
        String code = "citbra2";
        assertThat(conceptMapErreger.getTargetCode(code), equalTo("114262000"));
    }

    @Test
    public void testGetterDisplay() {
        ConceptMapErreger conceptMapErreger = new ConceptMapErreger("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/highmed-uksh-ucic-hl7-mibibefund-erreger-lokal-snomed?_format=application/fhir+json");
        String code = "yerentb1";
        assertThat(conceptMapErreger.getTargetDisplay(code), equalTo("Yersinia enterocolitica"));
    }

    @Test
    public void testGetterEquivalence() {
        ConceptMapErreger conceptMapErreger = new ConceptMapErreger("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/highmed-uksh-ucic-hl7-mibibefund-erreger-lokal-snomed?_format=application/fhir+json");
        String code = "cl";
        assertThat(conceptMapErreger.getEquivalence(code), equalTo("unmatched"));
    }


}
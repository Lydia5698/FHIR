package de.imi.fhir.conceptMap;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ConceptMapAntiinfektivaTest {

    @Test
    public void testGetterCode() {
        ConceptMapAntiinfektiva conceptMapAntiinfektiva = new ConceptMapAntiinfektiva("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/highmed-uksh-ucic-hl7-mibibefund-antiinfektiva-lokal-loinc?_format=application/fhir+json");
        String code = "ab";
        assertThat(conceptMapAntiinfektiva.getTargetCode(code), equalTo("18863-1"));
    }

    @Test
    public void testGetterDisplay() {
        ConceptMapAntiinfektiva conceptMapAntiinfektiva = new ConceptMapAntiinfektiva("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/highmed-uksh-ucic-hl7-mibibefund-antiinfektiva-lokal-loinc?_format=application/fhir+json");
        String code = "cs";
        assertThat(conceptMapAntiinfektiva.getTargetDisplay(code), equalTo("Colistin [Susceptibility]"));
    }

    @Test
    public void testGetterEquivalence() {
        ConceptMapAntiinfektiva conceptMapAntiinfektiva = new ConceptMapAntiinfektiva("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/highmed-uksh-ucic-hl7-mibibefund-antiinfektiva-lokal-loinc?_format=application/fhir+json");
        String code = "ame";
        assertThat(conceptMapAntiinfektiva.getEquivalence(code), equalTo("equivalent"));
    }

}
package de.imi.fhir;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ConceptMapHandlerTest {

    @Test
    public void testGetterCode() {
        ConceptMapHandler conceptMapHandler = new ConceptMapHandler();
        ConceptMap erreger = conceptMapHandler.getRightConceptMap("esccol^Escherichia coli^keim^2880");
        assertThat(erreger.getTargetCode("esccol"), equalTo("112283007"));
    }

    @Test
    public void testFalseInputs(){
        ConceptMapHandler conceptMapHandler = new ConceptMapHandler();
        assertThat(conceptMapHandler.getRightConceptMap("Wundinfektion"), equalTo(null));
    }

}
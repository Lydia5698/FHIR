package de.imi.fhir;

import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MyConceptMapTest {

    @Test
    public void testMapping() {
        MyConceptMap myConceptMap = new MyConceptMap("http://localhost:8888/fhir/ConceptMap/1?_format=application/fhir+json");
        String observationStatusString = "P";
        assertThat(myConceptMap.getObservationStatusStatusFor(observationStatusString), equalTo(Observation.ObservationStatus.PRELIMINARY));
    }

}

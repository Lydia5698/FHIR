package de.imi.fhir;

import ca.uhn.hl7v2.model.DataTypeException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
class V2Test {

    @Test
    public void testMapping() throws IOException, DataTypeException {
        Path path = Path.of("src/main/resources/outputs/Directory1/child0.json");
        String jsonData = Files.readString(path, StandardCharsets.ISO_8859_1);
        //System.out.println(jsonData);
        JSONObject observationJson = new JSONObject(jsonData);
        V2 v2 = new V2(observationJson);
        LinkedList<JSONObject> contentJsonDatei = v2.getCoding();
        assertThat(contentJsonDatei.get(0).getString("display"), equalTo("KOM"));
        assertThat(contentJsonDatei.get(1).getString("display"), equalTo("Das Material entspricht nach visueller Kontrolle nicht der Einsender-Angabe, daher wurde der Abstrich auf Gewebe geändert."));
    }

}
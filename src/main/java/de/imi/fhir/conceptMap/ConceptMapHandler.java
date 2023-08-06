package de.imi.fhir.conceptMap;

public class ConceptMapHandler {

    private String jsonEndung = "?_format=application/fhir+json";
    private ConceptMapAntiinfektiva conceptMapAntiinfektiva = new ConceptMapAntiinfektiva("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/highmed-uksh-ucic-hl7-mibibefund-antiinfektiva-lokal-loinc"+ jsonEndung);
    private ConceptMapErreger conceptMapErreger = new ConceptMapErreger("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/highmed-uksh-ucic-hl7-mibibefund-erreger-lokal-snomed"+ jsonEndung);
    private ConceptMapVirologischerBefund conceptMapVirologischerBefund = new ConceptMapVirologischerBefund("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/uksh-medic-ucic-virologischer-befund-virusnachweistest"+ jsonEndung);
            //TODO Was ist effizienter das JSON Object zurück geben oder methoden hier aufrufen und dieser Klasse ein Stichwort wie code übergeben?
    public ConceptMap getRightConceptMap(String eingabe){
        String[] cases = {"abio", "^keim", "cov"};

        int i;
        for(i = 0; i < cases.length; i++)
            if(eingabe.contains(cases[i])) break;

        switch(i) {
            case 0: //abio
                return conceptMapAntiinfektiva;
            case 1: //keim
                return conceptMapErreger;
            case 2: //corona
                return conceptMapVirologischerBefund; 
            default:
                return null;
        }
    }

}

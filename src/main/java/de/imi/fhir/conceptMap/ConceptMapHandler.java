package de.imi.fhir.conceptMap;

public class ConceptMapHandler {

    private final String jsonEndung = "?_format=application/fhir+json";
    private final ConceptMapAntiinfektiva conceptMapAntiinfektiva = new ConceptMapAntiinfektiva("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/highmed-uksh-ucic-hl7-mibibefund-antiinfektiva-lokal-loinc"+ jsonEndung);
    private final ConceptMapErreger conceptMapErreger = new ConceptMapErreger("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/highmed-uksh-ucic-hl7-mibibefund-erreger-lokal-snomed"+ jsonEndung);
    private final ConceptMapVirologischerBefund conceptMapVirologischerBefund = new ConceptMapVirologischerBefund("https://ontoserver.imi.uni-luebeck.de/fhir/ConceptMap/uksh-medic-ucic-virologischer-befund-virusnachweistest"+ jsonEndung);

    public ConceptMap getRightConceptMap(String eingabe){
        String[] cases = {"abio", "^keim", "cov"};

        int i;
        for(i = 0; i < cases.length; i++)
            if(eingabe.contains(cases[i])) break;

        switch(i) {
            case 0: //Antibiogramm
                return conceptMapAntiinfektiva;
            case 1: //Keim
                return conceptMapErreger;
            case 2: //Corona
                return conceptMapVirologischerBefund; 
            default:
                return null;
        }
    }

}

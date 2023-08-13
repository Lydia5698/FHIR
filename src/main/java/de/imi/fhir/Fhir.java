package de.imi.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.parser.Parser;
import de.imi.fhir.ressourcen.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Fhir {
    HapiContext context = new DefaultHapiContext();
    Parser p = context.getPipeParser();
    public void start(String saveTo, String file) throws HL7Exception, IOException  { // TODO umbauen um den startfile zu finden
        String directoryName = "ObservationDirectoryCorona";
        Path path = Path.of("src/main/resources/MiBi" +"/multiple678"); // File
        //Path path = Path.of(file);
        String hl7String = Files.readString(path, StandardCharsets.ISO_8859_1);
        Message message = p.parse(hl7String);
        ORU_R01 oruR01 = (ORU_R01) p.parse(message.encode());
        saveTo = "/Users/lydia/Desktop/Uni/6 Semester/BA Script/src/main/resources/outputs/Directory1"; //todo Hier save To ändern
        //saveToFhir(oruR01, saveTo);
        saveToFhir(oruR01, saveTo);

    }
    public void saveToFhir(ORU_R01 oruR01, String path) throws HL7Exception {
      /*  File directory = new File(path);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }*/
        KulturDiagnostik kulturDiagnostik = new KulturDiagnostik();
        MolekularDiagnostik molekularDiagnostik = new MolekularDiagnostik();
        SerologieImmunologie serologieImmunologie = new SerologieImmunologie();
        MikrobioDiagnosticReport mikrobioDiagnosticReport = new MikrobioDiagnosticReport();

        int orderObservationCount = 0;
        //ORU_R01_ORDER_OBSERVATION orderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(orderObservationCount);
        List<ORU_R01_ORDER_OBSERVATION> allOrderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATIONAll();

        Observation parentObservation = new Observation();
        // Kulturdiagnostik
        // String profile = "https://highmed.org/fhir/StructureDefinition/ic/Kulturdiagnostik&scope=MedizininformatikInitiative-HiGHmed-IC@current";
        // Directory
        // String profileDirectory = "src/main/resources/Profiles/MII/medizininformatikinitiative-highmed-ic/ressourcen-profile";
        parentObservation.getMeta().addProfile("https://highmed.org/fhir/StructureDefinition/ic/Kulturdiagnostik&scope=MedizininformatikInitiative-HiGHmed-IC@current");
        SpecimenMI specimen = new SpecimenMI();
        saveFile(specimen.specimenMI(allOrderObservation.get(0).getOBR()),"specimen", path);
        List<Reference> results = new ArrayList<>();
        for (ORU_R01_ORDER_OBSERVATION orderObservation : allOrderObservation) {
            List<ORU_R01_OBSERVATION> allObservations = orderObservation.getOBSERVATIONAll();
            OBR obr = orderObservation.getOBR();
            for (ORU_R01_OBSERVATION observation : allObservations) {
                if ((observation.getOBX().getObx3_ObservationIdentifier().getCe3_NameOfCodingSystem().encode().contains("abio"))){
                    Observation antibio = kulturDiagnostik.fillEmpfindlichkeit(observation.getOBX(),obr);
                    results.add(new Reference(path + "/" + "antibiogramm" + antibio.getIdElement().getValue() + ".json"));
                    saveFile(antibio, "antibiogramm", path);

                }
                else if (observation.getOBX().getObx5_ObservationValue(0).encode().contains("MRGN")){
                    Observation mrgn = kulturDiagnostik.fillMRGN(observation.getOBX(),obr);
                    results.add(new Reference(path + "/" + "mrgn" + mrgn.getIdElement().getValue() + ".json"));
                    saveFile(mrgn, "mrgn", path);
                }
                else if (observation.getOBX().getObx5_ObservationValue(0).encode().contains("MRE")){
                    Observation mre = kulturDiagnostik.fillMRE(observation.getOBX(),obr);
                    results.add(new Reference(path + "/" + "mre" + mre.getIdElement().getValue() + ".json"));
                    saveFile(mre, "mre", path);
                }
                else if (observation.getOBX().getObx3_ObservationIdentifier().encode().contains("cov")){
                    Observation corona = molekularDiagnostik.fillMolekularDiagnostik(observation.getOBX(),obr);
                    results.add(new Reference(path + "/" + "molekularDiagnostik" + corona.getIdElement().getValue() + ".json"));
                    saveFile(corona, "molekularDiagnostik", path);
                }
                else if (observation.getOBX().getObx3_ObservationIdentifier().encode().contains("Antigen") || observation.getOBX().getObx5_ObservationValue(0).encode().contains("Toxin") ) {
                    Observation antigen = serologieImmunologie.fillSerologieImmunologie(observation.getOBX(),obr);
                    results.add(new Reference(path + "/" + "serologie" + antigen.getIdElement().getValue() + ".json"));
                    saveFile(antigen, "serologie", path);
                }
                else {
                    Observation kultur = kulturDiagnostik.kulturNachweis(observation.getOBX(),obr);
                    results.add(new Reference(path + "/" + "kulturDiagnostik" + kultur.getIdElement().getValue() + ".json"));
                    saveFile(kultur, "kulturDiagnostik", path);
                }

            }
            if (!orderObservation.getOBR().isEmpty()){
                saveFile(mikrobioDiagnosticReport.fillDiagnosticReport(orderObservation.getOBR(),results), "diagnosticReport", path);
                results.clear();
            }
        }
        // todo Validator mit HTTP Put an den Marshal
        // http://localhost:8888/fhir/ConceptMap/1?_format=application/fhir+json

    }


    public void saveFile(IBaseResource observation, String name, String path){
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        parser.setPrettyPrint(true);
        File file = new File(path + "/"+ name + observation.getIdElement().getValue() + ".json");
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.ISO_8859_1);
            writer.write(parser.encodeResourceToString(observation));

        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                if (writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

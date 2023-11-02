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

    /**
     * In this method, the message is taken and each message line is assigned to a category. This is then saved with saveFile.
     * @param oruR01 The Message
     * @param path the path where the result is to be saved
     */
    public void saveToFhir(ORU_R01 oruR01, String path) throws HL7Exception {
        KulturDiagnostik kulturDiagnostik = new KulturDiagnostik();
        MolekularDiagnostik molekularDiagnostik = new MolekularDiagnostik();
        SerologieImmunologie serologieImmunologie = new SerologieImmunologie();
        MikrobioDiagnosticReport mikrobioDiagnosticReport = new MikrobioDiagnosticReport();

        List<ORU_R01_ORDER_OBSERVATION> allOrderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATIONAll();

        Observation parentObservation = new Observation();

        parentObservation.getMeta().addProfile("https://highmed.org/fhir/StructureDefinition/ic/Kulturdiagnostik&scope=MedizininformatikInitiative-HiGHmed-IC@current");
        // Dummy Specimen
        SpecimenMI specimen = new SpecimenMI();
        saveFile(specimen.specimenMI(allOrderObservation.get(0).getOBR()),"specimen", path);
        List<Reference> results = new ArrayList<>();
        // goes through all message blocks
        for (ORU_R01_ORDER_OBSERVATION orderObservation : allOrderObservation) {
            List<ORU_R01_OBSERVATION> allObservations = orderObservation.getOBSERVATIONAll();
            OBR obr = orderObservation.getOBR();
            // goes through all lines of the message blocks
            for (ORU_R01_OBSERVATION observation : allObservations) {
                // Case Antibiogramm
                if ((observation.getOBX().getObx3_ObservationIdentifier().getCe3_NameOfCodingSystem().encode().contains("abio"))){
                    Observation antibio = kulturDiagnostik.fillEmpfindlichkeit(observation.getOBX(),obr);
                    results.add(new Reference(path + "/" + "antibiogramm" + antibio.getIdElement().getValue() + ".json"));
                    saveFile(antibio, "antibiogramm", path);

                }
                // Case MRGN
                else if (observation.getOBX().getObx5_ObservationValue(0).encode().contains("MRGN")){
                    Observation mrgn = kulturDiagnostik.fillMRGN(observation.getOBX(),obr);
                    results.add(new Reference(path + "/" + "mrgn" + mrgn.getIdElement().getValue() + ".json"));
                    saveFile(mrgn, "mrgn", path);
                }
                // Case MRE
                else if (observation.getOBX().getObx5_ObservationValue(0).encode().contains("MRE")){
                    Observation mre = kulturDiagnostik.fillMRE(observation.getOBX(),obr);
                    results.add(new Reference(path + "/" + "mre" + mre.getIdElement().getValue() + ".json"));
                    saveFile(mre, "mre", path);
                }
                // Case Molecular Diagnostic
                else if (observation.getOBX().getObx3_ObservationIdentifier().encode().contains("cov")){
                    Observation corona = molekularDiagnostik.fillMolekularDiagnostik(observation.getOBX(),obr);
                    results.add(new Reference(path + "/" + "molekularDiagnostik" + corona.getIdElement().getValue() + ".json"));
                    saveFile(corona, "molekularDiagnostik", path);
                }
                // Case Serology Immunology
                else if (observation.getOBX().getObx3_ObservationIdentifier().encode().contains("Antigen") || observation.getOBX().getObx5_ObservationValue(0).encode().contains("Toxin") ) {
                    Observation antigen = serologieImmunologie.fillSerologieImmunologie(observation.getOBX(),obr);
                    results.add(new Reference(path + "/" + "serologie" + antigen.getIdElement().getValue() + ".json"));
                    saveFile(antigen, "serologie", path);
                }
                // Default Case Culture Diagnostics
                else {
                    Observation kultur = kulturDiagnostik.kulturNachweis(observation.getOBX(),obr);
                    results.add(new Reference(path + "/" + "kulturDiagnostik" + kultur.getIdElement().getValue() + ".json"));
                    saveFile(kultur, "kulturDiagnostik", path);
                }

            }
            // Case OBR diagnostic Report
            if (!orderObservation.getOBR().isEmpty()){
                saveFile(mikrobioDiagnosticReport.fillDiagnosticReport(orderObservation.getOBR(),results), "diagnosticReport", path);
                results.clear();
            }
        }
    }


    /**
     * this method stores the observation in JSON
     * @param observation the observation of the message line
     * @param name the type of examination
     * @param path the path
     */
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

package de.imi.fhir;

import ca.uhn.hl7v2.HL7Exception;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws HL7Exception, IOException {
        /*if (args.length != 2) {
            System.err.println("Please provide an argument with the input folder, an argument for the output directory");
            System.exit(1);
        }
        String inputFolder = args[0]; // src/main/resources/MiBi
        String outputFilename = args[1];*/
        Fhir fhir = new Fhir();
        //fhir.saveToFhir();
        String inputFolder = "src/main/resources/MiBi";
        String saveTo = "src/main/resources/outputs/Directory1";
        //fhir.start(inputFolder, outputFilename);
        fhir.start(saveTo,inputFolder);


    }
}

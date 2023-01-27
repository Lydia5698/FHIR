import ca.uhn.hl7v2.HL7Exception;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws HL7Exception, IOException {
        if (args.length != 2) {
            System.err.println("Please provide an argument with the input folder, and an argument for the output file");
            System.exit(1);
        }
        String inputFolder = args[0];
        String outputFilename = args[1];
        System.out.printf("Reading files from %s, writing to %s\n", inputFolder, outputFilename);
        Script script = new Script(inputFolder, outputFilename);
        script.ausgabe();

    }
}

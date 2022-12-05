import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Hl7InputStreamMessageIterator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Stream;


public class Script {
    private final String startFolder;
    private final String outputFilename;
    HapiContext context = new DefaultHapiContext();
    Parser p = context.getPipeParser();

    final int maximumRepetition = 100;

    public Script(String startFolder, String outputFilename) {
        this.startFolder = startFolder;
        this.outputFilename = outputFilename;
    }

    public void ausgabe() throws HL7Exception, IOException {

        //Stream<Path> streamOfPaths;
        Path outputPath = Path.of(this.outputFilename);
        HashSet<String> contentList;
        // the file may not exist at first
        if (outputPath.toFile().exists()) {
            contentList = new HashSet<>(Files.readAllLines(outputPath, StandardCharsets.ISO_8859_1));
        } else {
            contentList = new HashSet<>();
        }
        // try-with-resources allows for automatic closing of a buffer, so we can keep the writer open as long as we need it,
        // since re-opening the writer takes some time.
        try (Writer writer = new BufferedWriter(new FileWriter(outputPath.toFile(), StandardCharsets.ISO_8859_1, true))) {
            System.out.printf("Collecting files from input directory %s\n", this.startFolder);

            // files.walk is recursive, so we can find files in subdirs; and we can also pre-filter the stream
            try (Stream<Path> streamOfPaths = Files.walk(Path.of(this.startFolder)).filter(p ->
                    p.toFile().isFile() && !p.endsWith(".DS_Store") && !p.endsWith(".gitkeep"))) {

                Iterator<Path> pathIterator = streamOfPaths.iterator();
                long pathIndex = 0;
                // streamOfPaths is a stream that only enumerates its members when requested
                // this is *much* more performant than collecting this to a list with some million entries, and
                // requires much less RAM
                while (pathIterator.hasNext()) {
                    Path path = pathIterator.next();
                    File file = path.toFile();
                    pathIndex++;
                    // only print every few files, so we don't artificially slow down the process
                    if (pathIndex % 100 == 0) {
                        System.out.printf("Reading file %d - %s\n", pathIndex, path);
                    }

                    InputStream inputStream = new FileInputStream(file.getAbsolutePath());
                    Hl7InputStreamMessageIterator streamMessageIterator = new Hl7InputStreamMessageIterator(inputStream);
                    Message message = streamMessageIterator.next();
                    ORU_R01 oruR01 = (ORU_R01) p.parse(message.encode());
                    ORU_R01_ORDER_OBSERVATION orderObservation;
                    // When there are more than one OBX,OBR,ORC
                    for (int j = 0; j < maximumRepetition; j++) {
                        if (!oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j).isEmpty()) {
                            orderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j);
                            OBX obx = orderObservation.getOBSERVATION().getOBX();
                            String identifier = "";
                            for (int n = 0; n < obx.getObservationValue().length; n++) {
                                identifier = identifier.concat(obx.getObservationValue(n).encode());
                            }
                            if (!contentList.contains(identifier)) {
                                contentList.add(identifier);
                                writer.write(identifier);
                                writer.write("\n");
                                writer.flush(); // the output file is buffered, and only occasionally written to file
                                // we want to be able to track the progress while it's running.
                            }
                        }
                    }
                }
            }
        }
    }
}

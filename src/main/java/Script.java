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
import java.nio.file.*;
import java.util.List;

public class Script {
    HapiContext context = new DefaultHapiContext();
    Parser p = context.getPipeParser();

    public Script() {

    }

    public void ausgabe() throws HL7Exception, IOException {
        File folder = new File("src/main/resources/data/");
        File[] listOfFiles = folder.listFiles((dir, name) -> !name.equals(".DS_Store"));
        assert listOfFiles != null;
        for (File file : listOfFiles){
            InputStream inputStream = new FileInputStream(file.getAbsolutePath());
            Hl7InputStreamMessageIterator streamMessageIterator = new Hl7InputStreamMessageIterator(inputStream);
            Message message = streamMessageIterator.next();
            ORU_R01 oruR01 = (ORU_R01) p.parse(message.encode());
            ORU_R01_ORDER_OBSERVATION orderObservation;
            // When there are more than one OBX,OBR,ORC
            for (int j = 0; j<15; j++){
                if(!oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j).isEmpty()){
                    orderObservation = oruR01.getPATIENT_RESULT().getORDER_OBSERVATION(j);
                    OBX obx = orderObservation.getOBSERVATION().getOBX();
                    String identifier = "";
                    for (int n = 0; n < obx.getObservationValue().length; n++){
                        identifier = identifier.concat(obx.getObservationValue(n).encode());
                    }
                    Path path = Path.of("src/main/resources/observationValue.txt");
                    List<String> contentList = Files.readAllLines(path, StandardCharsets.UTF_8);
                    Writer writer = new BufferedWriter(new FileWriter("src/main/resources/observationValue.txt", true));
                    if (!contentList.contains(identifier)) {
                        writer.write(identifier);
                        writer.write("\n");
                        writer.close();
                    }
                }
            }
        }
    }
}

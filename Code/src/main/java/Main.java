import ca.uhn.hl7v2.HL7Exception;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws HL7Exception, IOException {
        System.out.println("Hello World");
        V2 v2 = new V2();
        v2.ausgabe();
    }


}

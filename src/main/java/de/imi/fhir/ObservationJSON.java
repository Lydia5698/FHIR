package de.imi.fhir;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hl7.fhir.r4.model.Observation;

import java.util.List;
public class ObservationJSON {
    @JsonProperty("resourceType")
    private String resourceType;
    @JsonProperty("status")
    private Observation.ObservationStatus status;
    @JsonProperty("component")
    private List<Component> component;
    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Observation.ObservationStatus getStatus() {
        return status;
    }

    public List<Component> getComponent() {
        return component;
    }

    public void setComponent(List<Component> component) {
        this.component = component;
    }

    static class Component {
        @JsonProperty("code")
        private Code code;

        @JsonProperty("interpretation")
        private List<Interpretation> interpretation;
        @JsonProperty("valueCodeableConcept")
        private ValueCodeableConcept valueCodeableConcept;


        public Code getCode() {
            return code;
        }
        public List<Interpretation> getInterpretation() {
            return interpretation;
        }
        public ValueCodeableConcept getValueCodeableConcept() {
            return valueCodeableConcept;
        }

        static class Code {
            public List<Coding> getCoding() {
                return coding;
            }
            @JsonProperty("coding")
            private List<Coding> coding;
        }
        static class ValueCodeableConcept {
            public List<Object> getCoding() {
                return coding;
            }
            @JsonProperty("coding")
            private  List<Object> coding;

        }

        static class Interpretation {
            public List<Object> getCoding() {
                return coding;
            }
            @JsonProperty("coding")
            private  List<Object> coding;

        }

        static class Coding {
            @JsonProperty("system")
            private String system;
            @JsonProperty("code")
            private String code;
            @JsonProperty("display")
            private  String display;
            public String getSystem() {
                return system;
            }
            public String getCode() {
                return code;
            }
            public String getDisplay() {
                return display;
            }
            public void setDisplay(String display) {
                this.display = display;
            }

        }

    }

}

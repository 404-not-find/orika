package ma.glasnost.orika.test.crossfeatures;

import java.util.Set;

public class PolicyElementsTestCaseClasses {
    
    public static class Policy {
        
        private Set<PolicyElement> elements;
        
        public Set<PolicyElement> getElements() {
            return elements;
        }
        
        public void setElements(Set<PolicyElement> elements) {
            this.elements = elements;
        }
        
    }
    
    public static abstract class PolicyElement {
        
    }
    
    public static class CustomerElement extends PolicyElement {
        
    }
    
    public static class ProductElement extends PolicyElement {
        
    }
    
    public static class OtherElement extends PolicyElement {
        
    }
    
    public static class OneOtherElement extends PolicyElement {
        
    }
    
    public static class PolicyDTO {
        
        private Set<PolicyElementDTO> elements;
        
        public Set<PolicyElementDTO> getElements() {
            return elements;
        }
        
        public void setElements(Set<PolicyElementDTO> elements) {
            this.elements = elements;
        }
        
    }
    
    public static abstract class PolicyElementDTO {
        
    }
    
    public static class CustomerElementDTO extends PolicyElementDTO {
        
    }
    
    public static class ProductElementDTO extends PolicyElementDTO {
        
    }
    
    public static class OtherElementDTO extends PolicyElementDTO {
        
    }
    
    public static class OneOtherElementDTO extends PolicyElementDTO {
        
    }
    
}

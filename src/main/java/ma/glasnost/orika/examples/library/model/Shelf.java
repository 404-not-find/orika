package ma.glasnost.orika.examples.library.model;

import java.util.ArrayList;
import java.util.List;

public class Shelf {
    private List<Media> elements;
    
    public List<Media> getElements() {
        return elements;
    }
    
    public void setElements(List<Media> elements) {
        this.elements = elements;
    }
    
    public void addElement(Media element) {
        if (elements == null) {
            elements = new ArrayList<Media>();
        }
        elements.add(element);
    }
    
}

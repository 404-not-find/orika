package ma.glasnost.orika.examples.library.dto;

import java.util.List;

public class ShelfDTO {
    private List<MediaDTO> elements;
    
    public List<MediaDTO> getElements() {
        return elements;
    }
    
    public void setElements(List<MediaDTO> elements) {
        this.elements = elements;
    }
    
}

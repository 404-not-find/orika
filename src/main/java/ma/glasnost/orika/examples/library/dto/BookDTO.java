package ma.glasnost.orika.examples.library.dto;

public class BookDTO extends MediaDTO {
    
    private int pages;
    
    public int getPages() {
        return pages;
    }
    
    public void setPages(int pages) {
        this.pages = pages;
    }
    
    @Override
    public String toString() {
        return "BookDTO [pages=" + pages + ", title=" + getTitle() + ",author=" + getAuthor() + ", category=" + getCategory() + "]";
    }
    
}

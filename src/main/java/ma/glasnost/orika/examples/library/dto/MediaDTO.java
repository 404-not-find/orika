package ma.glasnost.orika.examples.library.dto;

public abstract class MediaDTO {
    
    private String title;
    private AuthorDTO author;
    private String category;
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public AuthorDTO getAuthor() {
        return author;
    }
    
    public void setAuthor(AuthorDTO author) {
        this.author = author;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
}

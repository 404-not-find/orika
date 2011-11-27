package ma.glasnost.orika.examples.library.model;

public class Book extends Media {
    
    private int pages;
    
    public Book(String title, Author author, int pages) {
        this.pages = pages;
        
        setTitle(title);
        setAuthor(author);
    }
    
    public int getPages() {
        return pages;
    }
    
    public void setPages(int pages) {
        this.pages = pages;
    }
    
}

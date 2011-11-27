package ma.glasnost.orika.examples.library.model;

public class AudioFile extends Media {
    
    private int minutes;
    private int secondes;
    
    public AudioFile(String title, Author author, int minutes, int secondes) {
        this.minutes = minutes;
        this.secondes = secondes;
        
        setTitle(title);
        setAuthor(author);
    }
    
    public int getMinutes() {
        return minutes;
    }
    
    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
    
    public int getSecondes() {
        return secondes;
    }
    
    public void setSecondes(int secondes) {
        this.secondes = secondes;
    }
    
}

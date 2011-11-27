package ma.glasnost.orika.examples.library.dto;

public class AudioFileDTO extends MediaDTO {
    
    private int minutes;
    private int secondes;
    
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
    
    @Override
    public String toString() {
        return "AudioFileDTO [duration=" + minutes + ":" + secondes + ", title=" + getTitle() + ",author=" + getAuthor() + ", category="
                + getCategory() + "]";
    }
    
}

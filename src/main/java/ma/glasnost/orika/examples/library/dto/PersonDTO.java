package ma.glasnost.orika.examples.library.dto;

public class PersonDTO extends AuthorDTO {
    
    private String firstName;
    private String lastName;
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    @Override
    public String toString() {
        return "PersonDTO [firstName=" + firstName + ", lastName=" + lastName + "]";
    }
    
}

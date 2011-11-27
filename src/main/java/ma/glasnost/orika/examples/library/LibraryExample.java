package ma.glasnost.orika.examples.library;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.examples.library.dto.MediaDTO;
import ma.glasnost.orika.examples.library.dto.ShelfDTO;
import ma.glasnost.orika.examples.library.model.AudioFile;
import ma.glasnost.orika.examples.library.model.Book;
import ma.glasnost.orika.examples.library.model.Category;
import ma.glasnost.orika.examples.library.model.Person;
import ma.glasnost.orika.examples.library.model.Shelf;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LibraryExample {
    
    public static void main(String[] args) {
        
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("classpath*:library-beans.xml");
        
        MapperFacade mapperFacade = beanFactory.getBean(MapperFacade.class);
        
        ShelfDTO shelf = mapperFacade.map(buildShelf(), ShelfDTO.class);
        
        System.out.println("Number of items in the shelf : " + shelf.getElements().size());
        System.out.println("======================================");
        
        for (MediaDTO media : shelf.getElements()) {
            System.out.println(media);
        }
        
    }
    
    private static Shelf buildShelf() {
        Shelf shelf = new Shelf();
        Person khalilGebran = new Person("Khalil", "Gebran");
        shelf.addElement(new Book("Le Prophet", khalilGebran, 200));
        Person houdaSaad = new Person("Houda", "Saad");
        shelf.addElement(new AudioFile("Mazal Nabgheek", houdaSaad, 3, 20));
        
        AudioFile elNass = new AudioFile("EL Nass", houdaSaad, 3, 45);
        elNass.setCategory(Category.FICTION);
        shelf.addElement(elNass);
        return shelf;
    }
}

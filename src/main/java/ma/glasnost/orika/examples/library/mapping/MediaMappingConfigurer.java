package ma.glasnost.orika.examples.library.mapping;

import static ma.glasnost.orika.metadata.ClassMapBuilder.map;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.examples.common.MappingConfigurer;
import ma.glasnost.orika.examples.library.dto.AudioFileDTO;
import ma.glasnost.orika.examples.library.dto.AuthorDTO;
import ma.glasnost.orika.examples.library.dto.BookDTO;
import ma.glasnost.orika.examples.library.dto.PersonDTO;
import ma.glasnost.orika.examples.library.dto.ShelfDTO;
import ma.glasnost.orika.examples.library.model.AudioFile;
import ma.glasnost.orika.examples.library.model.Author;
import ma.glasnost.orika.examples.library.model.Book;
import ma.glasnost.orika.examples.library.model.Person;
import ma.glasnost.orika.examples.library.model.Shelf;

import org.springframework.stereotype.Component;

@Component
public class MediaMappingConfigurer implements MappingConfigurer {
    
    public void configure(MapperFactory factory) {
        configureAuthor(factory);
        configureMedia(factory);
        // configureAuthor(factory);
        // configureAuthor(factory);
        // configureAuthor(factory);
        // configureAuthor(factory);
        // configureAuthor(factory);
        
    }
    
    private void configureMedia(MapperFactory factory) {
        factory.registerClassMap(map(AudioFile.class, AudioFileDTO.class).byDefault().toClassMap());
        factory.registerClassMap(map(Book.class, BookDTO.class).byDefault().toClassMap());
        factory.registerClassMap(map(Shelf.class, ShelfDTO.class).byDefault().toClassMap());
        
    }
    
    private void configureAuthor(MapperFactory factory) {
        factory.registerClassMap(map(Person.class, PersonDTO.class).byDefault().toClassMap());
        factory.registerClassMap(map(Author.class, AuthorDTO.class).byDefault().toClassMap());
    }
}

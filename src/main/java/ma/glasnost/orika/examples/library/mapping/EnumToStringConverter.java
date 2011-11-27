package ma.glasnost.orika.examples.library.mapping;

import ma.glasnost.orika.converter.Converter;

import org.springframework.stereotype.Component;

@Component
public class EnumToStringConverter implements Converter<Enum<?>, String> {
    
    public boolean canConvert(Class<Enum<?>> sourceClass, Class<? extends String> destinationClass) {
        return sourceClass.isEnum() && String.class.equals(destinationClass);
    }
    
    public String convert(Enum<?> source, Class<? extends String> destinationClass) {
        return source.toString();
        
    }
    
}

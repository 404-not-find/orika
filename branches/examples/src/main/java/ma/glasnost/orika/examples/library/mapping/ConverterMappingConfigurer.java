package ma.glasnost.orika.examples.library.mapping;

import java.util.HashSet;
import java.util.Set;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.Converter;
import ma.glasnost.orika.converter.builtin.StringToEnumConverter;
import ma.glasnost.orika.examples.common.MappingConfigurer;

import org.springframework.stereotype.Component;

@Component
public class ConverterMappingConfigurer implements MappingConfigurer {
    
    private final Set<Converter<Object, Object>> converters;
    
    public ConverterMappingConfigurer(Set<Converter<Object, Object>> converters) {
        this.converters = converters;
    }
    
    public ConverterMappingConfigurer() {
        converters = new HashSet<Converter<Object, Object>>();
    }
    
    public void configure(MapperFactory factory) {
        factory.getConverterFactory().registerConverter(new StringToEnumConverter());
        if (converters != null) {
            for (Converter<Object, Object> converter : converters) {
                factory.getConverterFactory().registerConverter(converter);
            }
        }
    }
    
}

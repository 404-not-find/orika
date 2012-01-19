package ma.glasnost.orika.examples.common;

import java.util.Set;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class MapperFacadeFactoryBean implements FactoryBean<MapperFacade> {
    
    private final Set<MappingConfigurer> configurers;
    
    public MapperFacadeFactoryBean(Set<MappingConfigurer> configurers) {
        super();
        this.configurers = configurers;
    }
    
    public MapperFacade getObject() throws Exception {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        
        for (MappingConfigurer configurer : configurers) {
            configurer.configure(mapperFactory);
        }
        
        mapperFactory.build();
        
        return mapperFactory.getMapperFacade();
        
    }
    
    public Class<?> getObjectType() {
        return MapperFacade.class;
    }
    
    public boolean isSingleton() {
        return true;
    }
    
}

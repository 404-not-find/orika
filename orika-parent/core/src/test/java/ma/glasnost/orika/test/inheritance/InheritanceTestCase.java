package ma.glasnost.orika.test.inheritance;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.MapperFacade;

import org.junit.Test;

public class InheritanceTestCase {
    
    public static class BaseEntity {
        private Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
    }
    
    public static class BaseDTO {
        private Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
    }
    
    public static class ChildEntity extends BaseEntity {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static class ChildDTO extends BaseDTO {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    @Test
    public void testSimpleInheritance() {
        MapperFactory factory = new DefaultMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        ChildEntity entity = new ChildEntity();
        entity.setId(1L);
        entity.setName("Khettabi");
        
        ChildDTO dto = mapper.map(entity, ChildDTO.class);
        
        Assert.assertEquals(entity.getId(), dto.getId());
        Assert.assertEquals(entity.getName(), dto.getName());
    }
}

package ma.glasnost.orika.test.collection;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.MapperFacade;

import org.junit.Assert;
import org.junit.Test;

public class ArrayTestCase {
    
    @Test
    public void testMappingArrayOfString() {
        MapperFactory factory = new DefaultMapperFactory();
        MapperFacade mapper = factory.getMapperFacade();
        
        Product p = new Product();
        p.setTags(new String[] { "music", "sport" });
        
        ProductDTO productDTO = mapper.map(p, ProductDTO.class);
        
        Assert.assertEquals(p.getTags(), productDTO.getTags());
    }
    
    public static class Product {
        
        private String[] tags;
        
        public String[] getTags() {
            return tags;
        }
        
        public void setTags(String[] tags) {
            this.tags = tags;
        }
        
    }
    
    public static class ProductDTO {
        
        private String[] tags;
        
        public String[] getTags() {
            return tags;
        }
        
        public void setTags(String[] tags) {
            this.tags = tags;
        }
        
    }
}

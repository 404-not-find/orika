package ma.glasnost.orika.benchmarks.comparison;

import java.io.File;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.benchmarks.TestClasses.Product;
import ma.glasnost.orika.benchmarks.TestClasses.ProductDto;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.junit.Ignore;
import org.junit.Test;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

public class SimpleMapperComparisonBenchmark extends SimpleBenchmark {
	
	
    private Product createProduct() {
        Product product = new Product();
        product.setAvailability(true);
        product.setPrice(123d);
        product.setProductDescription("desc");
        product.setProductName("name");
        return product;
    }
    
    public int timeDozer(int reps) {
        int dummy = 0;
        
        Mapper mapper = new DozerBeanMapper();
        
        for (int i = 0; i < reps; i++) {
            Product product = createProduct();
            
            ProductDto dto = mapper.map(product, ProductDto.class);
            
            dummy = dto.hashCode();
        }
        return dummy;
    }
    
    public int timeOrika(int reps) {
        int dummy = 0;
        
        MapperFactory mapperFactory = new
                DefaultMapperFactory.Builder().build();
        mapperFactory.registerClassMap(ClassMapBuilder.map(Product.class,
                ProductDto.class).byDefault().toClassMap());
        mapperFactory.build();
        MapperFacade facade = mapperFactory.getMapperFacade();
        for (int i = 0; i < reps; i++) {
            Product product = createProduct();
            
            ProductDto dto = facade.map(product, ProductDto.class);
            
            dummy = dto.hashCode();
        }
        
        return dummy;
    }
    
    public int timeHandMapping(int reps) {
        int dummy = 0;
        
        for (int i = 0; i < reps; i++) {
            Product product = createProduct();
            
            ProductDto dto = new ProductDto();
            dto.setAvailability(product.getAvailability());
            dto.setProductDescription(product.getProductDescription());
            dto.setPrice(product.getPrice());
            dto.setProductName(product.getProductName());
            
            dummy = dto.hashCode();
        }
        
        return dummy;
    }
    
    private String[] args(String...arguments) {
        return arguments;
    }
    
    @Test
    @Ignore
    public void doComparison() {
        File outputFile = new File(getClass().getClassLoader().getResource("").getFile() + "caliper-results.xml");
        Runner.main(SimpleMapperComparisonBenchmark.class, args("-Dtrials=10,100,1000", "--saveResults", outputFile.getAbsolutePath()));
    }
    
}

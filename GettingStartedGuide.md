## _Some of this information may be outdated_ ##
## _Check out our new [User Guide](http://orika-mapper.github.com/orika-docs/)_ ##

---


# Getting Started Guide #

_This guide is very brief and incomplete. Feel free to fix or improve it_

## Installation ##
For maven users, the easiest way to get going is to add the following dependency to your project.

_Please verify the latest version_:


```
<dependency>
   <groupId>ma.glasnost.orika</groupId>
   <artifactId>orika-core</artifactId>
   <version>1.4.0</version><!-- latest version -->
</dependency> 
```

If you do not use maven please download the latest version of the archive zip with dependencies and put it on your classpath.


Once your project is configured you can begin the following tutorial.

### 3rd Party Dependencies ###

Orika use javassist to generate byte-code dynamically. If you're downloading and installing the compiled jar in your local m2 repository rather than building from source using maven, you will not get any of the transitive dependencies



## Tutorial ##

### Setup the mapping factory ###

The get an instance of MapperFacade initialise a MapperFactory:

```
MapperFactory factory = new DefaultMapperFactory.Builder().build();
factory.registerClassMap(factory.classMap(Order.class,OrderDTO.class)
  .field("product.state.type.label", "stateLabel")
  .field("product.name", "productName").toClassMap());
        
MapperFacade mapper = factory.getMapperFacade();
```

**Alternative Configuration/Setup Option (since 1.1.2)**:

Extend **`ma.glasnost.orika.impl.ConfigurableMapper`**, overriding the **`configure(MapperFactory mapperFactory)`** method to register any class-maps, converters, etc. Since ConfigurableMapper implements MapperFacade, you can then simply construct a new instance (or have one injected) of your extension class and use it as a mapper (your custom configure method will be called during initialization).
```
public class MyCustomMapper extends ConfigurableMapper {

   @Override
   public void configure(MapperFactory mapperFactory) {
      // register class maps, Mappers, ObjectFactories, and Converters
      ...
      mapperFactory.registerClassMap(...)
      mapperFactory.getConverterFactory().registerConverter(...)
      ...

   }
   @Override
   public void configureFactoryBuilder(DefaultMapperFactory.Builder builder) {
      // configure properties of the factory, if needed
   }
}

public class SomeOtherClass {

   private MapperFacade mapper = new MyCustomMapper();
 
   void someMethod() {
       
      mapper.map(blah, Blah.class);
      ...
   }
   ...
}

```
_This method works particularly well with Spring; just wire up your extension class as a singleton_
```
   <bean id="mapper" class="org.example.MyCustomMapper" />
```

### Reference ###

In this example we will show you how to map from a class A to another class B and vice versa.
Let’s have a look at our domain model:

**Product (A) Source :**
```
public class Product {

    private String productName;

    private String productDescription;

    private Double price;

    private Boolean availability;

    // getters & setters
    
}
```

**ProductDto (B) Destination :**

```
public class ProductDto {

    private String productName;

    private String description;

    private BigDecimal price;

    private Boolean availability;

    // getters & setters
}
```

Note that we have explicitly chosen different and alike attributes names and types.

### Auto mapping ###

#### Map from Product to ProductDto ####

**Result (Orika-AutoMapping) :**

Orika will automatically find all attributes with the same name and compatible types and map them.

NB: Remember that, when declaring a custom mapper and/or a converter, you are overriding the Orika auto-mapping functionality temporarily; applying the byDefault() method to the ClassMapBuilder will apply default auto-mapping behvaior to the remaining properties.
This allows you to do any of the following:
  1. map all properties automatically
```
// register the auto-mapping function
mapperFactory.registerClassMap(mapperFactory.classMap(Product.class,ProductDto.class).byDefault().toClassMap());
```
  1. explicitly specify every property mapping
```
mapperFactory.registerClassMap(mapperFactory.classMap(Order.class,OrderDTO.class)
  .field("product.state.type.label", "stateLabel")
  .field("product.name", "productName").toClassMap());
```
  1. customize some properties, and accept automatic behavior for the rest
```
mapperFactory.registerClassMap(mapperFactory.classMap(Order.class,OrderDTO.class)
  .field("product.state.type.label", "stateLabel")
  .field("product.name", "productName")
  .byDefault().toClassMap());
```

### Custom Mapper ###

**Let’s take now a closer look at our Dto:**

The description field in our case has nothing to do with the former one from the product class, this field is now a set of information collected from other fields, let’s say that the description should look something like this:
**Description example: The Dreamcast is a fabulous product which only cost 150** Description in java: ` "The " + product.getProductName() +" is a fabulous product which only cost " + product.getPrice() `

For specific field we have specific treatment, in this case we will create and register a custom mapper :

```
// register your custom mapper
ClassMapBuilder<ProductDto, Product> builder = mapperFactory.classMap(ProductDto.class, Product.class);
builder.customize(new CustomMapper<ProductDto, Product>() {
    
   // create your custom mapper                
   @Override
   public void mapBtoA( Product b,  ProductDto a, MappingContext  context) {
      a.setDescription("The " + b.getProductName() + " is a fabulous  product which only cost  " + b.getPrice());

   }});
mapperFactory.registerClassMap(builder.byDefault().toClassMap());

```

_Note that an instance of the current `MapperFacade` is available within your `CustomMapper` class as the protected field `mapperFacade`._

### Converter ###

Let’s move now to the price, in our case we want to format the price:
  * Price in our product class is a Double
  * Price in our Dto class is a BigDecimal
For this case we need to create a converter:

```
// register your converter
mapperFactory.getConverterFactory().registerConverter(new Converter<Double, String>() {
    public BigDecimal convert(Double source, Type<? extends BigDecimal> destinationType) {
        return new BigDecimal(source);
    }
    public boolean canConvert(Type<Double> sourceType, Type<? extends BigDecimal> destinationType) {
        return Double.class.equals(sourceType.getRawType()) && BigDecimal.class.equals(destinationType.getRawType());
    }
});
```

In this example our converter was anonymous, and Orika use the converter's `canConvert` to check if it can be used for a given mapping.

For this kind of simple type conversion, Orika provides a less verbose solution. You can just subclass the builtin `CustomConverter`:

```
// register your converter
mapperFactory.getConverterFactory().registerConverter(new CustomConverter<Double, String>() {
    public BigDecimal convert(Double source, Type<? extends BigDecimal> destinationClass) {
        return new BigDecimal(source);
    }
});
```

In some cases you may want to be more specific about when a converter is applied; you can specify a converter at field mapping scope
To use a field mapping scoped converter,  the converter is registered with a string identifier; you can then specify that converter by id when declaring a field-mapping.

```
ConverterFactory converterFactory = factory.getConverterFactory();
        converterFactory.registerConverter("dateConverter1", new DateToStringConverter("dd/MM/yyyy"));
converterFactory.registerConverter("dateConverter2", new DateToStringConverter("dd-MM-yyyy"));
        
factory.registerClassMap(factory.classMap(A.class, B.class).fieldMap("date").converter("dateConverter1").add().toClassMap());
factory.registerClassMap(factory.classMap(A.class, C.class).fieldMap("date").converter("dateConverter2").add().toClassMap());
```

In the last snippet, two converters are registered using two identifiers: ("dateConverter1" and "dateConverter2") and are specified on two different field mappings.

In the case of a two-way mapping, it is necessary to use a bidirectional converter. (In the previous example the `DateToStringConverter` is built-in bidirectional converter).
### Mapping fields with different name ###


Is an easy task--just point out the name of the attribute in class A and the supposed match in class B; keep in mind that the mapped attributes should be of an automatically-convertible type, otherwise you'll need to register a converter.

```
mapperFactory.registerClassMap(
   mapperFactory.classMap(Product.class,ProductDto.class)
      .field("productDescription", "description")
      .byDefault()
      .toClassMap()
   );
```

### Customize the default field-name mapping ###
In some cases, you may have a particular pattern that applies to the attributes to be mapped; or you might have some special property name mappings which are common to your whole object graph.
You can avoid declaring multiple explicit class (and field) mappings (if you wish) by registering a `DefaultFieldMapper` on the `MapperFactory`.

For example, suppose all of the properties on a class A have names like "name", "title", "age", etc., but the corresponding properties on another class B have names like "myName", "myTitle", "myAge", etc.
```
DefaultFieldMapper myDefaultMapper = new DefaultFieldMapper() {
   public String suggestMapping(String propertyName, Type<?> fromPropertyType) { 
      if(propertyName.startsWith("my")) {
         // Remove the "my" prefix and adjust camel-case
         return propertyName.substring(2,1).toLowerCase() +
               propertyName.substring(3);
      } else {
         // Add a "my" prefix and adjust camel-case
         return "my" + propertyName.substring(0,1).toUpperCase() +
               propertyName.substring(1);
      }   
   }
}

mapperFactory.registerDefaultFieldMapper(myDefaultMapper );
```
By using this default field mapper, you could automatically map from A to B without any explicit class mapping declaration; or you could combine with your own explicit mappings for some fields in the usual manner.

The `DefaultFieldMapper`s registered are applied whenever the internal auto-mapping functionality is triggered, and are applied only _after_ a direct one-to-one mapping for the attribute has failed. This means that if you have any attribute names which **_do_** match exactly, the hints would not override them; hints are used to provide extra 'guesses' when the name doesn't match.

You can also pass any default field mappers you've defined (as var-args) into the **byDefault()** method when using ClassMapBuilder to build a class mapping; like so:
```
DefaultFieldMapper myHint = ... // same hint as defined above

mapperFactory.registerClassMap(
   mapperFactory.classMap(A.class,B.class)
      .byDefault(myHint)
      .toClassMap()
   );
```
### Disabling auto-mapping altogether ###
To tell Orika that it should not auto-generate any mappers at runtime, you can apply the `useAutoMapping(false)` method on the DefaultMapperFactory.Builder, like so:
```
new DefaultMapperFactory.Builder().useAutoMapping(false).build();
```
This will only allow generation of mappers before the first time you call `getMapperFacade()`, which means that Only mappers you have registered will be auto-generated.

### Nested property ###


In some cases the class we’re mapping from is not a mirror of the one we’re mapping to. A nested property expression can be used to access attributes at different levels/depth.

Example:
```
public class Order {
   private Product product;

   // getter/setter
}

public class Product {
   
   private String productName;
   
   // getter/setter
}

public clsss OrderDTO {

   private String orderNum;

   // getter/setter
}
```

Our Dto diagram contains a new attribute the orderNum which is in our case the product name.
```
// register the nested property expression
mapperFactory.registerClassMap(
   mapperFactory.classMap(Order.class,OrderDto.class)
      .field("product.productName", "orderNum")
      .byDefault()
      .toClassMap()
   );
```

### Collections ###

In reality an order contains a set of products:
```
// register product and order
mapperFactory.registerClassMap(mapperFactory.classMap(Product.class,
ProductDto.class).field("productDescription", "description")
.byDefault().toClassMap());

mapperFactory.registerClassMap(mapperFactory.classMap(Order.class,
OrderDto.class).field("products", "productsDto").byDefault()
.toClassMap());

```

You have guessed right, there is no special treatment for collections case, since it’s implicit, Orika will automatically detects the collection inside your source object and map it to the destination.

### Bi-directional mapping ###

Let’s have a look at our bi-directional domain,

Our dto model :

```
BookDto bookDto = mapperFacade.map(book, BookDto.class);
```

As you can see, nothing has changed, and of course the destination object will have bi-directional references as well.

### Primitives ###

Orika will map from primitives to their respective wrapper and vice versa automatically.
Orika also automatically handles mappings for primitives (and their respective wrapper types) to and from `java.lang.String` whenever possible.

### Immutable Types ###

Orika has a built-in set of types it will treat as immutable (most of the primitive wrappers, String, etc.; the exact list (as of 1.1.9) is:
```
   Byte.class, Short.class, Integer.class, Long.class, Boolean.class,    
   Character.class, Float.class, Double.class, String.class,  
   BigDecimal.class, Date.class, java.sql.Date.class, Byte.TYPE,   
   Short.TYPE, Integer.TYPE, Long.TYPE, Boolean.TYPE, Character.TYPE, 
   Float.TYPE, Double.TYPE
```

To add your own types, just register a PassThroughConverter with any number of types you want to treat as immutable, like so:
```
mapperFactory.getConverterFactory().registerConverter(
   new PassThroughConverter(MyType1.class, MyType2.class, MyType3.class));
```

You can pass in any number of Class<?> or Type<?> instances when constructing the PassThroughConverter; these types will then be "passed through" like immutable types instead of mapping their individual properties.

### Enumerations ###

Orika will map between instances of the same Enum automatically.
Orika will also map from one Enum type to another so long as a matching instance name is found; if the appropriate mapping can't be determined by instance name, you can resolve this by registering a converter.

### Inheritance ###

Orika will auto-detect mappers defined for parent objects, and will attempt to reuse them in child class mappings.

Orika will also attempt to use an existing mapping from a parent or interface if a mapper for a child cannot be created. This means that if you have a defined mapping for `A` to `B`, and Orika is asked to map `A1` to `B`(where `A1` extends `A`, but cannot mapped directly--such as when `A1` is inaccessible to the current class-loader), Orika will use the declared mapping for `A` to `B`.

### Generic Types ###
As of version 1.1.0, all of the Orika map methods support type-specific versions which allow exact specification of the source type and destination type.

These new method signatures use the special `Type<?>` class to specify the generic information needed at runtime to exactly identify parameterized type(s).

For example, suppose you have the following domain (getters/setters omitted):
```
class Envelope<T>
   private T contents;
}

class Container<T>
   private T contained;
}

class Holder<T> {
   private T held;
}

class Entry<K, V> {
   private K key;
   private V value;
}
```
And you'd like to map from instances of: `Entry<Container<Holder<Long>>, Envelope<Container<String>>`
to instances of: `Entry<Holder<String>, Container<String>>`:

This would not normally be possible with the Class-based methods, because there is not enough information available (at runtime, after java erasure) to determine the nested properties of the objects.

But we can accomplish it by explicitly telling Orika both the source and target types to use:
```
/*
 * Register the class-mapping as usual, using the Type<?> instances instead
 * of Class values
 */
Type<Entry<Container<Holder<Long>>, Envelope<Container<String>>> fromType = new TypeBuilder<Entry<Container<Holder<Long>>, Envelope<Container<String>>>(){}.build();
Type<Entry<Holder<String>, Container<String>>> toType = new TypeBuilder<Entry<Holder<String>, Container<String>>>(){}.build();

mapperFactory.registerClassMap(
   mapperFactory.classMap(fromType, toType)
      .field("key.contained.held", "key.held")
      .field("value.contents.contained", "value.contained")
      .toClassMap());

/*
 * Map using the specific types
 */
Entry<Holder<String>, Container<String>> result = mapperFactory.getMapperFacade().map(fromObject, fromType, toType);
```

_Note: this example is implemented in `GenericsTestCase`, for those looking at the source code_

#### Working with Type<?> ####
All of the new generics-supporting methods require using the custom `ma.glasnost.orika.metadata.Type<?>` class to specify the actual generic types. Instances of `Type<?>` can be obtained in 2 ways:
  * Use the `TypeBuilder`, construct a new anonymous instance, and call the `build()` method on it, like so:
```
Type<A<B,C>> type = new TypeBuilder<A<B,C>>(){}.build();

Type<A<B<C,D>,E> type2 = new TypeBuilder<A<B<C,D>,E>(){}.build();
```
  * Use one of the static `TypeFactory` methods, like so:
```
/* 
 * First argument is the raw type, following are 0..N parameter types,
 * which may be any instance of java.lang.reflect.Type, of which 
 * ma.glasnost.orika.metadata.Type is also an instance.
 */ 
Type<A<B,C>> type = TypeFactory.valueOf(A.class, B.class, C.class);

Type<A<B<C,D>,E> type2 = TypeFactory.valueOf(A.class, 
   TypeFactory.valueOf(B.class, C.class, D.class), E.class);

```

### Object factories ###

In some cases we want to control how new object instances are created; in such cases we can define and register a custom `ObjectFactory`.

Let’s take for instance a case where we have a Person with a default address that we want Orika to add automatically whenever facing a PersonDto.
Let’s introduce the person domain’s model:

Dto’s model :

```
public class PersonFactory implements ObjectFactory<PersonDto> {

    @Override
    public PersonDto create(Object source, Type<PersonDto> destinationType) {
        PersonDto personDto = new PersonDto();
        // set the default adress
        personDto.setAdressDto(new AdressDto("Morocco", "Casablanca"));
            return personDto;
    }
}

```

Your factory must implement Orika’s `ObjectFactory`, with a suitable  `create()` method which provides the custom instances.

```
mapperFactory.registerObjectFactory(new PersonFactory(),PersonDto.class);
```

_Don’t forget to register your Object factory._

Orika is designed with the idea that the default behavior should be sufficient for most scenarios.  It then offers customization points which can be used as needed to support what the defaults cannot.

### Proxy Instances ###

Orika will automatically handle proxy instances of java.lang.reflect.Proxy, as well as javassist and cglib style byte-code proxies. This handling is provided by using super-type resolution mechanism to find an accessible super-type (class or interface) from which to build a mapper.


_more specifics needed_


---


## Debugging Generated Mappers, Converters, etc. ##
### _Pre-requisites_ ###
If you're not getting the expected results out of Orika (or you want to contribute a fix or feature), you may wish to download the source and step through the mapping process to see the details.

To enable step-through debugging into the generated objects (which Orika is building at run-time), you'll need to include the **orika-eclipse-tools** module in your project, like so:
```
<dependency>
   <groupId>ma.glasnost.orika</groupId>
   <artifactId>orika-eclipse-tools</artifactId>
   <version>1.3.0</version><!-- please verify the latest version -->
</dependency> 
```
In addition, you'll also need to tell Orika you want to use Eclipse Jdt as the compiler strategy; there are 2 ways to do this, listed in order of precedence:

  1. Use the DefaultMapperFactory.Builder
```
MapperFactory factory = 
        new DefaultMapperFactory.Builder()
                .compilerStrategy(new EclipseJdtCompilerStrategy())
                .build();
// add mappers, hints, converters, etc...     
```
  1. Set the system property
```
// Maybe more convenient, but sets at a global level...
System.setProperty(OrikaSystemProperties.COMPILER_STRATEGY,EclipseJdtCompilerStrategy.class.getName());
```

### _Edit Source Lookup Path_ ###
If you're using Eclipse as your IDE, you may initially receive the "Source not found" window with a button labeled "Edit Source Lookup Path..." while trying to (debug) step into the source of a generated object. When you click this button, you'll need to add the location of the **compiler output folder** for your project as a **File System Folder** (not a Workspace Folder). This is due to the fact that source files will not actually exist until just before you're at the break-point and Eclipse will have a "stale" view of the workspace folder by that time.

If you're using an IDE other than Eclipse, the procedure should be similar. Note that although Orika is leveraging the Eclipse Jdt to format and compile the code, it resolves these from it's own dependencies, so if using Maven, there should be few differences.

If your project follows the Maven folder structure, this folder would be the **target/test-classes** folder by default. Check your project configuration build path to see the target location if you're unsure.

If not using Maven, you'll need to make sure the following Eclipse Jdt artifacts are added to the build path:

| **_jdt artifact_** | **_version_** |
|:-------------------|:--------------|
| core               | 3.5.2         |
| text               | 3.3.0         |
| commands           | 3.3.0         |
| runtime            | 3.3.100       |
| osgi               | 3.3.0         |
| common             | 3.3.0         |
| jobs               | 3.3.0         |
| registry           | 3.3.0         |
| preferences        | 3.2.100       |
| contenttype        | 3.2.100       |

_Will other versions work (such as the latest from your own Eclipse IDE)? Probably...but we haven't confirmed this yet_

### _Security Exception for Eclipse jar signing_ ###
If you receive an exception like "'org.eclipse.core.runtime.ListenerList''s singing information doesn't match..., try moving the eclipse binaries to the front of the classpath; if you're using maven, just move the **orika-eclipse-tools** dependency before **orika-core** and you're done.

### _Generate Source and/or Class Files_ ###
When using Javassist (the default) as the compiler strategy, Orika will not generate source or class files by default. When using Eclipse Jdt, Orika will generate source but not class files by default.

This behavior can be customized by setting some special system properties, like so:
```
// Write out source files to (classpath:)/ma/glasnost/orika/generated/
System.setProperty(OrikaSystemProperties.WRITE_SOURCE_FILES,"true");

// Write out class files to (classpath:)/ma/glasnost/orika/generated/
System.setProperty(OrikaSystemProperties.WRITE_CLASS_FILES,"true");
```

Note that when using Javassist, the debug info is not being generated  so you can look at the code, but you normally _can't_ step into it during a debug session.

You may also need to apply the auto-formatting feature of your favorite IDE to the Javassist strategy source as we're not really shooting for style points on these (normally in-memory only) classes.

The Eclipse strategy generated files includes auto-formatting of the source and includes debug info so that you can step into the code during a debug session (as mentioned in the section above).

---

## Summary ##

Orika attempts to provide a relatively simple and painless means to map beans from one object graph to another, while also providing performance very close to coding by hand.

It does this by generating and reusing byte-code for mappers, avoiding most of the extra processing that comes from relying on reflection.

We're not attempting to discuss whether or not to use DTOs, or whether you _should_ perform mapping between object graphs. We _are_ going to illustrate some cases in which you may need to do this mapping, and where we hope Orika can help, such as:
  * Multi layered applications
  * DDD you need for example a data mapper between bounded context
  * Converting XML binding objects to UI beans
## Should I write unit tests to confirm that Orika mapped my objects successfully? ##

Of course! If you mapped these objects by hand, you would write a unit test, right? So you're just going to trust us that we mapped your objects the way you expected us to? That's giving Orika and it's authors a little too much credit. Please write tests to confirm the objects are mapped as you expect.

## What types of data objects are supported? ##

Orika uses reflection to access data object properties, and by default is designed to read object properties that conform to the JavaBeans standard. This means that an object with 'getName' and/or 'setName' methods would be said to have a property called 'name'; the 'setXXX' method is not required, and for (primitive) boolean return types, the 'getXXX' method can optionally be named 'isXXX' instead.
Orika takes this a little bit further, including java.lang.Boolean types for the optional 'isXXX' naming, and Orika will also recognize public fields as properties to be used.


## Will Orika convert data types? ##

Yes. Most scenarios are supported by default, including primitives, primitive wrappers, Enums, Collections, Arrays, Maps, and Complex types.
There are an additional set of converters for numeric and date types which can be enabled by calling the useBuiltinConverters(true) on DefaultMapperFactory.Builder.
Additionally, it is straightforward to register a custom mapper (by `extending ma.glasnost.orika.CustomMapper`) to deal with a conversion that is not built-in.

## How does Orika handle immutable types? ##

Orika has a base set of types which it treats as immutable, i.e., that are copied by reference from the source to the destination. Specifically, that list includes all of the primitive types and their corresponding wrapper classes, String, BigDecimal, etc.
User-defind/declared immutable types are possible via the registration of a special Converter (PassThroughConverter).

## Does Orika automatically map fields with the same property name? ##

Yes; when a mapping is requested for 2 types which have not been explicitly registered. When registering an explicit mapping from one type to another, Orika will do this only if the 'byDefault' method is called on the fluent-style ClassMapBuilder API.

## Is the default mapping behavior configurable? ##

Yes. Any number of DefaultFieldMapper instances can be defined/registered which are consulted for a default mapping, in the case where a matching field could not be found for a given property. Additionally, the default mapping behavior can be completely customized by extending ClassMapBuilder (see [issue#33](https://code.google.com/p/orika/issues/detail?id=#33)).


## Will Orika map recursively? ##

Yes. Orika will continue to map nested classes until mapping is completed with a "simple" type. It also contains fail-safes to properly deal with recursive references within the objects you're attempting to map.


## How does Orika perform the actual mapping? ##

Orika uses reflection to investigate the metadata of the objects to be mapped and then generates byte-code at runtime which is used to map those classes. This means that although some initial overhead is incurred to investigate the types and generate the mappers, the overall runtime performance is comparable to mapping by hand.


## Are Collections and Arrays and Maps supported? ##

Yes. Orika automatically maps between compatible multi-occurrence types (Arrays and java.util.Collection types, or between Map types) and automatically performs the necessary type conversions; mapping between Arrays, Collections, and Maps can be declared using the ClassMapBuilder API.


## Is class inheritance supported? ##

Yes. Orika will reuse mappers already defined for a given class hierarchy, and you can customize it using `ClassMapBuilder.use`

## Are generics supported? ##

Yes. Orika includes special runtime support for the mapping of generic types via a special Type<?> class which can be used to define the exact type elements of a templated type.

## Can Orika map to interfaces and abstract types? ##

Yes. Orika has support for the registration of a "concrete" type for a given abstract/interface type, including a default registration for the common Map and Collection types.

## How is Orika mapping configured? ##

Orika mapping is configured via Java code at runtime, via a fluent-style ClassMapBuilder API. An instance is obtained from a MapperFactory which is then used to describe the mapping from one type to antoher. Finally, the specified ClassMap is registered with the MapperFactory for later use in generating a byte-code mapper.
The recommended way to configure orika is by extending `ma.glasnost.orika.impl.ConfigurableMapper`, and providing any necessary customizations/configurations in the `configure` and `configureFactoryBuilder` methods; an instance of that custom mapper can then be safely shared as a singleton.

## Can Orika be used with Spring? ##
Yes. The best option is to define your own sub-class of ConfigurableMapper (as mentioned above) in singleton scope.


## When do I need to explicitly register mapping for 2 types? ##

Ideally, most of your field mappings can be performed automatically;  for the exceptions, a custom mapping can be registered.
Using an extension of ConfigurableMapper (as mentioned above), the configure() method is where this should be done.

## Is an explicitly registered mapping definition bi-directional, or do I need to define it in both directions? ##

Yes; registered (and automatically generated) mappings are bi-directional by default.

## What if I want some of the mappings require different definitions in each direction? ##

When defining a mapping, instead of using the `field("fieldA","fieldB")` method, use one of `fieldAToB("fieldA","fieldB")` or `fieldBToA("fieldB","fieldA")` to register a mapping in a single direction.

## How can I tell what Orika is doing at runtime? (Which mappers are registered, how did it resolve my request to a particular mapper/converter/etc., when and how did it auto-generate a mapping?) ##
Simply enable DEBUG logging for "ma.glasnost.orika" logger; Orika uses the Slf4J logging API, so should be compatible with most logging implementations if using an appropriate logging bridge.

## Which version JDK versions are supported? ##

1.5 and above.

## Is Orika available in the maven-central repository? ##

Yes.
```
    <dependency>
      <groupId>ma.glasnost.orika</groupId>
      <artifactId>orika-core</artifactId>
      <version>1.2.0</version>
    </dependency>
```
## Can I map an object contained in a collection or map to a field, or vise-versa? ##

Orika does not currently support this by default, but it can be achieved using a custom converter (by extending `ma.glasnost.orika.CustomConverter` or `ma.glasnost.orika.converter.BidirectionalConverter`).

## Can I map fields that don't have corresponding getter/setter methods, or call non-public constructors? ##

Orika will allow mapping of public fields as properties, but otherwise, the mapping of inaccessible fields is not supported.
Since Orika generates Java code to perform the actual mapping, it will generally follow the same restrictions of what you could write yourself.

## Are Enum types supported? ##

Yes. Enum instance are automatically mapped based on their instance values (when not of the same enumeration).

## When mapping generic Collections or Maps how do I tell Orika the values of the template parameters? ##

Use the ma.glasnost.orika.metadata.Type<?> construct to pass the runtime type into a given map method.

## I have a mapping case not supported by Orika--what are my options? ##

Orika mappings can be customized by creating your own custom Converters, Mappers, and ObjectFactory types. See the question below for information on what the different constructs are used for and which you might need.


## What is the difference between a Mapper, ObjectFactory, and Converter? ##
  * A **Mapper** is used for applying the properties of an object of one type to an object of another type.
  * An **ObjectFactory** is used for constructing an instance of a particular destination type, in the context of mapping from a particular source type.
  * A **Converter** is used to completely take over the mapping process for a particular set of types.


## How can I debug Orika? ##

To show debug logging information about the mappings used, generated, and the types resolved, set the "ma.glasnost.orika" package to DEBUG logging level.

To step-debug (in Eclipse or other IDE) into the actual generated classes, you'll first need to include the 'orika-eclipse-tools' jar in your project.
```
    <dependency>
      <groupId>ma.glasnost.orika</groupId>
      <artifactId>orika-eclipse-tools</artifactId>
      <version>1.2.0</version>
      <scope>test</scope>
    </dependency>
```
_Note that Orika makes use of Eclipse JDT independent of any containing Eclipse runtime environment_

We recommend including this at 'test' scope if using Maven. Then, you can enable use of the EclipseJdtCompilerStrategy.class by setting the following system property value:
> -Dma.glasnost.orika.compilerStrategy=ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy

This will tell Orika to use EclipseJdt to compile and format the generated mapper objects with debug information which is necessary for step-debugging.
Then, you'll need to add a File System folder to your debug source which includes your project's binary output location (if using Maven, this would be the 'target/classes' and 'target/test-classes' folders).
Note that it cannot be a workspace reference, since the generated classes won't be known to Eclipse until after the test VM has been launched.

## Can I use the EclipseJdt compiler strategy in production? ##

Yes. The reason it is not the default has mostly to do with size and number of additional jars required.
Runtime performance has been roughly equivalent to the Javassist strategy in tests thusfar, but you will want to disable the automatic writing of class and source files to disk by setting the following system properties (which default to 'true' in the EclipseJDt strategy):
> -Dma.glasnost.orika.writeSourceFiles=false
> -Dma.glasnost.orika.writeClassFiles=false

You will first need to make sure to include the 'orika-eclipse-tools' jar in your project's dependencies.
```
    <dependency>
      <groupId>ma.glasnost.orika</groupId>
      <artifactId>orika-eclipse-tools</artifactId>
      <version>1.2.0</version>
    </dependency>
```
To enable/specify this compiler strategy, the best option is to specify it in the `configureFactoryBuilder` method of your extension of `ConfigurableMapper`, like so:
```
   public void configureFactoryBuilder(DefaultMapperFactory.Builder builder) {
      builder.compilerStrategy(new EclipseJdtCompilerStrategy());
   }

```
A less-preferable option is to specify the system property (as mentioned in the above question about debugging Orika)
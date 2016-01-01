# Release Notes #
_by version_


---

Expected Release Date (2013-12-05):

## 1.4.4 (bugfix) ##
  * Added some javadocs, clean up code, fix tests
  * Added a copy of tccl instead of using the existing tccl to isolate from other tests.
  * Added explicit pre-loading of hidden types.
  * Removed separate alternate compiler strategy.
  * Migrated eclipse tools to use recent versions of eclipse jdt dependencies.
  * Fix [Issue 135](https://code.google.com/p/orika/issues/detail?id=135) : NPE while mapping to a nested property
  * Separated MultiLayeredClassloader into it's own suite, as it was being negatively affected by previously loaded classes
  * Fix  [Issue 134](https://code.google.com/p/orika/issues/detail?id=134) : Using eclipse compiler with classes in the default package.
  * Added optional deferred initialisation to allow subclass constructor arguments to be captured first.
  * Massive performance improvements for very large mappings
  * Added test case and fix for mapping non-assignable properties (in-place) if possible.
  * Added test case for [Issue 132](https://code.google.com/p/orika/issues/detail?id=132). Added ConstructorParameter, and ConstructorParameterResolver to help in resolving parameters from constructor.
  * Fix to debug logging for excluded fields.
  * Fix [Issue 128](https://code.google.com/p/orika/issues/detail?id=128)
  * Fix [Issue 126](https://code.google.com/p/orika/issues/detail?id=126) : To avoid throwing error in 'exclude' method if field does not exist in both types.
  * Fix [Issue 114](https://code.google.com/p/orika/issues/detail?id=114)
  * Fix [Issue 121](https://code.google.com/p/orika/issues/detail?id=121) : Added fixes to: Type: created new method isSelfOrAncestorParameterized, used by updated PropertyResol
  * Fix [Issue 119](https://code.google.com/p/orika/issues/detail?id=119)
  * Applied fixes to specifications for comparison generation. Added test cases for many-to-one and one-to-many mappings.
  * Added existsProperty method to allow checking for existence of a property without triggering exceptions.
  * Added handling for special one-to-many and many-to-one specifications.
  * Added one-to-many and many-to-one specs.
  * Fix to specification debug printing; restored field tag prefix was lost in a previous change.
  * Fix [Issue 130](https://code.google.com/p/orika/issues/detail?id=130) : Inline Property Syntax does not support primitives ...
  * Tighten type bounds on the parameters to filter methods.
  * Apply filters in multi-occurrence-to-multi-occurrence setting too.
  * Provide a NullFilter to simplify the implementation of simple filters.
  * Improve the information passed to Filter.shouldMap().
  * Provide more information when descending into nested objects.
  * Allow subclasses to determine the A- and B-types.
  * Fix NPE when destination in a comparison is null.
  * Fix [Issue 118](https://code.google.com/p/orika/issues/detail?id=118), Keep using unenhance strategy
  * Added fix to constructor resolution to avoid resolving constructors for which we can't map all the arguments with known source properties
  * Updated ObjectFactoryGenerator to check for mismatch of parameters to constructor args and throw a descriptive exception telling user t
  * Added fix for PersonMappingTestCase -- issue with properly detecting nested property when a element type is used
  * Enhance DateToStringConverter to handle localization.
  * Fix [issue 116](https://code.google.com/p/orika/issues/detail?id=116) : Remove SLF4J from orika-deps-included jar and add/relocate concurrentlinkedhashmap
  * Fix [Issue 113](https://code.google.com/p/orika/issues/detail?id=113)  Cannot find a mapper when a child bean infers a generic type of its parent bean
  * Fix [Issue 115](https://code.google.com/p/orika/issues/detail?id=115) : NPE in MultiOccurrence To MultiOccurrence mapping.
  * Fix Array or collection to array mapping specification.
  * Changed MappingContext constructor to public for better usability.
  * Added MappingContextFactory to UtilityResolver and to DefaultMapperFactory.Builder to allow users to customize the MappingContextFactor
  * Fix [Issue 112](https://code.google.com/p/orika/issues/detail?id=112), along with test case.
  * Added initial implementation of Filters feature



## 1.4.3 (bugfix) ##
Expected Release Date (2013-07-01):
  * [Issue 44](https://code.google.com/p/orika/issues/detail?id=44): Allow converters for Lists (or other collections).
  * [Issue 80](https://code.google.com/p/orika/issues/detail?id=80): There is a bug for JavaBean to Map
  * [Issue 85](https://code.google.com/p/orika/issues/detail?id=85): NullPointerException in MultiOccurrenceToMultiOccurrence.mapFields
  * [Issue 86](https://code.google.com/p/orika/issues/detail?id=86): Single directional default mapping
  * [Issue 87](https://code.google.com/p/orika/issues/detail?id=87): Nested properties: Allow abstract nested property's fields mapping
  * [Issue 90](https://code.google.com/p/orika/issues/detail?id=90): Simple mappings fail
  * [Issue 92](https://code.google.com/p/orika/issues/detail?id=92): NPE when mapping a class implementing a map
  * [Issue 96](https://code.google.com/p/orika/issues/detail?id=96): Constructors use and nested properties
  * [Issue 102](https://code.google.com/p/orika/issues/detail?id=102): Not able to set map type using orika
  * [Issue 105](https://code.google.com/p/orika/issues/detail?id=105): Using CaseInsensitiveClassMapBuilder for standard field mapping
  * [Issue 106](https://code.google.com/p/orika/issues/detail?id=106): Allow ObjectFactory to be registered by source and destination type
  * [Issue 107](https://code.google.com/p/orika/issues/detail?id=107): Custom mappers use unnecessary string concatenation
  * [Issue 108](https://code.google.com/p/orika/issues/detail?id=108): ma.glasnost.orika.impl.Comparators violates Comparator contract
  * [Issue 109](https://code.google.com/p/orika/issues/detail?id=109): Mapping for multi-occurrence elements doesn't trigger auto-generated mappings


---

## 1.4.2 (bugfix) ##
Release 1.4.2 (2013-05-16):

> _Fixes_
  * [Issue 100](https://code.google.com/p/orika/issues/detail?id=100): Add case-insensitive capability for mapping
  * [Issue 98](https://code.google.com/p/orika/issues/detail?id=98): ClassCastException when mapping list of subtypes
  * [Issue 97](https://code.google.com/p/orika/issues/detail?id=97): Concurrent Modification Exception
  * [Issue 95](https://code.google.com/p/orika/issues/detail?id=95): Orika sometimes fails to detect inacessible types -- results in IllegalAccessError
  * [Issue 94](https://code.google.com/p/orika/issues/detail?id=94): Mapping null values for nested fields does not work
  * [Issue 88](https://code.google.com/p/orika/issues/detail?id=88): 1.4.1 throws java.lang.NoSuchMethodError ConcurrentLinkedHashMap$Builder.maximumWeightedCapacity
  * [Issue 82](https://code.google.com/p/orika/issues/detail?id=82): MappingException on customization the default field-name mapping.


---

## 1.4.1 (bugfix) ##
Release 1.4.1 (2013-02-10):

> _Fixes_
  * [Issue 69](https://code.google.com/p/orika/issues/detail?id=69): Orika fails to map classes with lists which do not return internal references
  * [Issue 76](https://code.google.com/p/orika/issues/detail?id=76): Conversion between java.sql.Time and java.util.Date
  * [Issue 74](https://code.google.com/p/orika/issues/detail?id=74): Concurrent use of a non-threadsafe Map implementation
  * [Issue 77](https://code.google.com/p/orika/issues/detail?id=77): (Orika 1.4.0) A sub type is not beeing mapped to the configured mapping type


---

## 1.4.0 (feature) ##
Release 1.4.0 (2012-12-17):

  * Refactor of inline/adhoc property definitions; they now begin with ':{' and end with '}',
> and refactor of nested element expressions, which use '{ }'
> _Enhancements_
  * [Issue 62](https://code.google.com/p/orika/issues/detail?id=62): Add capability to ignore mapping of null values (instead of setting results NULL)

> _Fixes_
  * [Issue 53](https://code.google.com/p/orika/issues/detail?id=53): Dynamic mapping always uses existing parent mapper even if it doesn't map all fields
  * [Issue 64](https://code.google.com/p/orika/issues/detail?id=64): SimpleConstructorResolverStrategy ignores generic parameter types
  * [Issue 65](https://code.google.com/p/orika/issues/detail?id=65): Orika wraps user exceptions in MapperException
  * [Issue 71](https://code.google.com/p/orika/issues/detail?id=71): Orika maps enum to enum, but not List&lt;enum&gt; to List&lt;enum&gt;


---

## 1.3.5 (bug fix) ##
Release 1.3.5 (2012-10-21):

  * [Issue 61](https://code.google.com/p/orika/issues/detail?id=61):   StackOverflowError on circle association.



---

## 1.3.4 (bug fix) ##
Release 1.3.4 (2012-10-15):
  * [Issue 58](https://code.google.com/p/orika/issues/detail?id=58): Orika fails to map short to int, long to int, short to long, etc.
  * [Issue 59](https://code.google.com/p/orika/issues/detail?id=59): NPE thrown when mapping null list property on source to destination
  * [Issue 60](https://code.google.com/p/orika/issues/detail?id=60): Javolution dependency causing classloader problems on Websphere


---

## 1.3.0 (feature) ##
Release 1.3.0 (2012-10-04):

  * Performance Enhancement - BoundMapperFacade
> > This release includes a new BoundMapperFacade<A,B> which can be obtained from the MapperFactory
> > using `factory.getMapperFacade(Entity.class,Dto.class)`; this bound facade tends to complete
> > mappings in about 3/4th the time (on rough average) required by mapperFacade.map().
> > So if you're executing mapping operations against a known set of types, this is definitely the
> > route you want to take.


> In the case where you're mapping an object graph with no cycles (parent references child which references parent, or similar),
> then you can leverage a special "non-cyclic" version of the BoundMapperFacade which tends to
> complete mappings in about 1/4th the time (on rough average) required by mapperFacade.map().

> Note that the BoundMapperFacade pattern has been applied to the auto-generated mappers, which means
> that the overall performance is improved for each nested mapping (so this can be even greater improvement for
> if have multiple levels of nesting)

  * In-line Property Definition
> > It is now possible to define properties (with arbitrary getter/setter method) within an in-line ClassMapBuilder definition; here's an example:
```
factory.classMap(Entity.class, Dto.class)
   .field("customField{readTheCustomField|writeTheField}","field")
   ...
```
> > In this case, we're defining a property named "customField" with getter method "readTheCustomField"
> > and setter method "writeTheField".
> > Once you've defined an ad-hoc property, you can continue to reference it by the name you used to  define it, for example:
```
factory.classMap(Entity.class, Dto.class)
   .field("name{readTheName|writeName}.firstName","givenName")
   .field("name.lastName", "sirName")
   ...
```
> > Here, we've defined 'name' with getter 'readTheName' and setter 'writeTheName' (and used it within a
> > nested property), and then we re-use it in a following line (without re-defining the getter/setter).


> An in-line property with no setter is defined like this: "propertyName{getterName}", while an ad-hoc
> property with no getter is like this: "propertyName{|setterName}".

> Alternatively, in-line properties can also be specified as a java method call, even allowing for constant/static
> parameters to be passed, like so:
```
factory.classMap(Entity.class, Dto.class)
   .field("name{readTheNameIn(\"english\")|writeTheNameIn(\"english\", %s)|type=my.org.Name}.firstName","givenName")
   .field("name.lastName", "sirName")
```
> Note that a third argument (type={the type}) is allowed to specify the exact type of the property, since it will not be drawn from
> the read/write methods in this case. Type names passed in this way can be fully-qualified class names or valid generic
> type expressions. There is also some allowed assumption of "automatic imports" for 'java.lang' and 'java.util' packages
> for the types/sub-types specified this way, so `List<Long>` would be understood as `java.util.List<java.lang.Long>`.

  * Enhancements for extensibility of PropertyResolverStrategy
> > Now, your own custom implementation can be defined by extending from PropertyResolver (or one of the
> > other existing implementations) and implementing the 'collectProperties(...)' method.

  * Added new RegexPropertyResolver which can be configured to extend the default JavaBeans property discovery
> > with your own custom patterns for recognizing getter/setter methods.
> > An example configuration is as follows:
```
DefaultMapperFactory.Builder()
   .propertyResolverStrategy(
      new RegexPropertyResolver(
         "readThe([\\w]+)", "writeThe([\\w]+)Field", true, true))  
   .build();
```
> > Here, we've defined a regular expression for getters as "readThe([\\w]+)", and "writeThe([\\w]+)Field" for
> > the setters. This would discover a property named "address", based on finding a getter method named 'readTheAddress'
> > and a setter named 'writeTheAddressField'. In regular expression terms, there must be a group matched at
> > index (1), which is used as the name of the field.
> > The additional parameters (true, true) specify that JavaBeans properties should be supported and that
> > public fields should be supported, respectively.

  * Built-in Date/Numeric/String converters enabled by default
> > The enhancement to include the converters defined within BuiltinConverters is now enabled by default,
> > whereas, previously, it had to be explicitly enabled using `.useBuiltinConverters(true)` on the DefaultMapperFactory.Builder

  * Fix to converter resolution process;
> > Previously, converters were resolved according to non-deterministic ordering (HashSet lookup), which could result
> > in unexpected behavior when more than 1 converter is registered which is able to convert for a given type par.


> Now, the resolution process has been updated to use the same (most-specific-wins) strategy as is used to resolve
> mappers so the behavior is consistent, even when converters are registered in different order; if two converters
> with exact same set of types is registered, the first one registered wins (built-in converters are registered when
> the MapperFactory is first initialized, which is usually upon the first call to getMapperFacade().

  * Convenience 'register' method added to the ClassMapBuilder API
> > Now, to register a class map, the syntax is a bit simplified, to this:
```
mapperFactory.classMap(A.class, B.class)
   .field("name", "fullName")
   .field("address.street", "streetAddress")
   .field("age", "legalAge")
   .byDefault()
   .register(); 
```
> > For comparison, it used to be like this:
```
mapperFactory.registerClassMap(
   mapperFactory.classMap(A.class, Map.class)
      .field("name", "fullName")
      .field("address.street", "streetAddress")
      .field("age", "legalAge")
      .byDefault()
      .toClassMap());
```
> > Of course, the old syntax will still work (this is what is called under the covers anyway).

  * Capability to map from standard POJO types to Map
> > Now, a mapping can be defined which converts a POJO to a Map, using the normal mapping API, like so:
```
mapperFactory.classMap(A.class, Map.class)
   .field("name", "fullName")
   .field("address.street", "streetAddress")
   .field("age", "legalAge")
   .byDefault()
   .register();
```
> > This would map the 'name' property of class A to a value in the map keyed by "fullName", the nested 'address.street'
> > property to a value keyed by "streetAddress", and so on. The 'byDefault()' method in this case, would continue adding
> > the top-level properties of class A to the map, keyed by their property names.
> > Note that more specific Map types can be specified by passing a Type<?> value into the classMap method; when Map.class
> > is used, the type uses the default of Map<Object, Object>; an example of using a Map<String,String> is as follows:
> > > mapperFactory.classMap(TypeFactory.valueOf(A.class), new TypeBuilder<Map<String,String>>(){}.build())

> > .field(...)
> > In this case, it's best to define the map type once, since you'll need to pass it in as an argument to the 'map' method
> > (or the new 'getMapperFacade(Type<?>,Type<?>)' ) to assure that your specific Map type is used.

  * Capability to map from standard POJO types to List and Array
> > Now, a mapping can be defined which converts a POJO to a List or Array, using the normal mapping API, like so:
```
mapperFactory.classMap(A.class, List.class)
   .field("name", "0")
   .field("address.street", "1")
   .field("age", "2")
   .byDefault()
   .register();
```
> > This would map the 'name' property of class A to the 0-index value in the list/array, the nested 'address.street'
> > property to the 1-index value, and so on. The 'byDefault()' method in this case, would continue adding
> > the top-level properties of class A to the array/list, in the order of their declaration within class A.
> > As mentioned for Bean to Map (above), a more specific List type may be specified using the 'classMap(Type<?>,Type<?>)'
> > signature. Since arrays keep their component type at runtime, you can simply pass in the specific array type
> > to the standard 'map(Class<?>,Class<?>)'


---


## 1.2.2 (bug fix) ##
  * [Issue 52](https://code.google.com/p/orika/issues/detail?id=52); corrected additional problem found when used mappers were specified in flipped direction

## 1.2.1 (bug fix) ##
  * [Issue 33](https://code.google.com/p/orika/issues/detail?id=33): (enhancement) Improved extensibility of ClassMapBuilder
  * [Issue 46](https://code.google.com/p/orika/issues/detail?id=46): Class-cast exception for mapped objects
  * [Issue 48](https://code.google.com/p/orika/issues/detail?id=48): Exception on collection mapping
  * [Issue 49](https://code.google.com/p/orika/issues/detail?id=49): MappingException when enum toString() is overridden
  * [Issue 50](https://code.google.com/p/orika/issues/detail?id=50): Exclusions are ignored when combined with used mappers

---

## 1.2.0 (feature) ##
  * Improved debugging: detailed description of what was generated (and how) for registered and auto-generated mappers
    * example:
<pre>
08:56:01.928 [main] DEBUG m.g.o.impl.generator.MapperGenerator - Generating new mapper for (BookImpl, BookDTOWithAltCaseEnum)<br>
OrikaBookDTOWithAltCaseEnumBookImplMapper892899908.mapAToB(BookImpl, BookDTOWithAltCaseEnum) {<br>
Field(format(PublicationFormat), format(PublicationFormatDTOAltCase)) : using converter LegacyConverter(ma.glasnost.orika.test.enums.EnumsTestCase$1@150ac9a8)<br>
Field(title(String), title(String)) : treating as immutable (using copy-by-reference)<br>
}<br>
OrikaBookDTOWithAltCaseEnumBookImplMapper892899908.mapBToA(BookDTOWithAltCaseEnum, BookImpl) {<br>
Field(format(PublicationFormatDTOAltCase), format(PublicationFormat)) : mapping from String or enum to enum<br>
Field(title(String), title(String)) : treating as immutable (using copy-by-reference)<br>
}<br>
Types used: [PublicationFormatDTOAltCase]<br>
<br>
Converters used: LegacyConverter(ma.glasnost.orika.test.enums.EnumsTestCase$1@150ac9a8)]<br>
</pre>
  * Improved debugging: detailed description of class-mapping process for auto-generated mappers; now you can see what was mapped "`byDefault()`"
    * example:
<pre>
08:56:01.910 [main] DEBUG m.g.orika.metadata.ClassMapBuilder - ClassMap created:<br>
ClassMapBuilder.map(BookImpl, BookDTOWithAltCaseEnum)<br>
.field([format(PublicationFormat)], [format(PublicationFormatDTOAltCase)])<br>
.field([title(String)], [title(String)])</pre>
  * Improved debugging: details regarding the mapping 'strategy' chosen to map a particular set of inputs
    * example:
<pre>
08:56:01.929 [main] DEBUG m.g.orika.impl.MapperFacadeImpl - MappingStrategy resolved and cached:<br>
Inputs:[ sourceClass: ma.glasnost.orika.test.enums.EnumsTestCaseClasses.BookImpl, sourceType: BookImpl, destinationType: BookDTOWithAltCaseEnum]<br>
<br>
Resolved:[ strategy: InstantiateByDefaultAndUseCustomMapperStrategy, sourceType: BookImpl, destinationType: BookDTOWithAltCaseEnum, mapper: ma.glasnost.orika.generated.OrikaBookDTOWithAltCaseEnumBookImplMapper892899908@560c7816, mapInverse?: false]<br>
</pre>
  * Support for java.util.Map types
    * As properties of existing classes, for auto-generation
    * Via direct mapping methods ( mapAsMap(...), mapAsList(...) )
  * Support for direct registration of custom Mapper instances
    * example: `MapperFactory.registerMapper(Mapper mapper)`
  * Support for registration of concrete types to be instantiated for abstract classes or interfaces
    * example: `MapperFactory.registerConcreteType(anInterface, aConcreteType)`
  * Registered custom converters will now have access to an instance of the current MapperFacade (via a protected variable named `mapperFacade`)
  * Improved error reporting for mapping failures, giving better context information about the inputs of the mapping operation, and (if possible) the mapper/converter/etc. used
    * example:
<pre>
ma.glasnost.orika.MappingException: While attempting the folling mapping:<br>
sourceType = BigDecimal<br>
destinationType = Double<br>
Error occurred: No converter registered for conversion from BigDecimal to Double, nor any ObjectFactory which can generate Double from BigDecimal<br>
<br>
at ma.glasnost.orika.impl.DefaultMapperFactory.lookupMapper(DefaultMapperFactory.java:372)<br>
at ma.glasnost.orika.impl.MapperFacadeImpl.prepareMapper(MapperFacadeImpl.java:442)<br>
...<br>
</pre>
  * Support for extending ClassMapBuilder (enhancement for [Issue 33](https://code.google.com/p/orika/issues/detail?id=33))
  * Built-in converters for numeric and data/time types which can be enabled using `useBuiltinConverters(true)` on DefaultMapperFactory.Builder
  * Change to the way ClassMapBuilder instances are obtained; the old method, while convenient, resulted in a static member variable containing a PropertyResolverStrategy which is not desirable; now, ClassMapBuilders are obtained from the MapperFactory instance, like so:
```
   // new, preferred pattern
   mapperFactory.classMap(TypeA.class, TypeB.class)
      ...
   // old, deprecated pattern
   ClassMapBuilder.map(TypeA.class, TypeB.class)
```
  * Bug fixes for:
    * [issue 45](https://code.google.com/p/orika/issues/detail?id=45)
    * [issue 34](https://code.google.com/p/orika/issues/detail?id=34)

---


## 1.1.9 (bug fix) ##
  * [Issue 41](https://code.google.com/p/orika/issues/detail?id=41) : StackOverflowError for nested Enum
  * [Issue 38](https://code.google.com/p/orika/issues/detail?id=38) : Option to not set target object property when property value in the source object is null
  * [Issue 36](https://code.google.com/p/orika/issues/detail?id=36):  Cleanup orika-core pom.xml
  * [Issue 35](https://code.google.com/p/orika/issues/detail?id=35) : Remove compile-scope dependency to slf4j-simple

## 1.1.8 (bug fix) ##
  * [Issue 30](https://code.google.com/p/orika/issues/detail?id=30): MappingException: cannot determine runtime type of destination collection
  * [Issue 32](https://code.google.com/p/orika/issues/detail?id=32): ClassLoader leak via "strategyKey" ThreadLocal

## 1.1.7 (bug fix) ##
  * [Issue 28](https://code.google.com/p/orika/issues/detail?id=28): StackoverflowException on recursively-defined type

## 1.1.6 (bug fix) ##
  * [Issue 26](https://code.google.com/p/orika/issues/detail?id=26): Generic super-type not recognized
  * [Issue 27](https://code.google.com/p/orika/issues/detail?id=27): Occasionally the wrong Type is resolved for a class
  * fixes for multi-threading issues (simultaneous generation of mappers and object factories could occasionally fail)

## 1.1.5 (bug fix) ##
  * [Issue 21](https://code.google.com/p/orika/issues/detail?id=21): NPE when collection is changed. (failure to distinguish between mapping strategies for mapping in place and those for mapping to a new instance)
  * fix for NPE on TypeFactory.valueOf(null) when mapping using Class-based mapping methods with a null input value

## 1.1.4 (bug fix) ##
  * [Issue 20](https://code.google.com/p/orika/issues/detail?id=20): StackOverflowError mapping hibernate4 proxy.
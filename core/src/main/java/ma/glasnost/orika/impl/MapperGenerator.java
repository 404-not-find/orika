package ma.glasnost.orika.impl;

import static ma.glasnost.orika.impl.Specifications.aCollection;
import static ma.glasnost.orika.impl.Specifications.aPrimitiveToWrapper;
import static ma.glasnost.orika.impl.Specifications.aWrapperToPrimitive;
import static ma.glasnost.orika.impl.Specifications.anArray;
import static ma.glasnost.orika.impl.Specifications.immutable;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public final class MapperGenerator {

	private final MapperFactory mapperFactory;

	public MapperGenerator(MapperFactory mapperFactory) {
		this.mapperFactory = mapperFactory;
	}

	public GeneratedMapperBase build(ClassMap<?, ?> classMap) {

		ClassPool pool = ClassPool.getDefault();
		CtClass mapperClass = pool.makeClass("PA_" + System.identityHashCode(classMap) + Math.round(Math.random()) + "_Mapper");

		try {
			CtClass abstractMapperClass = pool.getCtClass(GeneratedMapperBase.class.getName());
			mapperClass.setSuperclass(abstractMapperClass);
			addGetTypeMethod(mapperClass, "getAType", classMap.getAType());
			addGetTypeMethod(mapperClass, "getBType", classMap.getBType());
			addMapMethod(mapperClass, true, classMap);
			addMapMethod(mapperClass, false, classMap);

			return (GeneratedMapperBase) mapperClass.toClass().newInstance();
		} catch (Exception e) {
			throw new MappingException(e);
		}
	}

	private void addMapMethod(CtClass mapperClass, boolean aToB, ClassMap<?, ?> classMap) throws CannotCompileException {
		CodeSourceBuilder out = new CodeSourceBuilder();
		String mapMethod = "map" + (aToB ? "AtoB" : "BtoA");
		out.append("public void ").append(mapMethod).append("(java.lang.Object a, java.lang.Object b, %s mappingContext) {",
				MappingContext.class.getName());

		Class<?> sourceClass, destinationClass;
		if (aToB) {
			sourceClass = classMap.getAType();
			destinationClass = classMap.getBType();
		} else {
			sourceClass = classMap.getBType();
			destinationClass = classMap.getAType();
		}
		out.assertType("a", sourceClass);
		out.assertType("b", destinationClass);

		out.append(sourceClass.getName()).append(" source = (").append(sourceClass.getName()).append(") a; \n");
		out.append(destinationClass.getName()).append(" destination = (").append(destinationClass.getName()).append(") b; \n");

		out.append("\nif(customMapper != null) customMapper.").append(mapMethod).append(
				"(source, destination, mappingContext);\n");

		for (FieldMap fieldMap : classMap.getFieldsMapping()) {
			if (!fieldMap.isExcluded()) {
				try {
					if (!aToB) {
						fieldMap = fieldMap.flip();
					}
					generateFieldMapCode(out, fieldMap);
				} catch (Exception e) {
					throw new MappingException(e);
				}
			}
		}

		out.append("\n}");

		System.out.println(out);
		mapperClass.addMethod(CtNewMethod.make(out.toString(), mapperClass));
	}

	private void generateFieldMapCode(CodeSourceBuilder code, FieldMap fieldMap) throws Exception {
		Property sp = fieldMap.getSource(), dp = fieldMap.getDestination();
		if (generateConverterCode(code, fieldMap)) {
			return;
		}
		try {
			if (fieldMap.getDestination().hasPath())
				return;
			if (fieldMap.is(immutable())) {
				code.ifSourceNotNull(sp).set(dp, sp);
			} else if (fieldMap.is(anArray())) {
				code.ifSourceNotNull(sp).then().setArray(dp, sp).end();
			} else if (fieldMap.is(aCollection())) {
				code.ifSourceNotNull(sp).setCollection(dp, sp);
			} else if (fieldMap.is(aWrapperToPrimitive())) {
				code.ifSourceNotNull(sp).setPrimitive(dp, sp);
			} else if (fieldMap.is(aPrimitiveToWrapper())) {
				code.ifSourceNotNull(sp).setWrapper(dp, sp);
			} else { /**/
				code.ifSourceNotNull(sp).setObject(dp, sp);
			}
		} catch (Exception e) {
			if (fieldMap.isConfigured())
				throw e;
			// elsewise ignore
		}
	}

	private boolean generateConverterCode(final CodeSourceBuilder code, final FieldMap fieldMap) {
		Converter<?, ?> converter = mapperFactory.lookupConverter(fieldMap.getSource().getType(), fieldMap.getDestination()
				.getType());
		if (converter != null) {
			code.append(String.format("destination.%s(mapperFacade.convert(source.%s(), %s); \n", fieldMap.getDestination()
					.getSetter(), fieldMap.getSource().getGetter(), fieldMap.getDestination().getType()));
			return true;
		} else {
			return false;
		}
	}

	private void addGetTypeMethod(CtClass mapperClass, String methodName, Class<?> value) throws CannotCompileException {
		StringBuilder output = new StringBuilder();
		output.append("\n").append("public java.lang.Class ").append(methodName).append("() { return ").append(value.getName())
				.append(".class; }");
		mapperClass.addMethod(CtNewMethod.make(output.toString(), mapperClass));
	}
}

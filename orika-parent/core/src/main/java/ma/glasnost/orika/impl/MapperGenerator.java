package ma.glasnost.orika.impl;

import java.util.HashSet;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import ma.glasnost.orika.CodeGenerator;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.generator.ArrayCodeGenerator;
import ma.glasnost.orika.impl.generator.CollectionCodeGenerator;
import ma.glasnost.orika.impl.generator.ImmutableCodeGenerator;
import ma.glasnost.orika.impl.generator.PrimitivesCodeGenerator;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;

public final class MapperGenerator {
    
    private final MapperFactory mapperFactory;
    
    private final Set<CodeGenerator> generators;
    
    public MapperGenerator(MapperFactory mapperFactory) {
        this(mapperFactory, new HashSet<CodeGenerator>());
    }
    
    public MapperGenerator(MapperFactory mapperFactory, Set<CodeGenerator> generators) {
        this.generators = generators;
        this.mapperFactory = mapperFactory;
        generators.add(new ImmutableCodeGenerator());
        generators.add(new PrimitivesCodeGenerator());
        generators.add(new CollectionCodeGenerator());
        generators.add(new ArrayCodeGenerator());
        
    }
    
    public GeneratedMapperBase build(ClassMap<?, ?> classMap) {
        
        ClassPool pool = ClassPool.getDefault();
        CtClass mapperClass = pool.makeClass("PA_" + classMap.getATypeName() + "_" + classMap.getBTypeName() + "_Mapper");
        
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
        StringBuilder out = new StringBuilder("public void ").append("map" + (aToB ? "AtoB" : "BtoA")).append(
                "(java.lang.Object a, java.lang.Object b) {");
        
        Class<?> sourceClass, destinationClass;
        if (aToB) {
            sourceClass = classMap.getAType();
            destinationClass = classMap.getBType();
        } else {
            sourceClass = classMap.getBType();
            destinationClass = classMap.getAType();
        }
        out.append("\nif(!(a instanceof ").append(sourceClass.getName())
                .append(")) throw new Exception(\"Not supported source object.\");");
        out.append("\nif(!(b instanceof ").append(destinationClass.getName())
                .append(")) throw new Exception(\"Not supported source object.\");\n");
        out.append(sourceClass.getName()).append(" source = (").append(sourceClass.getName()).append(") a; \n");
        out.append(destinationClass.getName()).append(" destination = (").append(destinationClass.getName()).append(") b; \n");
        
        for (FieldMap fieldMap : classMap.getFieldsMapping()) {
            if (!fieldMap.isExcluded()) {
                try {
                    if (!aToB) {
                        fieldMap = fieldMap.flip();
                    }
                    generateFieldMapCode(out, fieldMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        out.append("\n}");
        
        System.out.println(out.toString());
        mapperClass.addMethod(CtNewMethod.make(out.toString(), mapperClass));
    }
    
    private void generateFieldMapCode(StringBuilder code, FieldMap fieldMap) throws Exception {
        
        if (generateConverterCode(code, fieldMap)) {
            return;
        }
        for (CodeGenerator generator : generators) {
            if (generator.accept(fieldMap)) {
                try {
                    generator.generate(fieldMap.getSource(), fieldMap.getDestination(), code);
                } catch (Exception e) {
                    if (fieldMap.isConfigured())
                        throw e;
                    // elsewise ignore
                }
                continue;
            }
        }
    }
    
    private boolean generateConverterCode(final StringBuilder code, final FieldMap fieldMap) {
        Converter<?, ?> converter = mapperFactory.lookupConverter(fieldMap.getSource().getType(), fieldMap.getDestination().getType());
        if (converter != null) {
            code.append(String.format("destination.%s(mapperFacade.convert(source.%s(), %s); \n", fieldMap.getDestination().getSetter(),
                    fieldMap.getSource().getGetter(), fieldMap.getDestination().getType()));
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

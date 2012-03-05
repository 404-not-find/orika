/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.glasnost.orika.metadata;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.MappingHint;
import ma.glasnost.orika.impl.util.PropertyUtil;

public final class ClassMapBuilder<A, B> {
    
    private final Map<String, Property> aProperties;
    private final Map<String, Property> bProperties;
    private final Set<String> propertiesCacheA;
    private final Set<String> propertiesCacheB;
    final private Type<A> aType;
    final private Type<B> bType;
    final private Set<FieldMap> fieldsMapping;
    final private Set<MapperKey> usedMappers;
    private Mapper<A, B> customizedMapper;
    private String[] constructorA;
    private String[] constructorB;
    
    private ClassMapBuilder(Type<A> aType, Type<B> bType) {
        
        if (aType == null) {
            throw new MappingException("[aType] is required");
        }
        
        if (bType == null) {
            throw new MappingException("[bType] is required");
        }
        
        aProperties = PropertyUtil.getProperties(aType);
        bProperties = PropertyUtil.getProperties(bType);
        propertiesCacheA = new HashSet<String>();
        propertiesCacheB = new HashSet<String>();
        
        this.aType = aType;
        this.bType = bType;
        this.fieldsMapping = new HashSet<FieldMap>();
        this.usedMappers = new HashSet<MapperKey>();
    }
    
    /**
     * Map a field two way
     * 
     * @param a
     *            property name in type A
     * @param b
     *            property name in type B
     * @return
     */
    public ClassMapBuilder<A, B> field(String a, String b) {
        return fieldMap(a, b).add();
    }
    
    public FieldMapBuilder<A, B> fieldMap(String a) {
        return fieldMap(a, a);
    }
    
    public FieldMapBuilder<A, B> fieldMap(String a, String b) {
        final FieldMapBuilder<A, B> fieldMapBuilder = new FieldMapBuilder<A, B>(this, a, b);
        
        return fieldMapBuilder;
    }
    
    public ClassMapBuilder<A, B> customize(Mapper<A, B> customizedMapper) {
        this.customizedMapper = customizedMapper;
        return this;
    }
    
    public <X,Y> ClassMapBuilder<A, B> use(Class<?> aParentClass, Class<?> bParentClass) {
        
    	@SuppressWarnings("unchecked")
		Type<Object> aParentType = TypeFactory.valueOf((Class<Object>)aParentClass);
        @SuppressWarnings("unchecked")
		Type<Object> bParentType = TypeFactory.valueOf((Class<Object>)bParentClass);
    	
    	if (aType.isAssignableFrom(aParentType)) {
            throw new MappingException(aType.getSimpleName() + " is not a subclass of " + aParentClass.getSimpleName());
        }
        
        if (bType.isAssignableFrom(bParentType)) {
            throw new MappingException(bType.getSimpleName() + " is not a subclass of " + bParentClass.getSimpleName());
        }
        
        usedMappers.add(new MapperKey(aParentType, bParentType));
        
        return this;
    }
    
    public ClassMapBuilder<A, B> byDefault(DefaultFieldMapper... defaults) {
        
        for (final String propertyName : aProperties.keySet()) {
            if (!propertiesCacheA.contains(propertyName)) {
                if (bProperties.containsKey(propertyName)) {
                    if (!propertiesCacheB.contains(propertyName)) {
                    	fieldMap(propertyName).add();
                    }
                } else {
                    Property prop = aProperties.get(propertyName);
                    for (DefaultFieldMapper defaulter : defaults) {
                        String suggestion = defaulter.suggestMappedField(propertyName, prop.getType());
                        if (suggestion != null && bProperties.containsKey(suggestion)) {
                            if (!propertiesCacheB.contains(suggestion)) {
                            	fieldMap(propertyName, suggestion).add();
                            }
                        }
                    }
                }
            }
        }
        
        return this;
    }
    
    @Deprecated
    public ClassMapBuilder<A, B> byDefault(MappingHint hint0) {
    	return byDefault(new MappingHint[]{hint0});
    }
    
    @Deprecated
    public ClassMapBuilder<A, B> byDefault(MappingHint hint0, MappingHint hint1, MappingHint... mappingHints) {
    	MappingHint[] hints = new MappingHint[mappingHints.length+2];
    	hints[0] = hint0;
    	hints[1] = hint1;
    	if (mappingHints.length>0) {
    		System.arraycopy(mappingHints, 0, hints, 2, mappingHints.length);
    	}
    	return byDefault(new MappingHint[]{hint0, hint1});
    }
    
    @Deprecated
    public ClassMapBuilder<A, B> byDefault(MappingHint[] mappingHints) {
        
    	
    	for (final String propertyName : aProperties.keySet()) {
            if (!propertiesCacheA.contains(propertyName)) {
                if (bProperties.containsKey(propertyName)) {
                    if (!propertiesCacheB.contains(propertyName)) {
                    	fieldMap(propertyName).add();
                    }
                } else {
                    Property prop = aProperties.get(propertyName);
                    for (MappingHint hint : mappingHints) {
                        String suggestion = hint.suggestMappedField(propertyName, prop.getType().getRawType());
                        if (suggestion != null && bProperties.containsKey(suggestion)) {
                            if (!propertiesCacheB.contains(suggestion)) {
                            	fieldMap(propertyName, suggestion).add();
                            }
                        }
                    }
                }
            }
        }
        
        return this;
    }
    
    public ClassMap<A, B> toClassMap() {
        return new ClassMap<A, B>(aType, bType, fieldsMapping, customizedMapper, usedMappers, constructorA, constructorB);
    }
    
    public static <A, B> ClassMapBuilder<A, B> map(Class<A> aType, Class<B> bType) {
        return new ClassMapBuilder<A, B>(TypeFactory.<A>valueOf(aType), TypeFactory.<B>valueOf(bType));
    }
    
    public static <A, B> ClassMapBuilder<A, B> map(Type<A> aType, Type<B> bType) {
        return new ClassMapBuilder<A, B>(aType, bType);
    }
    
    public static <A, B> ClassMapBuilder<A, B> map(Class<A> aType, Type<B> bType) {
        return new ClassMapBuilder<A, B>(TypeFactory.<A>valueOf(aType), bType);
    }
    
    public static <A, B> ClassMapBuilder<A, B> map(Type<A> aType, Class<B> bType) {
        return new ClassMapBuilder<A, B>(aType, TypeFactory.<B>valueOf(bType));
    }
    
    Property resolveProperty(java.lang.reflect.Type type, String expr) {
        Property property;
        if (PropertyUtil.isExpression(expr)) {
            property = PropertyUtil.getNestedProperty(type, expr);
        } else {
            final Map<String, Property> properties = PropertyUtil.getProperties(type);
            if (properties.containsKey(expr)) {
                property = properties.get(expr);
            } else {
                throw new MappingException(expr + " does not belong to " + type);
            }
        }
        
        return property;
    }
    
    Property resolveAProperty(String expr) {
        Property property;
        if (PropertyUtil.isExpression(expr)) {
            property = PropertyUtil.getNestedProperty(aType, expr);
        } else if (aProperties.containsKey(expr)) {
            property = aProperties.get(expr);
        } else {
            throw new MappingException(expr + " does not belong to " + aType.getSimpleName());
        }
        
        return property;
    }
    
    Property resolveBProperty(String expr) {
        Property property;
        if (PropertyUtil.isExpression(expr)) {
            property = PropertyUtil.getNestedProperty(bType, expr);
        } else if (bProperties.containsKey(expr)) {
            property = bProperties.get(expr);
        } else {
            throw new MappingException(expr + " does not belong to " + bType.getSimpleName());
        }
        
        return property;
    }
    
    void addFieldMap(FieldMap fieldMap) {
        this.fieldsMapping.add(fieldMap);
        propertiesCacheA.add(fieldMap.getSource().getExpression());
        propertiesCacheB.add(fieldMap.getDestination().getExpression());
    }
    
    public ClassMapBuilder<A, B> constructorA(String... args) {
        this.constructorA = args.clone();
        return this;
    }
    
    public ClassMapBuilder<A, B> constructorB(String... args) {
        this.constructorB = args.clone();
        return this;
    }
    
}

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

package ma.glasnost.orika.test.generics;

import java.io.Serializable;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import ma.glasnost.orika.impl.util.PropertyUtil;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class GenericsTestCase {
    
    @Test
    public void testTypeErasure() {
        MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();
        EntityLong entity = new EntityLong();
        entity.setId(42L);
        
        new EntityGeneric<String>().setId("Hello");
        new EntityGeneric<Integer>().setId(42);
        EntityLong clone = mapperFacade.map(entity, EntityLong.class);
        
        Assert.assertEquals(Long.valueOf(42L), clone.getId());
    }
    
    @Test
    public void testTypeErasure2() {
        MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();
        EntityLong entity = new EntityLong();
        entity.setId(42L);
        
        new EntityGeneric<String>().setId("Hello");
        EntityGeneric<Long> sourceObject = new EntityGeneric<Long>();
        sourceObject.setId(42L);
        EntityLong clone = mapperFacade.map(sourceObject, EntityLong.class);
        
        Assert.assertEquals(Long.valueOf(42L), clone.getId());
    }
    
    @Test
    public void testGenericsWithNestedParameterizedTypes() {
        MappingUtil.useEclipseJdt();
    	
    	
    	MapperFactory factory = MappingUtil.getMapperFactory(); 
        
        try {
	        ClassMapBuilder.map(EntityGeneric.class, EntityLong.class).field("id.key", "id").toClassMap();
	        Assert.fail("should throw exception for unresolvable nested property");
        } catch (Exception e) {
        	Assert.assertTrue(e.getLocalizedMessage().contains("could not resolve nested property [id.key]"));
        }
        
        // If we explicitly declare the generic type for the source object,
        // we can successfully register the class map
        Type<EntityGeneric<NestedKey<Long>>> sourceType = new TypeBuilder<EntityGeneric<NestedKey<Long>>>(){}.build();
        factory.registerClassMap(
	        ClassMapBuilder.map(sourceType, EntityLong.class).field("id.key", "id").toClassMap());
        
        MapperFacade mapperFacade = factory.getMapperFacade();
        
        EntityGeneric<NestedKey<Long>> sourceObject = new EntityGeneric<NestedKey<Long>>();
   
        NestedKey<Long> key = new NestedKey<Long>();
        key.setKey(42L);
        sourceObject.setId(key);
        
        EntityLong clone = mapperFacade.map(sourceObject, sourceType, TypeFactory.valueOf(EntityLong.class));
        
        Assert.assertEquals(Long.valueOf(42L), clone.getId());
    }
    
    @Test
    public void testParameterizedPropertyUtil() {
        
        Type<?> t = new TypeBuilder<TestEntry<Holder<Long>,Holder<String>>>(){}.build();
        
        Property p = PropertyUtil.getNestedProperty(t, "key.contents");
        Assert.assertEquals(p.getType().getRawType(),Long.class);
        Assert.assertEquals(p.getType(), TypeFactory.valueOf(Long.class));
         
        Map<String,Property> properties = PropertyUtil.getProperties(t);
        Assert.assertTrue(properties.containsKey("key"));
        Assert.assertEquals(properties.get("key").getType(), new TypeBuilder<Holder<Long>>(){}.build());
    }
    
    @Test
    public void testMappingParameterizedTypes() {
        
        MappingUtil.useEclipseJdt();
    	
        Type<TestEntry<Holder<Long>, Holder<String>>> fromType = 
        		new TypeBuilder<TestEntry<Holder<Long>, Holder<String>>>(){}.build();
        Type<OtherTestEntry<Container<String>, Container<String>>> toType = 
        		new TypeBuilder<OtherTestEntry<Container<String>, Container<String>>>(){}.build();
     
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        TestEntry<Holder<Long>, Holder<String>> fromObject = new TestEntry<Holder<Long>, Holder<String>>();
        fromObject.setKey(new Holder<Long>());
        fromObject.getKey().setContents(Long.valueOf(42L));
        fromObject.setValue(new Holder<String>());
        fromObject.getValue().setContents("What is the meaning of life?");
        
        factory.registerClassMap(
                ClassMapBuilder.map(new TypeBuilder<Holder<String>>(){}.build(), new TypeBuilder<Container<String>>(){}.build())
                    .field("contents", "contained").byDefault().toClassMap());
        factory.registerClassMap(
                ClassMapBuilder.map(new TypeBuilder<Holder<Long>>(){}.build(), new TypeBuilder<Container<String>>(){}.build())
                    .field("contents", "contained").byDefault().toClassMap());
        
        MapperFacade mapper = factory.getMapperFacade(); 
        
        OtherTestEntry<Container<String>, Container<String>> result = mapper.map(fromObject, fromType, toType);
        
        Assert.assertNotNull(result);
        Assert.assertEquals(""+fromObject.getKey().getContents(),result.getKey().getContained());
        Assert.assertEquals(fromObject.getValue().getContents(),result.getValue().getContained());
    }
    
    public static class TestEntry<K, V> {
        
        private K key;
        private V value;
        
        public K getKey() {
            return key;
        }
        
        public void setKey(K key) {
            this.key = key;
        }
        
        public V getValue() {
            return value;
        }
        
        public void setValue(V value) {
            this.value = value;
        }
    }
    
    public static class OtherTestEntry<A, B> {
        
        private A key;
        private B value;
        
        public A getKey() {
            return key;
        }
        
        public void setKey(A key) {
            this.key = key;
        }
        
        public B getValue() {
            return value;
        }
        
        public void setValue(B value) {
            this.value = value;
        }
    }
    
    public static class Holder<H> {
        private H contents;
        
        public H getContents() {
            return contents;
        }
        
        public void setContents(H contents) {
            this.contents = contents;
        }
        
    }
    
    public static class Container<C> {
        private C contained;
        
        public C getContained() {
            return contained;
        }
        
        public void setContained(C contained) {
            this.contained = contained;
        }
    }
    
    public static interface Entity<T extends Serializable> {
        public T getId();
        
        public void
        
        setId(T id);
    }
    
    public static class EntityLong implements Entity<Long> {
        private Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
    }
    
    public static class EntityString implements Entity<String> {
        private String id;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
    }
    
    public static class NestedKey<K> implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        private K key;
        
        public K getKey() {
            return key;
        }
        
        public void setKey(K key) {
            this.key = key;
        }
    }
    
    public static class EntityGeneric<T extends Serializable> implements Entity<T> {
        private T id;
        
        public T getId() {
            return id;
        }
        
        public void setId(T id) {
            this.id = id;
        }
        
    }
    
}

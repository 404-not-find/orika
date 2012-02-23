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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.util.PropertyUtil;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.TypeHolder;
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

	//@Test
    public void testGenericsWithNestedTypes() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        //factory.registerClassMap(EntityGeneric.class)
	    
        ClassMap<?,?> classMap = ClassMapBuilder.map(EntityGeneric.class, EntityLong.class)
	        .field("id.key","id").toClassMap();
        factory.registerClassMap(classMap);
        // Generics Problem 2: multiple class mappings registered
        
        MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();
        

        EntityGeneric<NestedKey<Long>> sourceObject = new EntityGeneric<NestedKey<Long>>(); 
        EntityGeneric<Long> other = new EntityGeneric<Long>();
        Type t = sourceObject.getClass().getGenericSuperclass();
        Type[] interfaces = sourceObject.getClass().getGenericInterfaces();
        Type[] typeParams = sourceObject.getClass().getTypeParameters();
        Type[] parentParams = null;
        if (t instanceof ParameterizedType) {
            parentParams = ((ParameterizedType)t).getActualTypeArguments();
        }
        sourceObject.getId().setKey(42L);
        EntityLong clone = mapperFacade.map(sourceObject, EntityLong.class);

        Assert.assertEquals(Long.valueOf(42L), clone.getId());
    }
	
	@Test
	public void testParameterizedPropertyUtil() {
	    
	   ParameterizedType t = 
	            TypeHolder.valueOf(TestEntry.class,  
	                    TypeHolder.valueOf(Holder.class, Long.class));
	    
	   Property p = PropertyUtil.getNestedProperty(t, "key.contents");
	    
	}
	
	@Test
	public void testMappingParameterizedTypes() {
	    
	    TypeHolder<TestEntry<?,?>> fromType = 
	    		new TypeHolder<TestEntry<?,?>>(TestEntry.class,  
	    				new TypeHolder<Holder<Long>>(Holder.class, Long.class),
	    				new TypeHolder<Holder<String>>(Holder.class, String.class));
	    
	    TypeHolder<OtherTestEntry<?,?>> toType = 
	    		new TypeHolder<OtherTestEntry<?,?>>(OtherTestEntry.class,
	    				new TypeHolder<Container<Long>>(Container.class, String.class),
	    				new TypeHolder<Container<Long>>(Container.class, String.class));
	    
	    ClassMap<TestEntry<?,?>,OtherTestEntry<?,?>> classMap = 
	            ClassMapBuilder.map(fromType, toType).byDefault().toClassMap();  
	    
	    MapperFactory factory = MappingUtil.getMapperFactory();
	    MapperFacade mapper = factory.getMapperFacade();
	    
	    TestEntry<Holder<Long>,Holder<String>> fromObject = new TestEntry<Holder<Long>,Holder<String>>();
	    fromObject.setKey(new Holder<Long>());
	    fromObject.getKey().setContents(Long.valueOf(42L));
	    fromObject.setValue(new Holder<String>());
	    fromObject.getValue().setContents("What is the meaning of life?");
	    
	    // TODO: need to override the map method to allow a TypeHolder<?> to be
	    // passed as the toType...
	    OtherTestEntry<?,?> result = mapper.map(fromObject, toType);
	    
	    Assert.assertNotNull(result);
	    
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

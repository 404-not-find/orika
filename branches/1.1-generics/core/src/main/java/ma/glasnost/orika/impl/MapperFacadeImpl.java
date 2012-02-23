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

package ma.glasnost.orika.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.TypeHolder;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

public class MapperFacadeImpl implements MapperFacade {
    
    private final MapperFactory mapperFactory;
    private final UnenhanceStrategy unenhanceStrategy;
    
    public MapperFacadeImpl(MapperFactory mapperFactory, UnenhanceStrategy unenhanceStrategy) {
        this.mapperFactory = mapperFactory;
        this.unenhanceStrategy = unenhanceStrategy;
    }
    
    public <S, D> D map(S sourceObject, TypeHolder<D> destinationClass) {
        return map(sourceObject, destinationClass, new MappingContext());
    }
    
	public <S, D> D map(S sourceObject, TypeHolder<D> destinationType,
			MappingContext context) {
		if (destinationType == null) {
            throw new MappingException("Can not map to a null class.");
        }
        if (sourceObject == null) {
            // throw new MappingException("Can not map a null object.");
            return null;
        }
        
        if (context.isAlreadyMapped(sourceObject, destinationType)) {
			return context.getMappedObject(sourceObject, destinationType);
        }
        
        final S unenhancedSourceObject = unenhanceStrategy.unenhanceObject(sourceObject);
        final TypeHolder<S> sourceClass = unenhanceStrategy.unenhanceClass(unenhancedSourceObject);
        
        // XXX when it's immutable it's ok to copy by ref
        if (ClassUtil.isImmutable(sourceClass) && (sourceClass.equals(destinationType) || sourceClass.getRawType().equals(ClassUtil.getWrapperType(destinationType.getRawType())))) {
            @SuppressWarnings("unchecked")
			D result = (D) unenhancedSourceObject;
        	return result; 
        }
        
        // Check if we have a converter
        
        if (canConvert(sourceClass, destinationType)) {
            return convert(unenhancedSourceObject, destinationType, null);
        }
        
        TypeHolder<? extends D> concreteDestinationClass = mapperFactory.lookupConcreteDestinationClass(sourceClass, destinationType, context);
        if (concreteDestinationClass == null) {
            if (!ClassUtil.isConcrete(destinationType)) {
                throw new MappingException("No concrete class mapping defined for source class " + sourceClass.getName());
            } else {
                concreteDestinationClass = destinationType;
            }
        }
       
        final Mapper<Object, Object> mapper = prepareMapper(sourceClass,concreteDestinationClass);
        
        final D destinationObject = newObject(unenhancedSourceObject, concreteDestinationClass, context);
        
        context.cacheMappedObject(sourceObject, destinationObject);
        
        mapDeclaredProperties(unenhancedSourceObject, destinationObject, sourceClass, concreteDestinationClass, context, mapper);
        
        return destinationObject;
	}
    
    public <S, D> void map(S sourceObject, D destinationObject, MappingContext context) {
        if (destinationObject == null) {
            throw new MappingException("[destinationObject] can not be null.");
        }
        if (sourceObject == null) {
            throw new MappingException("[sourceObject] can not be null.");
        }
        
        final S unenhancedSourceObject = unenhanceStrategy.unenhanceObject(sourceObject);
        final D unenhancedDestinationObject = unenhanceStrategy.unenhanceObject(destinationObject);
        
        @SuppressWarnings("unchecked")
        final TypeHolder<S> sourceClass = TypeHolder.typeOf(unenhancedSourceObject);
        @SuppressWarnings("unchecked")
        final TypeHolder<D> destinationClass = TypeHolder.typeOf(unenhancedDestinationObject);
        
        final Mapper<Object, Object> mapper = prepareMapper(sourceClass,destinationClass);
        mapDeclaredProperties(unenhancedSourceObject, unenhancedDestinationObject, sourceClass, destinationClass, context, mapper);
    }
    
    public <S, D> void map(S sourceObject, D destinationObject) {
        map(sourceObject, destinationObject, new MappingContext());
    }
    
    public final <S, D> Set<D> mapAsSet(Iterable<S> source, TypeHolder<D> destinationClass) {
        return mapAsSet(source, destinationClass, new MappingContext());
    }
    
    public final <S, D> Set<D> mapAsSet(Iterable<S> source, TypeHolder<D> destinationClass, MappingContext context) {
        return (Set<D>) mapAsCollection(source, destinationClass, new HashSet<D>(), context);
    }
    
    public final <S, D> List<D> mapAsList(Iterable<S> source, TypeHolder<D> destinationClass) {
        return (List<D>) mapAsCollection(source, destinationClass, new ArrayList<D>(), new MappingContext());
    }
    
    public final <S, D> List<D> mapAsList(Iterable<S> source, TypeHolder<D> destinationClass, MappingContext context) {
        return (List<D>) mapAsCollection(source, destinationClass, new ArrayList<D>(), context);
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, TypeHolder<D> destinationClass) {
        return mapAsArray(destination, source, destinationClass, new MappingContext());
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, TypeHolder<D> destinationClass) {
        return mapAsArray(destination, source, destinationClass, new MappingContext());
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, TypeHolder<D> destinationClass, MappingContext context) {
        
        if (source == null) {
            return null;
        }
        
        int i = 0;
        for (final S s : source) {
            destination[i++] = map(s, destinationClass);
        }
        return destination;
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, TypeHolder<D> destinationClass, MappingContext context) {
        
        if (source == null) {
            return null;
        }
        
        int i = 0;
        for (final S s : source) {
            destination[i++] = map(s, destinationClass);
        }
        return destination;
    }
    
    public <S, D> List<D> mapAsList(S[] source, TypeHolder<D> destinationClass) {
        return mapAsList(source, destinationClass, new MappingContext());
    }
    
    public <S, D> List<D> mapAsList(S[] source, TypeHolder<D> destinationClass, MappingContext context) {
        final List<D> destination = new ArrayList<D>(source.length);
        for (final S s : source) {
            destination.add(map(s, destinationClass, context));
        }
        return destination;
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, TypeHolder<D> destinationClass) {
        return mapAsSet(source, destinationClass, new MappingContext());
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, TypeHolder<D> destinationClass, MappingContext context) {
        final Set<D> destination = new HashSet<D>(source.length);
        for (final S s : source) {
            destination.add(map(s, destinationClass));
        }
        return destination;
    }
    
    Mapper<Object,Object> prepareMapper(TypeHolder<?> sourceType, TypeHolder<?> destinationType) {
    	 final MapperKey mapperKey = new MapperKey(sourceType, destinationType);
         final Mapper<Object, Object> mapper = mapperFactory.lookupMapper(mapperKey);
         
         if (mapper == null) {
             throw new IllegalStateException(String.format("Can not create a mapper for classes : %s, %s", destinationType,
            		 sourceType));
         }
         return mapper;
    }
    
    void mapDeclaredProperties(Object sourceObject, Object destinationObject, TypeHolder<?> sourceClass, TypeHolder<?> destinationClass,
            MappingContext context, Mapper<Object, Object> mapper ) {
        
        if (mapper.getAType().equals(sourceClass)) {
            mapper.mapAtoB(sourceObject, destinationObject, context);
        } else if (mapper.getAType().equals(destinationClass)) {
            mapper.mapBtoA(sourceObject, destinationObject, context);
        } else if (mapper.getAType().isAssignableFrom(sourceClass)) {
            mapper.mapAtoB(sourceObject, destinationObject, context);
        } else if (mapper.getAType().isAssignableFrom(destinationClass)) {
            mapper.mapBtoA(sourceObject, destinationObject, context);
        } else {
            throw new IllegalStateException(String.format("Source object type's must be one of '%s' or '%s'.", mapper.getAType(),
                    mapper.getBType()));
            
        }
    }
    
    public <S, D> D newObject(S sourceObject, TypeHolder<? extends D> destinationClass, MappingContext context) {
        
        try {
            final ObjectFactory<? extends D> objectFactory = mapperFactory.lookupObjectFactory(destinationClass);
            if (objectFactory != null) {
                return objectFactory.create(sourceObject, context);
            } else {
                return destinationClass.getRawType().newInstance();
            }
        } catch (final InstantiationException e) {
            throw new MappingException(e);
        } catch (final IllegalAccessException e) {
            throw new MappingException(e);
        }
    }
    
    <S, D> Collection<D> mapAsCollection(Iterable<S> source, TypeHolder<D> destinationClass, Collection<D> destination, MappingContext context) {
        
        if (source == null) {
            return null;
        }
        
        for (final S item : source) {
            destination.add(map(item, destinationClass, context));
        }
        return destination;
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> D convert(S source, TypeHolder<D> destinationClass, String converterId) {
        final TypeHolder<? extends Object> sourceClass = unenhanceStrategy.unenhanceClass(source);
        Converter<S, D> converter;
        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
        if (converterId == null) {
            converter = (Converter<S, D>) converterFactory.getConverter(sourceClass, destinationClass);
        } else {
            converter = (Converter<S, D>) converterFactory.getConverter(converterId);
        }
        
        return converter.convert(source, destinationClass);
    }
    
    private <S, D> boolean canConvert(TypeHolder<S> sourceClass, TypeHolder<D> destinationClass) {
        return mapperFactory.getConverterFactory().canConvert(sourceClass, destinationClass);
    }

	public <S, D> D map(S sourceObject, Class<D> destinationClass) {
		return map(sourceObject, TypeHolder.valueOf(destinationClass));
	}

	public <S, D> D map(S sourceObject, Class<D> destinationClass,
			MappingContext context) {
		return map(sourceObject, TypeHolder.valueOf(destinationClass), context);
	}

	public <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass) {
		return mapAsSet(source, TypeHolder.valueOf(destinationClass));
	}

	public <S, D> Set<D> mapAsSet(Iterable<S> source,
			Class<D> destinationClass, MappingContext context) {
		return mapAsSet(source, TypeHolder.valueOf(destinationClass), context);
	}

	public <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass) {
		return mapAsSet(source, TypeHolder.valueOf(destinationClass));
	}

	public <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass,
			MappingContext context) {
		return mapAsSet(source, TypeHolder.valueOf(destinationClass), context);
	}

	public <S, D> List<D> mapAsList(Iterable<S> source,
			Class<D> destinationClass) {
		return mapAsList(source, TypeHolder.valueOf(destinationClass));
	}

	public <S, D> List<D> mapAsList(Iterable<S> source,
			Class<D> destinationClass, MappingContext context) {
		return mapAsList(source, TypeHolder.valueOf(destinationClass), context);
	}

	public <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass) {
		return mapAsList(source, TypeHolder.valueOf(destinationClass));
	}

	public <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass,
			MappingContext context) {
		return mapAsList(source, TypeHolder.valueOf(destinationClass), context);
	}

	public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source,
			Class<D> destinationClass) {
		return mapAsArray(destination, source, TypeHolder.valueOf(destinationClass));
	}

	public <S, D> D[] mapAsArray(D[] destination, S[] source,
			Class<D> destinationClass) {
		return mapAsArray(destination, source, TypeHolder.valueOf(destinationClass));
	}

	public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source,
			Class<D> destinationClass, MappingContext context) {
		return mapAsArray(destination, source, TypeHolder.valueOf(destinationClass), context);
	}

	public <S, D> D[] mapAsArray(D[] destination, S[] source,
			Class<D> destinationClass, MappingContext context) {
		return mapAsArray(destination, source, TypeHolder.valueOf(destinationClass), context);
	}

	public <S, D> D convert(S source, Class<D> destinationClass,
			String converterId) {
		return convert(source, TypeHolder.valueOf(destinationClass), converterId);
	}

}

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
import java.util.concurrent.ConcurrentHashMap;

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
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

public class MapperFacadeImpl implements MapperFacade {
    
    private final MapperFactory mapperFactory;
    private final UnenhanceStrategy unenhanceStrategy;
    private final ConcurrentHashMap<java.lang.reflect.Type, Type<?>> resolvedTypes = new ConcurrentHashMap<java.lang.reflect.Type, Type<?>>();
    
    public MapperFacadeImpl(MapperFactory mapperFactory, UnenhanceStrategy unenhanceStrategy) {
        this.mapperFactory = mapperFactory;
        this.unenhanceStrategy = unenhanceStrategy;
    }
    
    @SuppressWarnings("unchecked")
    private <S> Type<S> normalizeSourceType(S sourceObject, Type<S> sourceType) {
        
        /*
         * Use the raw type in cases where the sourceType is null or not
         * providing any extra information
         */
        java.lang.reflect.Type typeKey = sourceType;
        if (sourceType == null || !sourceType.isParameterized()) {
            typeKey = sourceObject.getClass();
        }
        
        Type<?> resolvedType = resolvedTypes.get(typeKey);
        if (resolvedType == null) {
            Type<?> newlyResolvedType;
            if (sourceType != null) {
                if (sourceType.isAssignableFrom(sourceObject.getClass())) {
                    sourceType = (Type<S>) TypeFactory.valueOf(sourceObject.getClass());
                }
                if (ClassUtil.isConcrete(sourceType)) {
                    newlyResolvedType = unenhanceStrategy.unenhanceType(sourceObject, sourceType);
                } else {
                    newlyResolvedType = unenhanceStrategy.unenhanceType(sourceObject, TypeFactory.resolveTypeOf(sourceObject, sourceType));
                }
                resolvedType = resolvedTypes.putIfAbsent(typeKey, newlyResolvedType);
                if (resolvedType == null) {
                    resolvedType = newlyResolvedType;
                }
            } else {
                resolvedType = unenhanceStrategy.unenhanceType(sourceObject, TypeFactory.typeOf(sourceObject));
            }
            
        }
        return (Type<S>) resolvedType;
    }
    
    public <S, D> D map(S sourceObject, Type<S> sourceType, Type<D> destinationClass) {
        return map(sourceObject, sourceType, destinationClass, new MappingContext());
    }
    
    public <S, D> D map(S sourceObject, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        if (destinationType == null) {
            throw new MappingException("Can not map to a null class.");
        }
        if (sourceObject == null) {
            // throw new MappingException("Can not map a null object.");
            return null;
        }
        
        if (context.isAlreadyMapped(sourceObject, destinationType)) {
            D result = context.getMappedObject(sourceObject, destinationType);
            return result;
        }
        
        final Type<S> resolvedSourceType = normalizeSourceType(sourceObject, sourceType);
        sourceObject = unenhanceStrategy.unenhanceObject(sourceObject, sourceType);
        
        // We can copy by reference when source and destination types are the
        // same and immutable.
        if (canCopyByReference(destinationType, resolvedSourceType)) {
            @SuppressWarnings("unchecked")
            D result = (D) sourceObject;
            return result;
        }
        
        // Check if we have a converter
        if (canConvert(resolvedSourceType, destinationType)) {
            return convert(sourceObject, sourceType, destinationType, null);
        }
        
        Type<? extends D> resolvedDestinationType = mapperFactory.lookupConcreteDestinationType(resolvedSourceType, destinationType,
                context);
        if (resolvedDestinationType == null) {
            if (!ClassUtil.isConcrete(destinationType)) {
                throw new MappingException("No concrete class mapping defined for source class " + resolvedSourceType.getName());
            } else {
                resolvedDestinationType = destinationType;
            }
        }
        
        final Mapper<Object, Object> mapper = prepareMapper(resolvedSourceType, resolvedDestinationType);
        
        final D destinationObject = newObject(sourceObject, resolvedDestinationType, context);
        
        context.cacheMappedObject(sourceObject, destinationType, destinationObject);
        
        mapDeclaredProperties(sourceObject, destinationObject, resolvedSourceType, resolvedDestinationType, context, mapper);
        
        return destinationObject;
    }
    
    private <D, S> boolean canCopyByReference(Type<D> destinationType, final Type<S> resolvedSourceType) {
        return ClassUtil.isImmutable(resolvedSourceType)
                && (resolvedSourceType.equals(destinationType) || resolvedSourceType.getRawType().equals(
                        ClassUtil.getWrapperType(destinationType.getRawType())));
    }
    
    public <S, D> void map(S sourceObject, D destinationObject, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        if (destinationObject == null) {
            throw new MappingException("[destinationObject] can not be null.");
        }
        if (sourceObject == null) {
            throw new MappingException("[sourceObject] can not be null.");
        }
        
        final Type<S> theSourceType = normalizeSourceType(sourceObject, sourceType != null ? sourceType : TypeFactory.typeOf(sourceObject));
        final Type<D> theDestinationType = destinationType != null ? destinationType : TypeFactory.typeOf(destinationObject);
        
        final Mapper<Object, Object> mapper = prepareMapper(theSourceType, theDestinationType);
        mapDeclaredProperties(sourceObject, destinationObject, theSourceType, theDestinationType, context, mapper);
    }
    
    public <S, D> void map(S sourceObject, D destinationObject, Type<S> sourceType, Type<D> destinationType) {
        map(sourceObject, destinationObject, sourceType, destinationType, new MappingContext());
    }
    
    public <S, D> void map(S sourceObject, D destinationObject, MappingContext context) {
        map(sourceObject, destinationObject, null, null, context);
    }
    
    public <S, D> void map(S sourceObject, D destinationObject) {
        map(sourceObject, destinationObject, new MappingContext());
    }
    
    public final <S, D> Set<D> mapAsSet(Iterable<S> source, Type<S> sourceType, Type<D> destinationType) {
        return mapAsSet(source, sourceType, destinationType, new MappingContext());
    }
    
    public final <S, D> Set<D> mapAsSet(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        return (Set<D>) mapAsCollection(source, sourceType, destinationType, new HashSet<D>(), context);
    }
    
    public final <S, D> List<D> mapAsList(Iterable<S> source, Type<S> sourceType, Type<D> destinationType) {
        return (List<D>) mapAsCollection(source, sourceType, destinationType, new ArrayList<D>(), new MappingContext());
    }
    
    public final <S, D> List<D> mapAsList(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        return (List<D>) mapAsCollection(source, sourceType, destinationType, new ArrayList<D>(), context);
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Type<S> sourceType, Type<D> destinationType) {
        return mapAsArray(destination, source, sourceType, destinationType, new MappingContext());
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Type<S> sourceType, Type<D> destinationType) {
        return mapAsArray(destination, source, sourceType, destinationType, new MappingContext());
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        
        if (source == null) {
            return null;
        }
        
        int i = 0;
        for (final S s : source) {
            destination[i++] = map(s, sourceType, destinationType);
        }
        return destination;
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        
        if (source == null) {
            return null;
        }
        
        int i = 0;
        for (final S s : source) {
            destination[i++] = map(s, sourceType, destinationType, context);
        }
        return destination;
    }
    
    public <S, D> List<D> mapAsList(S[] source, Type<S> sourceType, Type<D> destinationType) {
        return mapAsList(source, sourceType, destinationType, new MappingContext());
    }
    
    public <S, D> List<D> mapAsList(S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        final List<D> destination = new ArrayList<D>(source.length);
        for (final S s : source) {
            destination.add(map(s, sourceType, destinationType, context));
        }
        return destination;
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Type<S> sourceType, Type<D> destinationType) {
        return mapAsSet(source, sourceType, destinationType, new MappingContext());
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        final Set<D> destination = new HashSet<D>(source.length);
        for (final S s : source) {
            destination.add(map(s, sourceType, destinationType, context));
        }
        return destination;
    }
    
    Mapper<Object, Object> prepareMapper(Type<?> sourceType, Type<?> destinationType) {
        final MapperKey mapperKey = new MapperKey(sourceType, destinationType);
        final Mapper<Object, Object> mapper = mapperFactory.lookupMapper(mapperKey);
        
        if (mapper == null) {
            throw new IllegalStateException(String.format("Can not create a mapper for classes : %s, %s", destinationType, sourceType));
        }
        return mapper;
    }
    
    void mapDeclaredProperties(Object sourceObject, Object destinationObject, Type<?> sourceClass, Type<?> destinationType,
            MappingContext context, Mapper<Object, Object> mapper) {
        
        if (mapper.getAType().equals(sourceClass)) {
            mapper.mapAtoB(sourceObject, destinationObject, context);
        } else if (mapper.getAType().equals(destinationType)) {
            mapper.mapBtoA(sourceObject, destinationObject, context);
        } else if (mapper.getAType().isAssignableFrom(sourceClass)) {
            mapper.mapAtoB(sourceObject, destinationObject, context);
        } else if (mapper.getAType().isAssignableFrom(destinationType)) {
            mapper.mapBtoA(sourceObject, destinationObject, context);
        } else {
            throw new IllegalStateException(String.format("Source object type's must be one of '%s' or '%s'.", mapper.getAType(),
                    mapper.getBType()));
            
        }
    }
    
    public <S, D> D newObject(S sourceObject, Type<? extends D> destinationType, MappingContext context) {
        
        try {
            final ObjectFactory<? extends D> objectFactory = mapperFactory.lookupObjectFactory(destinationType);
            if (objectFactory != null) {
                return objectFactory.create(sourceObject, context);
            } else {
                return destinationType.getRawType().newInstance();
            }
        } catch (final InstantiationException e) {
            throw new MappingException(e);
        } catch (final IllegalAccessException e) {
            throw new MappingException(e);
        }
    }
    
    <S, D> Collection<D> mapAsCollection(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, Collection<D> destination,
            MappingContext context) {
        
        if (source == null) {
            return null;
        }
        
        for (final S item : source) {
            destination.add(map(item, sourceType, destinationType, context));
        }
        return destination;
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> D convert(S source, Type<S> sourceType, Type<D> destinationType, String converterId) {
        final Type<? extends Object> sourceClass = unenhanceStrategy.unenhanceType(source, sourceType);
        Converter<S, D> converter;
        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
        if (converterId == null) {
            converter = (Converter<S, D>) converterFactory.getConverter(sourceClass, destinationType);
        } else {
            converter = (Converter<S, D>) converterFactory.getConverter(converterId);
        }
        
        return converter.convert(source, destinationType);
    }
    
    private <S, D> boolean canConvert(Type<S> sourceType, Type<D> destinationType) {
        return mapperFactory.getConverterFactory().canConvert(sourceType, destinationType);
    }
    
    public <S, D> D map(S sourceObject, Class<D> destinationClass) {
        return map(sourceObject, TypeFactory.typeOf(sourceObject), TypeFactory.valueOf(destinationClass));
    }
    
    public <S, D> D map(S sourceObject, Class<D> destinationClass, MappingContext context) {
        return map(sourceObject, TypeFactory.typeOf(sourceObject), TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass) {
        return mapAsSet(source, TypeFactory.elementTypeOf(source), TypeFactory.valueOf(destinationClass));
    }
    
    public <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return mapAsSet(source, TypeFactory.elementTypeOf(source), TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass) {
        return mapAsSet(source, TypeFactory.componentTypeOf(source), TypeFactory.valueOf(destinationClass));
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass, MappingContext context) {
        return mapAsSet(source, TypeFactory.componentTypeOf(source), TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass) {
        return mapAsList(source, TypeFactory.elementTypeOf(source), TypeFactory.valueOf(destinationClass));
    }
    
    public <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return mapAsList(source, TypeFactory.elementTypeOf(source), TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass) {
        return mapAsList(source, TypeFactory.componentTypeOf(source), TypeFactory.valueOf(destinationClass));
    }
    
    public <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass, MappingContext context) {
        return mapAsList(source, TypeFactory.componentTypeOf(source), TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass) {
        return mapAsArray(destination, source, TypeFactory.elementTypeOf(source), TypeFactory.valueOf(destinationClass));
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass) {
        return mapAsArray(destination, source, TypeFactory.componentTypeOf(source), TypeFactory.valueOf(destinationClass));
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return mapAsArray(destination, source, TypeFactory.elementTypeOf(source), TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass, MappingContext context) {
        return mapAsArray(destination, source, TypeFactory.componentTypeOf(source), TypeFactory.valueOf(destinationClass), context);
    }
    
    public <S, D> D convert(S source, Class<D> destinationClass, String converterId) {
        return convert(source, TypeFactory.typeOf(source), TypeFactory.valueOf(destinationClass), converterId);
    }
    
}
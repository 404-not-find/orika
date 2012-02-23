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

package ma.glasnost.orika;

import java.util.Set;

import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.TypeHolder;

/**
 * MapperFactory
 * 
 * The mapper factory is the heart of Orika, a small container where metadata
 * are stored, it's used by other component of engine, to look for generated
 * mappers, converter, object factories ... etc.
 * 
 * @author S.M. El Aatifi
 * 
 */
public interface MapperFactory {
    
    <A, B> Mapper<A, B> lookupMapper(MapperKey mapperKey);
    
    <A, B> void registerClassMap(ClassMap<A, B> classMap);
    
    <T> void registerObjectFactory(ObjectFactory<T> objectFactory, TypeHolder<T> targetType);
    
    // TODO: use Type here? might allow better back-compatibility; as long
    // as we're using the key for an identity lookup and not a hierarchical
    // inheritance check...
    <T> ObjectFactory<T> lookupObjectFactory(TypeHolder<T> targetClass);
    
    <S, D> TypeHolder<? extends D> lookupConcreteDestinationClass(TypeHolder<S> sourceType, TypeHolder<D> destinationType, MappingContext context);
    
    void registerMappingHint(MappingHint... hint);
    
    Set<ClassMap<Object, Object>> lookupUsedClassMap(MapperKey mapperKey);
    
    <A, B> ClassMap<A, B> getClassMap(MapperKey mapperKey);
    
    Set<TypeHolder<? extends Object>> lookupMappedClasses(TypeHolder<?> type);
    
    MapperFacade getMapperFacade();
    
    ConverterFactory getConverterFactory();
    
    void build();
    
}

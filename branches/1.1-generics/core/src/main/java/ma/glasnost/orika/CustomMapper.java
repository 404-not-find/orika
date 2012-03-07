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

import java.lang.reflect.ParameterizedType;

import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * Abstract super-class for all generated mappers and user custom mappers.
 * 
 * @see ma.glasnost.orika.metadata.ClassMapBuilder
 * @author S.M. El Aatifi
 * 
 */
public abstract class CustomMapper<A, B> implements Mapper<A, B> {
    
    protected MapperFacade mapperFacade;
    protected Type<A> aType;
    protected Type<B> bType;
    
    public CustomMapper() {
        java.lang.reflect.Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass != null && genericSuperclass instanceof ParameterizedType) {
            ParameterizedType superType = (ParameterizedType)genericSuperclass;
            aType = TypeFactory.valueOf(superType.getActualTypeArguments()[0]);
            bType = TypeFactory.valueOf(superType.getActualTypeArguments()[1]);
        } else {
            throw new IllegalStateException("When you subclass " + 
                    CustomMapper.class.getSimpleName() + " A and B type-parameters are required.");
        }
    }
    
    public void mapAtoB(A a, B b, MappingContext context) {
        /* */
    }
    
    public void mapBtoA(B b, A a, MappingContext context) {
        /* */
    }
    
    public void setMapperFacade(MapperFacade mapper) {
        this.mapperFacade = mapper;
    }
    
    public Type<A> getAType() {
    	return aType;
    }
    
    public Type<B> getBType() {
    	return bType;
    }
    
    public void setUsedMappers(Mapper<Object, Object>[] mapper) {
        throw throwShouldNotCalledCustomMapper();
    }
    
    public void setUsedTypes(Type<Object>[] usedTypes) {
        throw throwShouldNotCalledCustomMapper();
    }
    
    private IllegalStateException throwShouldNotCalledCustomMapper() {
        return new IllegalStateException("Should not be called for a user custom mapper.");
    }
}

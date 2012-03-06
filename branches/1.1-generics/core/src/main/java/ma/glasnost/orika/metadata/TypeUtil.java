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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;

abstract class TypeUtil {
    
    static java.lang.reflect.Type[] resolveActualTypeArguments(ParameterizedType type, Type<?> reference) {
          
        return resolveActualTypeArguments(type.getActualTypeArguments(), reference);
    }
    
    static java.lang.reflect.Type[] resolveActualTypeArguments(java.lang.reflect.Type[] typeArguments, Type<?> reference) {
    	
		java.lang.reflect.Type[] actualTypeArguments = new java.lang.reflect.Type[typeArguments.length];
        for (int i=0, len=actualTypeArguments.length; i < len; ++i) {
            java.lang.reflect.Type typeArg = typeArguments[i];
            if (typeArg instanceof TypeVariable) {
                java.lang.reflect.Type resolvedVariable = null;
                if (reference!=null) {
                    resolvedVariable = reference.getTypeByVariable((TypeVariable<?>)typeArg);
                }
                if (resolvedVariable!=null) {
                    actualTypeArguments[i] = resolvedVariable;
                } else {
                    actualTypeArguments[i] = TypeFactory.valueOf(Object.class);
                }
            }
        }   
        return actualTypeArguments;
	
    }
    
    @SuppressWarnings("unchecked")
    static Type<?>[] convertTypeArguments(Class<?> rawType, java.lang.reflect.Type[] actualTypeArguments) {
        
        TypeVariable<?>[] typeVariables = rawType.getTypeParameters();
        Type<?>[] resultTypeArguments = new Type<?>[typeVariables.length];
        if (actualTypeArguments.length == 0) {
            for (int i=0, len=typeVariables.length; i < len; ++i) {
                //typesByVariable.put(typeVariables[i], typeVariables[i]);
                resultTypeArguments[i] = TypeFactory.valueOf(Object.class);
            }
        } else if (actualTypeArguments.length < typeVariables.length) {
            throw new IllegalArgumentException("Must provide all type-arguments or none");
        } else {
        
            for (int i=0, len=actualTypeArguments.length; i < len; ++i) {
                java.lang.reflect.Type t = actualTypeArguments[i];
                if (t instanceof Type) {
                    // Unused
                    //((Type<?>) t).setOwnerType(this);
                } else if (t instanceof Class) {
                    t = TypeFactory.valueOf((Class<Object>) t);
                } else if (t instanceof ParameterizedType){
                    t = TypeFactory.valueOf((ParameterizedType)t);
                } else {
                    // LOG.warn("Resolving unspecified template variable '" + t + "' to Object");
                    t = TypeFactory.valueOf(Object.class);
                }
                resultTypeArguments[i] = (Type<?>)t;
                //typesByVariable.put(typeVariables[i], t);
            }
        }
        return resultTypeArguments;
    }
}

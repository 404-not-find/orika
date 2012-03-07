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

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

abstract class TypeUtil {
    
    @SuppressWarnings("unchecked")
    static final Set<Type<?>> IGNORED_TYPES = 
            new HashSet<Type<?>>(Arrays.asList(
                    TypeFactory.valueOf(Cloneable.class), 
                    TypeFactory.valueOf(Serializable.class),
                    TypeFactory.valueOf(Externalizable.class)));
    
    
    static java.lang.reflect.Type[] resolveActualTypeArguments(ParameterizedType type, Type<?> reference) {
          
        return resolveActualTypeArguments(((Class<?>)type.getRawType()).getTypeParameters(), type.getActualTypeArguments(), reference);
    }
    
    static java.lang.reflect.Type[] resolveActualTypeArguments(TypeVariable<?>[] vars, java.lang.reflect.Type[] typeArguments, Type<?> reference) {
    	
		java.lang.reflect.Type[] actualTypeArguments = new java.lang.reflect.Type[typeArguments.length];
        for (int i=0, len=actualTypeArguments.length; i < len; ++i) {
            java.lang.reflect.Type typeArg = typeArguments[i];
            TypeVariable<?> var = vars[i];
            // TODO: need to clean up this section:
            // we should loop through the types provided by the reference type, 
            // and if they are more specific than the existing type, use the reference instead
            if (typeArg instanceof TypeVariable) {
                var = (TypeVariable<?>)typeArg;
            }
            Type<?> typeFromReference = (Type<?>) reference.getTypeByVariable(var);
            Type<?> typeFromArgument = (Type<?>) TypeFactory.valueOf(typeArg);
            actualTypeArguments[i] = getMostSpecificType(typeFromReference, typeFromArgument, IGNORED_TYPES);
//            if (typeFromReference != null && typeFromArgument.isAssignableFrom(typeFromReference)) {
//                actualTypeArguments[i] = typeFromReference;
//            } else {
//                actualTypeArguments[i] = typeFromArgument;
//            }
            
            
//            if (typeArg instanceof TypeVariable) {
//                java.lang.reflect.Type resolvedVariable = null;
//                if (reference!=null) {
//                    resolvedVariable = reference.getTypeByVariable((TypeVariable<?>)typeArg);
//                }
//                if (resolvedVariable!=null) {
//                    actualTypeArguments[i] = resolvedVariable;
//                } else {
//                    actualTypeArguments[i] = TypeFactory.valueOf(Object.class);
//                }
//            }
        }   
        return actualTypeArguments;
	
    }
    
    static Type<?> getMostSpecificType(Type<?> type0, Type<?> type1, Set<Type<?>> ignoredTypes) {
        if (type1 == null && type0 == null) {
            return null;
        } else if (type0 == null && type1 != null) {
            return type1;
        } else if (type1 == null && type0 != null) {
            return type0;
        } else if (type1 == null && type0 == null) {
            return null;
        } else if (ignoredTypes.contains(type1) && ignoredTypes.contains(type0)) {
            return TypeFactory.TYPE_OF_OBJECT;
        } else if (ignoredTypes.contains(type1)) {
            return type0;
        } else if (ignoredTypes.contains(type0)) {
            return type1;
        } else if (type0.isAssignableFrom(type1)) {
            return type1;
        } else if (type1.isAssignableFrom(type0)) {
            return type0;
        } else {
            // Types not comparable...
            //return TypeFactory.TYPE_OF_OBJECT;
            throw new IllegalArgumentException("types " + type0 + " and " + type1 + " are not comparable");
        }
    }
    
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
                resultTypeArguments[i] = TypeFactory.valueOf(t);
            }
        }
        return resultTypeArguments;
    }
}

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import extra166y.CustomConcurrentHashMap;

/**
 * TypeFactory contains various methods for obtaining a Type instance to
 * represent various type situations.
 * 
 * @author matt.deboer@gmail.com
 * 
 * @param <T>
 */
public abstract class TypeFactory implements ParameterizedType {
    
    static class TypeKey {
        private final byte[] bytes;
        public TypeKey(byte[] bytes) {
            this.bytes = bytes;
        }
        
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (other == null)
                return false;
            if (other.getClass() != getClass())
                return false;
            TypeKey otherKey = (TypeKey)other;
                
            return Arrays.equals(this.bytes, otherKey.bytes);
        }
        
        public int hashCode() {
            return Arrays.hashCode(this.bytes);
        }
    }
    
    /**
     * Use a custom concurrent map to avoid keeping static references to Types
     * (classes) which may belong to descendant class-loaders
     */
    private static final CustomConcurrentHashMap<TypeKey, Type<?>> typeCache = new CustomConcurrentHashMap<TypeKey, Type<?>>(
            CustomConcurrentHashMap.STRONG, CustomConcurrentHashMap.EQUALS, CustomConcurrentHashMap.WEAK, CustomConcurrentHashMap.EQUALS,
            16);
    
    static {
        intern(Object.class, new Type<?>[0]);
    }
    
    /**
     * Merge an int value into byte array, starting at the specified starting
     * index (occupies the next 4 bytes);
     * 
     * @param value
     * @param bytes
     * @param startIndex
     */
    private static final void intToByteArray(int value, byte[] bytes, int startIndex) {
        int i = startIndex * 4;
        bytes[i] = (byte) (value >>> 24);
        bytes[i + 1] = (byte) (value >>> 16);
        bytes[i + 2] = (byte) (value >>> 8);
        bytes[i + 3] = (byte) (value);
    }
    
    /**
     * Calculates an identity for a Class, Type[] pair; avoids maintaining a
     * reference the actual class.
     * 
     * @param rawType
     * @param typeArguments
     * @return
     */
    private static final TypeKey getIdentityKey(Class<?> rawType, java.lang.reflect.Type[] typeArguments) {
          byte[] identityHashBytes = new byte[(typeArguments.length + 1) * 4];
          intToByteArray(System.identityHashCode(rawType), identityHashBytes, 0);
          for (int i = 0, len = typeArguments.length; i < len; ++i) {
              intToByteArray(System.identityHashCode(typeArguments[i]), identityHashBytes, i + 1);
          }
          return new TypeKey(identityHashBytes);
        
          // Alternative byte-hash generation
          
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        DataOutputStream dos = new DataOutputStream(baos);
//        try {
//            dos.writeInt(System.identityHashCode(rawType));
//            for (int i = 0, len = typeArguments.length; i < len; ++i) {
//                dos.writeInt(System.identityHashCode(typeArguments[i]));
//            }   
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return new TypeKey(baos.toByteArray());
        

    }
    
    /**
     * Store the combination of rawType and type arguments as a Type within the
     * type cache.<br>
     * Use the existing type if already available; we try to enforce that Type
     * should be immutable.
     * 
     * @param rawType
     * @param typeArguments
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> Type<T> intern(Class<T> rawType, java.lang.reflect.Type[] typeArguments) {
        
        Type<?>[] convertedArguments = TypeUtil.convertTypeArguments(rawType, typeArguments);
        TypeKey key = getIdentityKey(rawType, convertedArguments);
        
        Type<T> mapped = (Type<T>) typeCache.get(key);
        if (mapped == null) {
            mapped = createType(key, rawType, convertedArguments);
            Type<T> existing = (Type<T>) typeCache.putIfAbsent(key, mapped);
            if (existing != null) {
                mapped = existing;
            }
        }
        return mapped;
    }
    
    private static <T> Type<T> createType(TypeKey key, Class<T> rawType, Type<?>[] typeArguments) {
        Map<TypeVariable<?>, java.lang.reflect.Type> typesByVariable = null;
        if (typeArguments.length > 0) {
            typesByVariable = new HashMap<TypeVariable<?>, java.lang.reflect.Type>(typeArguments.length);
            for (int i = 0, len = typeArguments.length; i < len; ++i) {
                typesByVariable.put(rawType.getTypeParameters()[i], typeArguments[i]);
            }
        }
        return new Type<T>(key, rawType, typesByVariable, typeArguments);
    }
    
    /**
     * @param rawType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <E> Type<E> valueOf(final Class<? extends E> rawType) {
        if (rawType == null) {
            return null;
        } else if (rawType.isAnonymousClass() && rawType.getGenericSuperclass() instanceof ParameterizedType) {
            ParameterizedType genericSuper = (ParameterizedType) rawType.getGenericSuperclass();
            return valueOf(genericSuper);
        } else {
            return (Type<E>) intern(rawType, new java.lang.reflect.Type[0]);
        }
    }
    
    /**
     * @param rawType
     * @param actualTypeArguments
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <E> Type<E> valueOf(final Class<? extends E> rawType, final java.lang.reflect.Type... actualTypeArguments) {
        if (rawType == null) {
            return null;
        } else {
            return (Type<E>) intern((Class<E>) rawType, actualTypeArguments);
        }
    }
    
    /**
     * This method declaration helps to shortcut the other methods for
     * ParameterizedType which it extends; we just return it.
     * 
     * @param type
     * @return
     */
    public static <T> Type<T> valueOf(final Type<T> type) {
        return type;
    }
    
    /**
     * Return the Type for the given ParameterizedType, resolving actual type
     * arguments where possible.
     * 
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> valueOf(final ParameterizedType type) {
        return valueOf((Class<? extends T>) type.getRawType(), type.getActualTypeArguments());
    }
    
    
    /**
     * Return the Type for the given java.lang.reflect.Type, either for a ParameterizedType
     * or a Class instance
     * 
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> valueOf(final java.lang.reflect.Type type) {
        if (type instanceof ParameterizedType) {
            return valueOf((ParameterizedType)type);
        } else if (type instanceof Class) {
            return valueOf((Class<T>)type);
        } else {
            throw new IllegalArgumentException(type + " is an unsupported type");
        }
    }
    
    
    /**
     * Resolve the Type for the given ParameterizedType, using the provided
     * referenceType to resolve any unresolved actual type arguments.
     * 
     * @param type
     * @param referenceType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> resolveValueOf(final ParameterizedType type, final Type<?> referenceType) {
        if (type == null) {
            return null;
        } else {
            java.lang.reflect.Type[] actualTypeArguments = TypeUtil.resolveActualTypeArguments(type, referenceType);
            Type<T> result = intern((Class<T>) type.getRawType(), actualTypeArguments);
            
            return result;
        }
    }
    
    /**
     * Resolve the Type for the given Class, using the provided referenceType to
     * resolve the actual type arguments.
     * 
     * @param type
     * @param referenceType
     * @return
     */
    public static <T> Type<T> resolveValueOf(final Class<T> type, final Type<?> referenceType) {
        if (type == null) {
            return null;
        } else {
            if (type.getTypeParameters() != null && type.getTypeParameters().length > 0) {
                java.lang.reflect.Type[] actualTypeArguments = TypeUtil.resolveActualTypeArguments(type.getTypeParameters(), referenceType);
                return valueOf(type, actualTypeArguments);
            } else {
                return valueOf(type);
            }
        }
    }
    
    /**
     * Return the Type for the given object.
     * 
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> typeOf(final T object) {
        return valueOf((Class<T>) object.getClass());
    }
    
    /**
     * Resolve the Type for the given object, using the provided referenceType
     * to resolve the actual type arguments.
     * 
     * @param object
     * @param referenceType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> resolveTypeOf(final T object, Type<?> referenceType) {
        return resolveValueOf((Class<T>) object.getClass(), referenceType);
    }
    
    /**
     * Resolve the (element) component type for the given array.
     * 
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> componentTypeOf(final T[] object) {
        return valueOf((Class<T>) object.getClass().getComponentType());
    }
    
    /**
     * Resolve the nested element type for the given Iterable.
     * 
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> elementTypeOf(final Iterable<T> object) {
        return valueOf((Class<T>) object.iterator().next().getClass());
    }
    
}

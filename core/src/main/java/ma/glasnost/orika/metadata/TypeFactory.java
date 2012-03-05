package ma.glasnost.orika.metadata;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import extra166y.CustomConcurrentHashMap;
import extra166y.CustomConcurrentHashMap.Equivalence;
import extra166y.CustomConcurrentHashMap.Strength;

/**
 * Type is an implementation of ParameterizedType which may be
 * used in various mapping methods where a Class instance would normally
 * be used, in order to provide more specific details as to the actual types
 * represented by the generic template parameters in a given class.<br><br>
 * 
 * Such details are not normally available at runtime using a Class instance
 * due to type-erasure.
 * 
 * @author matt.deboer@gmail.com
 *
 * @param <T>
 */
public abstract class TypeFactory<T> implements ParameterizedType {
    
	// TODO: problem: we need a weak reference map to properly handle
//    private static final ConcurrentHashMap<byte[],Type<?>> typeCache =
//    		new ConcurrentHashMap<byte[],Type<?>>();
    private static final CustomConcurrentHashMap<byte[],Type<?>> typeCache =
    		new CustomConcurrentHashMap<byte[],Type<?>>(
    				CustomConcurrentHashMap.STRONG,
    				CustomConcurrentHashMap.EQUALS, 
    				CustomConcurrentHashMap.WEAK, 
    				CustomConcurrentHashMap.EQUALS,
    				0);
    
    /**
     * @param value
     * @param bytes
     * @param startIndex
     */
    private static final void intToByteArray(int value, byte[] bytes, int startIndex) {
    	int i = startIndex * 4;
    	bytes[i] = (byte)(value >>> 24);
    	bytes[i+1] = (byte)(value >>> 16);
    	bytes[i+2] = (byte)(value >>> 8);
    	bytes[i+3] = (byte)(value);
    }
    
    private static final byte[] getIdentityHashBytes(Class<?> rawType,java.lang.reflect.Type[] typeArguments) {
    	byte[] identityHashBytes = new byte[(typeArguments.length+1)*4];
    	intToByteArray(System.identityHashCode(rawType), identityHashBytes, 0);
    	for (int i=0, len = typeArguments.length; i < len; ++i) {
    		intToByteArray(System.identityHashCode(typeArguments[i]), identityHashBytes, i+1);
    	}
    	return identityHashBytes;
    }
    
    @SuppressWarnings("unchecked")
    private static <T> Type<T> intern(Class<T> rawType, java.lang.reflect.Type...typeArguments) {
    	
    	byte[] typeKey = getIdentityHashBytes(rawType,typeArguments);
    	Type<T> mapped = (Type<T>) typeCache.get(typeKey);
        if (mapped == null) {
        	mapped = new Type<T>(rawType, typeArguments);
        	Type<T> existing = (Type<T>) typeCache.putIfAbsent(typeKey, mapped);
        	if (existing != null) {
        		mapped = existing;
        	}
        }
        return mapped;
    }
    
    @SuppressWarnings("unchecked")
	public static <E> Type<E> valueOf(final Class<? extends E> rawType) {
    	if (rawType==null) {
    		return null;
    	} else if (rawType.getGenericSuperclass() instanceof ParameterizedType) {
			ParameterizedType genericSuper = (ParameterizedType)rawType.getGenericSuperclass();
			return valueOf(rawType, genericSuper.getActualTypeArguments());
		} else if (rawType.getTypeParameters().length > 0 ) {
			java.lang.reflect.Type[] actualTypeArguments = rawType.getTypeParameters().clone();
			for (java.lang.reflect.Type interfaceType: rawType.getGenericInterfaces()) {
				if (interfaceType instanceof ParameterizedType) {
					resolveTypeArgumentsFromInterfaces(actualTypeArguments, rawType, (ParameterizedType) interfaceType);
				}
			}
			return valueOf(rawType, actualTypeArguments);
		} else {
			return (Type<E>)intern(rawType);
		}
    }
    
    @SuppressWarnings("unchecked")
	public static <E> Type<E> valueOf(final Class<? extends E> rawType, final java.lang.reflect.Type... actualTypeArguments) {
        if (rawType == null) {
            return null;
        } else if (actualTypeArguments.length  == 0) {
        	// shouldn't ever reach this point, due to java method resolution
        	return (Type<E>) intern((Class<E>)rawType);
        } else {
            return (Type<E>) intern((Class<E>) rawType, actualTypeArguments);
        }
    }
    
    public static <T> Type<T> valueOf(final Type<T> type) {
    	return type;
    }
    
    @SuppressWarnings("unchecked")
	public static <T> Type<T> valueOf(final ParameterizedType type) {
    	return valueOf((Class<? extends T>)type.getRawType(), type.getActualTypeArguments());
    }
    
    @SuppressWarnings("unchecked")
	public static <T> Type<T> valueOf(final ParameterizedType type, final Type<?> referenceType) {
    	if (type==null) {
    		return null;
    	} else {
        	java.lang.reflect.Type[] actualTypeArguments = TypeUtil.resolveActualTypeArguments(type, referenceType);
    		Type<T> result = intern((Class<T>)type.getRawType(), actualTypeArguments);
      	
        	return result;
    	}
    }
    
    public static <T> Type<T> valueOf(final Class<T> type, final Type<?> referenceType) {
    	if (type==null) {
    		return null;
    	} else {
    		if (type.getTypeParameters()!=null && type.getTypeParameters().length > 0) {
    			java.lang.reflect.Type[] actualTypeArguments = TypeUtil.resolveActualTypeArguments(type.getTypeParameters(), referenceType);
    			return valueOf(type, actualTypeArguments);	
    		} else {
    			return valueOf(type);
    		}
    	}
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Type<T> typeOf(final T object) {
        return valueOf((Class<T>)object.getClass());
    }
    
    @SuppressWarnings("unchecked")
	public static <T> Type<T> typeOf(final T object, Type<?> referenceType) {
    	return valueOf((Class<T>)object.getClass(), referenceType);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Type<T> componentTypeOf(final T[] object) {
        return valueOf((Class<T>)object.getClass().getComponentType());
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Type<T> componentTypeOf(final Iterable<T> object) {
        return valueOf((Class<T>)object.iterator().next().getClass());
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Type<T> typeOf(final T object, final java.lang.reflect.Type[] actualTypeArguments) {
        return valueOf((Class<T>)object.getClass(),actualTypeArguments);
    }
    
    public static void clearCache() {
    	typeCache.clear();
    }
    
    private static void resolveTypeArgumentsFromInterfaces(java.lang.reflect.Type[] actualTypeArguments, Class<?> rawType, ParameterizedType interfaceType) {
    
    	Set<Method> methods = new HashSet<Method>(Arrays.asList(((Class<?>)interfaceType.getRawType()).getDeclaredMethods()));
    	for (java.lang.reflect.Type type: actualTypeArguments) {
    		if (type instanceof TypeVariable<?>) {
    			TypeVariable<?> variable = (TypeVariable<?>)type;
    			// needs resolution
    			for (Method m: methods) {
    	    		try {
						Method implementedMethod = rawType.getMethod(m.getName(), m.getParameterTypes());
						for (java.lang.reflect.Type paramType: implementedMethod.getGenericParameterTypes()) {
							if (paramType.equals(variable)) {
								
							}
						}
						java.lang.reflect.Type returnType = m.getGenericReturnType();
						if (returnType instanceof ParameterizedType) {
							
						} else if (returnType instanceof Class) {
							
						}
    	    		
    	    		} catch (NoSuchMethodException e) {
						continue;
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    	    		
    			}
    		}
	    		
    	}
    	
    	
    }
    
}

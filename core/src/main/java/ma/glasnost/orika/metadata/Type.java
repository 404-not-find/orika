package ma.glasnost.orika.metadata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ma.glasnost.orika.impl.util.ClassUtil;

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
public abstract class Type<T> implements ParameterizedType {
    
	private static abstract class TypeGenerator<T> {
		
		private java.lang.reflect.Type[] typeArgs;
		private Class<T> rawType;
		
		@SuppressWarnings("unchecked")
		public TypeGenerator() {
			java.lang.reflect.Type t = getClass().getGenericSuperclass();
			if (!(t instanceof ParameterizedType)) {
				throw new RuntimeException("Invalid TypeGenerator; must specify type parameters");
			}
			ParameterizedType pt = (ParameterizedType) t;
			if (pt.getRawType() != TypeGenerator.class) {
				throw new RuntimeException("Invalid TypeGenerator; must directly extend TypeGenerator");
			}
			this.typeArgs = pt.getActualTypeArguments();
			this.rawType = (Class<T>)pt.getRawType();
		}
		
		public java.lang.reflect.Type[] getActualTypeArguments() {
			return typeArgs;
		}
		
		public Class<T> getRawType() {
			return rawType;
		}
	}
	
	public static <T> Type<T> create() {
		TypeGenerator<T> instance = new TypeGenerator<T>(){};
		return Type.valueOf(instance.getRawType(), instance.getActualTypeArguments());
	}
	
	
    private static final ConcurrentHashMap<byte[],Type<?>> typeCache =
    		new ConcurrentHashMap<byte[],Type<?>>();

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
        	mapped = new TypeImpl<T>(rawType, typeArguments);
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
    
    private final Class<T> rawType;
    //private java.lang.reflect.Type ownerType;
    private final Type<?>[] actualTypeArguments;
    private final boolean isParameterized;
    private Map<TypeVariable<?>, java.lang.reflect.Type> typesByVariable;
    private Type<?> superType;
    private Type<?>[] interfaces;
    private Type<?> componentType;
    

    @SuppressWarnings("unchecked")
    public Type() {
       
        ParameterizedType parameterizedType = (ParameterizedType)getClass().getGenericSuperclass();
        java.lang.reflect.Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof ParameterizedType) {
            parameterizedType = (ParameterizedType)type;
            this.rawType = (Class<T>)parameterizedType.getRawType();
            this.actualTypeArguments = new Type<?>[rawType.getTypeParameters().length];
            this.isParameterized = rawType.getTypeParameters().length > 0;
            if (isParameterized) {
                resolveTypeArguments(parameterizedType.getActualTypeArguments());
            }
        } else {
            this.rawType = (Class<T>)type;
            this.isParameterized = false;
            this.actualTypeArguments = new Type<?>[0];
        }
    }
    

    @SuppressWarnings("unchecked")
    public Type(Class<?> rawType, java.lang.reflect.Type... actualTypeArguments) {
        this.rawType = (Class<T>)rawType;
        this.actualTypeArguments = new Type<?>[rawType.getTypeParameters().length];
        this.isParameterized = rawType.getTypeParameters().length > 0;
        
        if (isParameterized) {
            resolveTypeArguments(actualTypeArguments);
        }
    }
    
    
    @SuppressWarnings("unchecked")
    private void resolveTypeArguments(java.lang.reflect.Type[] actualTypeArguments) {
        this.typesByVariable = new HashMap<TypeVariable<?>, java.lang.reflect.Type>(this.actualTypeArguments.length);
        TypeVariable<?>[] typeVariables = this.rawType.getTypeParameters();
        if (actualTypeArguments.length == 0) {
            for (TypeVariable<?> typeVariable: typeVariables) {
                this.typesByVariable.put(typeVariable, typeVariable);
            }
        } else if (actualTypeArguments.length < this.actualTypeArguments.length) {
            throw new IllegalArgumentException("Must provide all type-arguments or none");
        } else {
        
            for (int i=0, len=actualTypeArguments.length; i < len; ++i) {
                java.lang.reflect.Type t = actualTypeArguments[i];
                if (t instanceof Type) {
                //    ((Type<?>) t).setOwnerType(this);
                } else if (t instanceof Class) {
                    t = Type.valueOf((Class<Object>) t);
                } else if (t instanceof ParameterizedType){
                    t = Type.valueOf((ParameterizedType)t);
                } else {
                    t = Type.valueOf(Object.class);
                }
                this.actualTypeArguments[i] = (Type<?>)t;
                typesByVariable.put(typeVariables[i], t);
            }
        }
    }
    
    public boolean isParameterized() {
        return isParameterized;
    }
    
    private Type<?> resolveGenericAncestor(java.lang.reflect.Type ancestor) {
    	Type<?> resolvedType = null;
		if (ancestor instanceof ParameterizedType) {
			resolvedType = Type.valueOf((ParameterizedType)ancestor, this);
		} else if (ancestor instanceof Class) {
			resolvedType = Type.valueOf((Class<?>)ancestor);
		} else {
			throw new IllegalStateException("super-type of " + this.toString() + 
					" is neither Class, nor ParameterizedType, but " + ancestor);
		}
		return resolvedType;
    }
    
    
    public Type<?> getSuperType() {
    	if (this.superType == null) {
    		synchronized(this) {
	    		if (this.superType == null) {
	    			this.superType = resolveGenericAncestor(rawType.getGenericSuperclass());
	    		}
    		}
    	}
        return this.superType;
    }
    
    public Type<?>[] getInterfaces() {
    	if (this.interfaces == null) {
    		synchronized(this) {
	    		if (this.interfaces == null) {
	    			this.interfaces = new Type<?>[rawType.getGenericInterfaces().length];
		    		int i=0;
		    		for (java.lang.reflect.Type interfaceType: rawType.getGenericInterfaces()) {
		    			this.interfaces[i++] = resolveGenericAncestor(interfaceType);
		    		}
	    		}
    		}
    	}
    	return interfaces;
    }

    public java.lang.reflect.Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }
    
    public java.lang.reflect.Type getTypeByVariable(TypeVariable<?> typeVariable) {
        if (isParameterized) {
            return typesByVariable.get(typeVariable);
        } else {
            return null;
        }
    }
    
    public Class<T> getRawType() {
        return rawType;
    }
    
    public Type<?> getComponentType() {
        if (componentType == null) {
            componentType = rawType.isArray() ? Type.valueOf(rawType.getComponentType()) : Type.valueOf(rawType);
        }
        return componentType;
    }
    
    public java.lang.reflect.Type getOwnerType() {
    	throw new UnsupportedOperationException();
    }
    
    public String getSimpleName() {
        return this.rawType.getSimpleName();
    }
    
    public String getName() {
        return this.rawType.getName();
    }
    
    public String getCanonicalName() {
        return this.rawType.getCanonicalName();
    }
    
    public boolean isAssignableFrom(Type<?> other) {
        if (other==null) {
            return false;
        }
        if (!this.getRawType().isAssignableFrom(other.getRawType())) {
            return false;
        }
        if (this.getActualTypeArguments().length!=other.getActualTypeArguments().length) {
            return false;
        }
        java.lang.reflect.Type[] thisTypes = this.getActualTypeArguments();
        java.lang.reflect.Type[] thatTypes = other.getActualTypeArguments();
        for (int i=0, total=thisTypes.length; i < total; ++i ) {
            if (thisTypes[i] instanceof Type && thatTypes[i] instanceof Type) {
            	if (!((Type<?>)thisTypes[i]).isAssignableFrom((Type<?>)thatTypes[i])) {
            		return false;
            	}
            } else if (thisTypes[i] instanceof Type) {
            	return false;
            } else if (thatTypes[i] instanceof Type) {
            	// TODO: consider using GentyRef here...
            	return false;
            } else {
            	// TODO: consider using GentyRef here as well...
            }
        }
        return true;
        
    }
    
    public boolean isAssignableFrom(Class<?> other) {
    	if (other==null) {
            return false;
        }
        if (this.isParameterized()) {
            return false;
        }
        return this.getRawType().isAssignableFrom(other);
    }
    
    public boolean isResolved() {
    	return true;
    }
    
    public boolean isImmutable() {
    	return ClassUtil.isImmutable(getRawType());
    }
    
    public boolean isEnum() {
    	return getRawType().isEnum();
    }
   
    public boolean isPrimitive() {
    	return getRawType().isPrimitive();
    }
    
    public boolean isPrimitiveWrapper() {
    	return ClassUtil.isPrimitiveWrapper(getRawType());
    }
    
    public boolean isConvertibleFromString() {
    	return ClassUtil.isConvertibleFromString(getRawType());
    }
    
    public String toString() {
    	StringBuilder stringValue = new StringBuilder();
    	stringValue.append(rawType.getSimpleName());
    	if (actualTypeArguments.length > 0) {
    		stringValue.append("<");
    		for (java.lang.reflect.Type arg: actualTypeArguments) {
    			stringValue.append(""+arg + ", ");
    		}
    		stringValue.setLength(stringValue.length()-2);
    		stringValue.append(">");
    	}
    	
    	return stringValue.toString();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(actualTypeArguments);
        result = prime * result + ((rawType == null) ? 0 : rawType.hashCode());
        return result;
    }
    
    /**
     * Equals comparison when the other type is known to be an instance of Type
     * 
     * @param other
     * @return
     */
    public boolean isEqualTo(Type<?> other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        
        if (!Arrays.equals(actualTypeArguments, other.actualTypeArguments)) {
            return false;
        }
        
        if (rawType == null) {
            if (other.rawType != null) {
                return false;
            }
        } else if (!rawType.equals(other.rawType)) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Type<?> other = (Type<?>) obj;
        
        if (!Arrays.equals(actualTypeArguments, other.actualTypeArguments)) {
            return false;
        }
        
        if (rawType == null) {
            if (other.rawType != null) {
                return false;
            }
        } else if (!rawType.equals(other.rawType)) {
            return false;
        }
        return true;
    }
    
    public static class TypeImpl<T> extends Type<T> { 
        
        public TypeImpl(Class<?> rawType, java.lang.reflect.Type...actualTypeArguments) {
            super(rawType,actualTypeArguments);
        }
        
    }
}

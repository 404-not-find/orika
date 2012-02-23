package ma.glasnost.orika.metadata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ma.glasnost.orika.impl.util.ClassUtil;

/**
 * TypeHolder is an implementation of ParameterizedType which may be
 * used in various mapping methods where a Class instance would normally
 * be used, in order to provide more specific details as to the actual types
 * represented by the generic template parameters in a given class.<br><br>
 * Such details are not normally available at runtime using a Class instance
 * due to type-erasure.
 * 
 * @author matt.deboer@gmail.com
 *
 * @param <T>
 */
public class TypeHolder<T> implements ParameterizedType {
    
    private static final ConcurrentHashMap<Class<?>,TypeHolder<?>> basicTypes = 
            new ConcurrentHashMap<Class<?>,TypeHolder<?>>();
    
    @SuppressWarnings("unchecked")
    private static <T> TypeHolder<T> intern(Class<T> rawType) {
        TypeHolder<T> mapped = (TypeHolder<T>) basicTypes.get(rawType);
        if (mapped == null) {
            synchronized(rawType) {
                mapped = (TypeHolder<T>) basicTypes.get(rawType);
                if (mapped == null) {
                    mapped = new TypeHolder<T>(rawType);
                }
            }
        }
        return mapped;
    }
    
    @SuppressWarnings("unchecked")
	public static <E> TypeHolder<E> valueOf(final Class<? extends E> rawType) {
    	if (rawType==null) {
    		return null;
    	} else if (rawType.getGenericSuperclass() instanceof ParameterizedType) {
			ParameterizedType genericSuper = (ParameterizedType)rawType.getGenericSuperclass();
			return valueOf(rawType, genericSuper.getActualTypeArguments());
		} else {
			return (TypeHolder<E>)intern(rawType);
		}
    }
    
    @SuppressWarnings("unchecked")
	public static <E> TypeHolder<E> valueOf(final Class<? extends E> rawType, final Type... actualTypeArguments) {
        if (rawType == null) {
            return null;
        } else if (actualTypeArguments.length  == 0) {
        	// shouldn't ever reach this method, due to java method resolution
        	return valueOf(rawType);
        } else {
            return (TypeHolder<E>) new TypeHolder<E>((Class<E>) rawType, actualTypeArguments);
        }
    }
    
    @SuppressWarnings("unchecked")
	public static <T> TypeHolder<T> valueOf(final ParameterizedType type) {
    	if (type==null) {
    		return null;
    	}
    	
    	for (Type typeArg : type.getActualTypeArguments()) {
    		if (typeArg instanceof TypeVariable) {
    			throw new IllegalArgumentException("type has non-actual type arguments");
    		}
    	}
    	
		TypeHolder<T> result = new TypeHolder<T>((Class<T>)type.getRawType(), type.getActualTypeArguments());
    	Type ownerType = type.getOwnerType();
    	if (ownerType instanceof ParameterizedType) {
    		result.setOwnerType(valueOf((ParameterizedType)ownerType));
    	} else if (ownerType instanceof Class) {
    		result.setOwnerType(valueOf((Class<T>)ownerType));
    	}
    	
    	return result;
    }
    
    @SuppressWarnings("unchecked")
	public static <T> TypeHolder<T> typeOf(final T object, final Type...actualTypeArguments) {
    	return valueOf((Class<T>)object.getClass(),actualTypeArguments);
    }
    
    private final Class<T> rawType;
    private Type ownerType;
    private final TypeHolder<?>[] actualTypeArguments;
    private final boolean isParameterized;
    private Map<String, TypeHolder<?>> typesByVariable;
    private TypeHolder<?> superType;
    
    @SuppressWarnings("unchecked")
    public TypeHolder(Class<?> rawType, Type... actualTypeArguments) {
        this.rawType = (Class<T>)rawType;
        this.actualTypeArguments = new TypeHolder<?>[actualTypeArguments.length];
        this.isParameterized = actualTypeArguments.length > 0;
        
        if (isParameterized) {
            
            this.typesByVariable = new HashMap<String, TypeHolder<?>>(actualTypeArguments.length);
            TypeVariable<?>[] typeVariables = this.rawType.getTypeParameters();
            int i = -1;
            for (Type t : actualTypeArguments) {
                if (t instanceof TypeHolder) {
                    ((TypeHolder<?>) t).setOwnerType(this);
                } else {
                    t = new TypeHolder<Object>((Class<Object>) t);
                }
                this.actualTypeArguments[++i] = (TypeHolder<?>)t;
                typesByVariable.put(typeVariables[i].getName(), (TypeHolder<?>) t);
            }
        }
        
        Type superType = rawType.getGenericSuperclass();
        if (superType instanceof ParameterizedType) {
            Type[] actualTypes = ((ParameterizedType)superType).getActualTypeArguments();
            if (actualTypes!=null && actualTypes.length > 0) {
                
            }
        }
    }
    
    public boolean isParameterized() {
        return isParameterized;
    }
    
    public TypeHolder<?> getSuperType() {
        return superType;
    }
    
    public TypeHolder<?>[] getActualTypeArguments() {
        return actualTypeArguments;
    }
    
    public TypeHolder<?> getTypeByVariable(String typeVariable) {
        if (isParameterized) {
            return typesByVariable.get(typeVariable);
        } else {
            return null;
        }
    }
    
    public Class<T> getRawType() {
        return rawType;
    }
    
    public Type getOwnerType() {
        return ownerType;
    }
    
    void setOwnerType(TypeHolder<?> ownerType) {
        this.ownerType = ownerType;
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
    
    public boolean isAssignableFrom(TypeHolder<?> other) {
        if (other==null) {
            return false;
        }
        if (!this.getRawType().isAssignableFrom(other.getRawType())) {
            return false;
        }
        if (this.getActualTypeArguments().length!=other.getActualTypeArguments().length) {
            return false;
        }
        TypeHolder<?>[] thisTypes = this.getActualTypeArguments();
        TypeHolder<?>[] thatTypes = other.getActualTypeArguments();
        for (int i=0, total=thisTypes.length; i < total; ++i ) {
            if(!thisTypes[i].isAssignableFrom(thatTypes[i])) {
                return false;
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
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(actualTypeArguments);
        result = prime * result + ((rawType == null) ? 0 : rawType.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TypeHolder<?> other = (TypeHolder<?>) obj;
        
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
    
}

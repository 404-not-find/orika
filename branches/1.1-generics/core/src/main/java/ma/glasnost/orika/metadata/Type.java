package ma.glasnost.orika.metadata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
public final class Type<T> implements ParameterizedType {
    
    private final Class<T> rawType;
    //private java.lang.reflect.Type ownerType;
    private final Type<?>[] actualTypeArguments;
    private final boolean isParameterized;
    private Map<TypeVariable<?>, java.lang.reflect.Type> typesByVariable;
    private Type<?> superType;
    private Type<?>[] interfaces;
    private Type<?> componentType;

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
			resolvedType = TypeFactory.valueOf((ParameterizedType)ancestor, this);
		} else if (ancestor instanceof Class) {
			resolvedType = TypeFactory.valueOf((Class<?>)ancestor);
		} else {
			throw new IllegalStateException("super-type of " + this.toString() + 
					" is neither Class, nor ParameterizedType, but " + ancestor);
		}
		return resolvedType;
    }
    
    
    /**
     * @return the direct super-type of this type, with type arguments resolved with 
     * respect to the actual type arguments of this type.
     * 
     */
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
    
    /**
     * @return the interfaces implemented by this type, with type arguments resolved with
     * respect to the actual type arguments of this type.
     */
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

    /* (non-Javadoc)
     * @see java.lang.reflect.ParameterizedType#getActualTypeArguments()
     */
    public java.lang.reflect.Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }
    
    public Map<TypeVariable<?>, java.lang.reflect.Type> getTypesByVariable() {
    	return Collections.unmodifiableMap(typesByVariable);
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
            componentType = rawType.isArray() ? TypeFactory.valueOf(rawType.getComponentType()) : TypeFactory.valueOf(rawType);
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
            Type<?> thisType = (Type<?>)thisTypes[i];
            Type<?> thatType = (Type<?>)thatTypes[i];
            // TODO: this may not actually be correct according to generic
            // specification rules...
        	if (!thisType.isAssignableFrom(thatType)) {
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
}

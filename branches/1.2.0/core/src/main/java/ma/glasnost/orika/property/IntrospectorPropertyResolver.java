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

package ma.glasnost.orika.property;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ma.glasnost.orika.metadata.NestedProperty;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * IntrospectionPropertyResolver leverages JavaBeans introspector to resolve
 * properties for provided types.<br>
 * 
 * @author
 * 
 */
public class IntrospectorPropertyResolver implements PropertyResolverStrategy {
    
    private final Map<java.lang.reflect.Type, Map<String, Property>> propertiesCache = new ConcurrentHashMap<java.lang.reflect.Type, Map<String, Property>>();
    
    public Map<String, Property> getProperties(java.lang.reflect.Type theType) {
        
    	Map<String, Property> properties = propertiesCache.get(theType);
        if (properties == null) {
        	synchronized(theType) {
	        	properties = propertiesCache.get(theType);
	        	if (properties == null) {
	        		
			        properties = new LinkedHashMap<String, Property>();
			        Type<?> typeHolder;
			        
			        if (theType instanceof Type) {
			            typeHolder = (Type<?>) theType;
			        } else if (theType instanceof Class) {
			            typeHolder = TypeFactory.valueOf((Class<?>) theType);
			        } else {
			            throw new IllegalArgumentException("type " + theType + " not supported.");
			        }
			        
			        BeanInfo beanInfo;
			        PropertyDescriptor currentPd = null;
			        try {
			            LinkedList<Class<? extends Object>> types = new LinkedList<Class<? extends Object>>();
			            types.addFirst((Class<? extends Object>) typeHolder.getRawType());
			            while (!types.isEmpty()) {
			                Class<? extends Object> type = types.removeFirst();
			                beanInfo = Introspector.getBeanInfo(type);
			                PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
			                for (final PropertyDescriptor pd : descriptors) {
			                	currentPd = pd;
			                	try {
			                        
			                        final Property property = new Property();
			                        final String capitalName = pd.getName().substring(0,1).toUpperCase() + pd.getName().substring(1);
			                        Method readMethod;
			                        if (pd.getReadMethod() == null && Boolean.class.equals(pd.getPropertyType())) {
			                        	/*
			                             * Special handling for Boolean "is" read method; not compliant with JavaBeans spec, but still very common
			                             */
			                        	try {
			                        		readMethod = type.getMethod("is" + capitalName);
			                        	} catch (NoSuchMethodException e) {
			                        		readMethod = null;
			                        	}
			                        } else {
			                        	readMethod = pd.getReadMethod();
			                        }
			                        Method writeMethod = pd.getWriteMethod();
			                        
			                        property.setExpression(pd.getName());
			                        property.setName(pd.getName());
			                        
			                        
			                        if (readMethod != null) {
			                            property.setGetter(readMethod.getName() + "()");
			                        }
			                        if (writeMethod != null) {
			                            property.setSetter(writeMethod.getName() + "(%s)");
			                        } 
			                        
			                        if (readMethod == null && writeMethod == null) {
			                            continue;
			                        }
			                        
			                        Class<?> rawType = resolveRawPropertyType(pd);
			                        
			                        if (typeHolder.isParameterized() 
			                        		|| type.getTypeParameters().length > 0 
			                        		|| rawType.getTypeParameters().length > 0) {
			                            /*
			                             * Make attempts to determine the parameters
			                             */
			                            Type<?> resolvedGenericType = null;
			                            if (readMethod != null) {
			                                try {
												resolvedGenericType = resolveGenericType(readMethod.getDeclaringClass()
												        .getDeclaredMethod(readMethod.getName(), new Class[0])
												        .getGenericReturnType(), typeHolder);
											} catch (NoSuchMethodException e) {
												throw new IllegalStateException("readMethod does not exist",e);
											}
			                            }
			                            
			                            if (resolvedGenericType != null && !resolvedGenericType.isAssignableFrom(rawType)) {
			                                property.setType(resolvedGenericType);
			                            } else {
			                                property.setType(TypeFactory.valueOf(rawType));
			                            }
			                            
			                        } else {
			                            /*
			                             * Neither the type nor it's parameter is generic; use the raw type
			                             */
			                            property.setType(TypeFactory.valueOf(rawType));
			                        }
			                        
			                        if (writeMethod == null) {
			                            /*
			                             * Special handling for fluid APIs where setters return a value
			                             */
			                            try {
			                                writeMethod = type.getMethod("set" + capitalName, property.getType().getRawType());
			                                property.setSetter(writeMethod.getName() + "(%s)");
			                            } catch (NoSuchMethodException e) {
			                                writeMethod = null;
			                            }
			                        }
			
			                        
			                        Property existing = properties.get(pd.getName());
			                        if (existing == null) {
			                            properties.put(pd.getName(), property);
			                        } else if (existing.getType().isAssignableFrom(property.getType()) && !existing.getType().equals(property.getType())) {
			                            /*
			                             * The type has been refined by the generic information in a super-type
			                             */
			                            existing.setType(property.getType());
			                        }
			                        
			                    } catch (final RuntimeException e) {
			                    	/*
			                    	 * Wrap with info for the property we were trying to introspect 
			                    	 */
			                    	throw new RuntimeException("Unexpected error while trying to resolve property " + theType + ", [" + pd.getName() + "]", e);
			                    }
			                }
			                
			                if (type.getSuperclass() != null && !Object.class.equals(type.getSuperclass())) {
			                    types.add(type.getSuperclass());
			                }
			                
			                List<? extends Class<? extends Object>> interfaces = Arrays.<Class<? extends Object>> asList(type.getInterfaces());
			                types.addAll(interfaces);
			            }
			        
			        } catch (final IntrospectionException e) {
			            if (currentPd != null) {
			            	throw new RuntimeException("Unexpected error while trying to resolve property " + theType + ", [" + currentPd.getName() + "]", e);
			            } else {
			            	throw new RuntimeException("Unexpected error while trying to resolve properties for " + theType, e);
			            }
			        }
			        
			        /*
			         * Add public non-static fields as properties; we call this outside of
			         * the loop because the fields returned are already inclusive of
			         * ancestors.
			         */
			        for (Field f : typeHolder.getRawType().getFields()) {
			            if (!Modifier.isStatic(f.getModifiers())) {
			                final Property property = new Property();
			                property.setExpression(f.getName());
			                property.setName(f.getName());
			                
			                Class<?> rawType = f.getType();
			                Type<?> genericType = resolveGenericType(f.getGenericType(), typeHolder);
			                if (genericType != null && !genericType.isAssignableFrom(rawType)) {
			                    property.setType(genericType);
			                } else {
			                    property.setType(TypeFactory.valueOf(rawType));
			                }
			                
			                Property existing = properties.get(property.getName());
			                if (existing == null) {
			                    property.setGetter(property.getName());
			                    property.setSetter(property.getName() + " = %s");
			                    properties.put(property.getName(), property);
			                } else if (existing.getSetter() == null) {
			                    existing.setSetter(property.getName() + " = %s");
			                }
			            }
			        }
		        
			        propertiesCache.put(theType, Collections.unmodifiableMap(properties));
	        	}
        	}
        }
        return properties;
    }
    
    /**
     * Attempt to resolve the generic type, using refereceType to resolve  
     * TypeVariables
     * 
     * @param genericType the type to resolve
     * @param referenceType the reference type to use for lookup of type variables
     * @return
     */
    private Type<?> resolveGenericType(java.lang.reflect.Type genericType, Type<?> referenceType) {
    	Type<?> resolvedType = null;
        Type<?> reference = referenceType;
        do {
	        if (genericType instanceof TypeVariable && reference.isParameterized()) {
	            java.lang.reflect.Type t = reference.getTypeByVariable((TypeVariable<?>) genericType);
	            if (t != null) {
	                resolvedType = TypeFactory.valueOf(t);
	            }
	        } else if (genericType instanceof ParameterizedType) {
	            if (reference.isParameterized()) {
	                resolvedType = TypeFactory.resolveValueOf((ParameterizedType) genericType, reference);
	            } else {
	                resolvedType = TypeFactory.valueOf((ParameterizedType) genericType);
	            }
	        }
	        reference = reference.getSuperType();
        } while (resolvedType == null && reference != TypeFactory.TYPE_OF_OBJECT);
        return resolvedType;
    }
    
    /**
     * Resolves the raw property type from a property descriptor;
     * if a read method is available, use it to refine the type.
     * The results of pd.getPropertyType() are sometimes inconsistent across
     * platforms.
     * 
     * @param pd
     * @return
     */
    private Class<?> resolveRawPropertyType(PropertyDescriptor pd) {
    	Class<?> rawType = pd.getPropertyType();
    	try {
	        return ( pd.getReadMethod() == null ? rawType :
	        pd.getReadMethod().getDeclaringClass()
	        .getDeclaredMethod(pd.getReadMethod().getName(), new Class[0])
	        .getReturnType());
        } catch (Exception e) {
	        return rawType;
        } 
    }
    
    public NestedProperty getNestedProperty(java.lang.reflect.Type type, String p) {
        
        String typeName = type.toString();
        Map<String, Property> properties = getProperties(type);
        Property property = null;
        final List<Property> path = new ArrayList<Property>();
        if (p.indexOf('.') != -1) {
            final String[] ps = p.split("\\.");
            int i = 0;
            while (i < ps.length) {
                if (!properties.containsKey(ps[i])) {
                    throw new RuntimeException("could not resolve nested property [" + p + "] on " + type + ", because "
                            + type + " does not contain property [" + ps[i] + "]");
                }
                property = properties.get(ps[i]);
                properties = getProperties(property.getType());
                i++;
                if (i < ps.length) {
                    path.add(property);
                }
            }
        }
        
        if (property == null) {
            throw new RuntimeException(typeName + " does not contain property [" + p + "]");
        }
        
        return new NestedProperty(p, property, path.toArray(new Property[path.size()]));
    }
}

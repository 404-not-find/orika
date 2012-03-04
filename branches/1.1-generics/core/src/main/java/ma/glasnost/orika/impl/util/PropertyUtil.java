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

package ma.glasnost.orika.impl.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ma.glasnost.orika.metadata.NestedProperty;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;

public final class PropertyUtil {
    
    private static final Map<java.lang.reflect.Type, Map<String, Property>> PROPERTIES_CACHE = new ConcurrentHashMap<java.lang.reflect.Type, Map<String, Property>>();
    
    private PropertyUtil() {
        
    }
   
    /**
     * Gets properties for a potentially parameterized type.
     * Support for both Class and ParameterizedType inputs.
     * 
     * @param theType
     * @return
     */
    public static Map<String, Property> getProperties(java.lang.reflect.Type theType) {
        
        if (PROPERTIES_CACHE.containsKey(theType)) {
            return PROPERTIES_CACHE.get(theType);
        }
        
        final Map<String, Property> properties = new HashMap<String, Property>();
        Type<?> typeHolder;
        if (theType instanceof Type) {
        	typeHolder = (Type<?>)theType;
        } else if (theType instanceof Class) {
        	typeHolder = Type.valueOf((Class<?>)theType);
        } else {
            throw new IllegalArgumentException("type " + theType + " not supported.");
        }
        BeanInfo beanInfo;
        try {
            LinkedList<Class<? extends Object>> types = new LinkedList<Class<? extends Object>>();
            types.push((Class<? extends Object>)typeHolder.getRawType());
            while(!types.isEmpty()) {
                Class<? extends Object> type = types.pop();
                beanInfo = Introspector.getBeanInfo(type);
                PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
                for (final PropertyDescriptor pd : descriptors) {
                    try {
                        
                        final Property property = new Property();
                        property.setExpression(pd.getName());
                        property.setName(pd.getName());
                        if (pd.getReadMethod() != null) {
                            property.setGetter(pd.getReadMethod().getName());
                        }
                        if (pd.getWriteMethod() != null) {
                            property.setSetter(pd.getWriteMethod().getName());
                        }
                        
                        if (pd.getReadMethod()==null && pd.getWriteMethod()==null) {
                            continue;
                        }
                        
                        Class<?> rawType = pd.getPropertyType();
                        Class<?> returnType = null;
                        java.lang.reflect.Type genericType = pd.getReadMethod().getGenericReturnType();
                        try {
                            returnType = pd.getReadMethod().getDeclaringClass()
                                    .getDeclaredMethod(property.getGetter(), new Class[0])
                                    .getReturnType();  
                        } catch (final Exception e) {
                            
                        }
                        
                        if (genericType instanceof TypeVariable && typeHolder.isParameterized()) {
                            java.lang.reflect.Type t = typeHolder.getTypeByVariable((TypeVariable<?>) genericType);
                            if (t instanceof Type) {
                            	property.setType((Type<?>)t);
                            } else if (t instanceof ParameterizedType) {
                            	property.setType(Type.valueOf((ParameterizedType)t));
                            } else {
                            	property.setType(Type.valueOf((Object.class)));
                            	//throw new IllegalStateException("unresolved property type");
                            }
                        } else if (rawType!=returnType && rawType.isAssignableFrom(returnType)) {
                        	property.setType(Type.valueOf(returnType));
                        } else if (genericType instanceof ParameterizedType) {
                        	 property.setType(Type.valueOf((ParameterizedType)genericType));
                        } else {
                        	 property.setType(Type.valueOf(rawType));
                        }
                       
                        
                        Property existing = properties.get(pd.getName());
                        if (existing==null || existing.getType().isAssignableFrom(rawType)) {
                            properties.put(pd.getName(), property);
                        
                            if (pd.getReadMethod() != null) {
                                final Method method = pd.getReadMethod();
                                if (property.getType() != null && property.isCollection()) {
                                    if (method.getGenericReturnType() instanceof ParameterizedType) {
                                        property.setParameterizedType((Class<?>) ((ParameterizedType) method.getGenericReturnType())
                                                .getActualTypeArguments()[0]);
                                    } 
                                }
                            } else if (pd.getWriteMethod() != null) {
                                final Method method = pd.getWriteMethod();
                                
                                if (property.isCollection() && method.getGenericParameterTypes().length > 0) {
                                    property.setParameterizedType((Class<?>) ((ParameterizedType) method.getGenericParameterTypes()[0])
                                            .getActualTypeArguments()[0]);
                                }
                            } else {
                                
                            }
                        }
                    } catch (final Throwable e) {
                        e.printStackTrace();
                    }
                }
                if (type.getSuperclass()!=null && !Object.class.equals(type.getSuperclass())) {
                    types.add(type.getSuperclass());
                }
                types.addAll(Arrays.asList(type.getInterfaces()));
            }
        } catch (final IntrospectionException e) {
            e.printStackTrace();
            /* Ignore */
        }
        
        PROPERTIES_CACHE.put(theType, Collections.unmodifiableMap(properties));
        return properties; 
    }
    

    public static NestedProperty getNestedProperty(java.lang.reflect.Type type, String p) {
        
        String typeName = type.toString();
        Class<?> rawType = (Class<?>)((type instanceof ParameterizedType) ? ((ParameterizedType)type).getRawType() : type);
        Map<String, Property> properties = getProperties(type);
        Property property = null;
        final List<Property> path = new ArrayList<Property>();
        if (p.indexOf('.') != -1) {
            final String[] ps = p.split("\\.");
            int i = 0;
            while (i < ps.length) {
                if (!properties.containsKey(ps[i])) {
                    throw new RuntimeException(rawType.getName() + " does not contain property [" + ps[i] + "]");
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
    
    public static boolean isExpression(String a) {
        return a.indexOf('.') != -1;
    }
}

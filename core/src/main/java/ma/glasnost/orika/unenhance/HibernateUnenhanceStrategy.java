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

package ma.glasnost.orika.unenhance;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUnenhanceStrategy implements UnenhanceStrategy {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateUnenhanceStrategy.class);
    private Method getHibernateClass;
    
    public HibernateUnenhanceStrategy() {
        try {
            Class<?> hibernate = Class.forName("org.hibernate.Hibernate", false, Thread.currentThread().getContextClassLoader());
            getHibernateClass = hibernate.getMethod("getClass", Object.class);
        } catch (ClassNotFoundException e) {
            hibernateInaccessible(e);
        } catch (NoSuchMethodException e) {
            hibernateInaccessible(e);
        } catch (SecurityException e) {
            hibernateInaccessible(e);
        }
    }
    
    private static void hibernateInaccessible(Exception e) {
        throw new ExceptionInInitializerError("org.hibernate.Hibernate is not accessible" + e);
    }
    
    private static void hibernateGetClassUnavailable(Exception e) {
        LOGGER.warn("org.hibernate.Hibernate.getClass(Object) is not available",e);
    }
    
    @SuppressWarnings("unchecked")
    public <T> Type<T> unenhanceType(T object, Type<T> type) {
        
        try {
            return TypeFactory.resolveValueOf((Class<T>)getHibernateClass.invoke(null, object), type);
        } catch (IllegalAccessException e) {
            hibernateGetClassUnavailable(e);
        } catch (IllegalArgumentException e) {
            hibernateGetClassUnavailable(e);
        } catch (InvocationTargetException e) {
            hibernateGetClassUnavailable(e);
        }
        return null;
    }
}

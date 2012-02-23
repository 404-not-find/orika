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

package ma.glasnost.orika.inheritance;

import ma.glasnost.orika.metadata.TypeHolder;


public final class SuperTypeResolver {
	
	
	@SuppressWarnings("unchecked")
    public static <T> TypeHolder<T> getSuperType(final TypeHolder<?> enhancedClass, final SuperTypeResolverStrategy strategy) {
    	
		TypeHolder<T> mappedClass = (TypeHolder<T>) enhancedClass;
    	if (strategy.shouldLookupSuperType(mappedClass.getRawType())) {
    		
    		TypeHolder<T> mappedSuper = (TypeHolder<T>)tryFirstLookupOption(mappedClass,strategy);
    		if (mappedSuper!=null) {
    			mappedClass = mappedSuper;
    		} else {
    			mappedSuper = (TypeHolder<T>) trySecondLookupOption(mappedClass,strategy);
    			if (mappedSuper!=null) {
        			mappedClass = mappedSuper;
        		}
    		}
    		
    	}
    	return mappedClass;
    }
	
	private static TypeHolder<?> tryFirstLookupOption(final TypeHolder<?> theClass, final SuperTypeResolverStrategy strategy) {
		if (strategy.shouldPreferClassOverInterface()) {
			return lookupMappedSuperType(theClass,strategy);
		} else {
			return lookupMappedInterface(theClass,strategy);
		}
	}
	
	private static TypeHolder<?> trySecondLookupOption(final TypeHolder<?> theClass, final SuperTypeResolverStrategy strategy) {
		if (strategy.shouldPreferClassOverInterface()) {
			return lookupMappedInterface(theClass,strategy);
		} else {
			return lookupMappedSuperType(theClass,strategy);
		}
	}
	
	private static TypeHolder<?> lookupMappedSuperType(final TypeHolder<?> theClass, final SuperTypeResolverStrategy strategy) { 
    	
		Class<?> targetClass = theClass.getRawType().getSuperclass();
		Class<?> mappedClass = null;
    	
    	while (mappedClass==null && targetClass!=null && !targetClass.equals(Object.class)) {
    		
    		if(strategy.accept(targetClass)) {
    			mappedClass = targetClass;
    			break;
    		} 
    		targetClass = targetClass.getSuperclass();
    	}
    	
    	return TypeHolder.valueOf(mappedClass);
    }
    
    private static TypeHolder<?> lookupMappedInterface(final TypeHolder<?> theClass, final SuperTypeResolverStrategy strategy) {
    	
    	Class<?> targetClass = theClass.getRawType();
		Class<?> mappedClass = null;
    	
		while (mappedClass==null && targetClass!=null && !targetClass.equals(Object.class)) {
	    	
    		for (Class<?> theInterface: targetClass.getInterfaces()) {
	    		if(strategy.accept(theInterface)) {
	    			mappedClass = theInterface;
	    			break;
	    		} 
    		}
    		targetClass = targetClass.getSuperclass();
		}
    	
    	return TypeHolder.valueOf(mappedClass);

    }
    
}

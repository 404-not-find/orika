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
package ma.glasnost.orika;

import ma.glasnost.orika.metadata.Type;

public interface Converter<S, D> {
    
    boolean canConvert(Type<?> sourceClass, Type<?> destinationType);
    
    D convert(S source, Type<? extends D> destinationType);
    
    public static class LegacyConverter<S,D> implements Converter<S, D> {

    	private ma.glasnost.orika.converter.Converter<S,D> delegate;
    	
    	public LegacyConverter(ma.glasnost.orika.converter.Converter<S, D> delegate) {
    		this.delegate = delegate;
    	}
    	
		@SuppressWarnings("unchecked")
		public boolean canConvert(Type<?> sourceClass,
				Type<?> destinationType) {
			
			return delegate.canConvert((Class<S>)sourceClass.getRawType(), 
					(Class<D>)destinationType.getRawType());
		}

		public D convert(S source, Type<? extends D> destinationType) {
			
			return delegate.convert(source, destinationType.getRawType());
		}
    	
    }
}

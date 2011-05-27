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

package ma.glasnost.orika.proxy;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.HibernateProxyHelper;
import org.hibernate.proxy.LazyInitializer;

public class HibernateUnenhanceStrategy implements UnenhanceStrategy {

	@SuppressWarnings("unchecked")
	public <T> T unenhanceObject(T object) {
		if (object instanceof HibernateProxy) {
			HibernateProxy hibernateProxy = (HibernateProxy) object;
			LazyInitializer lazyInitializer = hibernateProxy.getHibernateLazyInitializer();

			return (T) lazyInitializer.getImplementation();
		}
		return object;
	}

	@SuppressWarnings("unchecked")
	public <T> Class<T> unenhanceClass(T object) {
		return HibernateProxyHelper.getClassWithoutInitializingProxy(object);
	}
}

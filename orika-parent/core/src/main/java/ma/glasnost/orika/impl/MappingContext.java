package ma.glasnost.orika.impl;

import java.util.HashMap;
import java.util.Map;

public class MappingContext {

	private final Map<Class<?>, Class<?>> mapping = new HashMap<Class<?>, Class<?>>();
	private final Map<Object, Object> cache = new HashMap<Object, Object>();

	public <S, D> Class<? extends D> getConcreteClass(Class<S> sourceClass, Class<D> destinationClass) {

		Class<?> clazz = mapping.get(sourceClass);
		if (clazz != null && destinationClass.isAssignableFrom(clazz)) {
			@SuppressWarnings("unchecked")
			Class<? extends D> concreteClass = (Class<? extends D>) clazz;
			return concreteClass;
		}
		return null;
	}

	public <S, D> void cacheMappedObject(S source, D destination) {
		cache.put(source, destination);
	}

	public <S> boolean isAlreadyMapped(S source) {
		return cache.containsKey(source);
	}

	@SuppressWarnings("unchecked")
	public <S, D> D getMappedObject(S source) {
		return (D) cache.get(source);
	}
}

package ma.glasnost.orika.impl;

import java.util.HashMap;
import java.util.Map;

public class MappingContext {

	private final Map<Class<?>, Class<?>> mapping = new HashMap<Class<?>, Class<?>>();

	public <S, D> Class<? extends D> getConcreteClass(Class<S> sourceClass, Class<D> destinationClass) {

		Class<?> clazz = mapping.get(sourceClass);
		if (clazz != null && destinationClass.isAssignableFrom(clazz)) {
			@SuppressWarnings("unchecked")
			Class<? extends D> concreteClass = (Class<? extends D>) clazz;
			return concreteClass;
		}
		return null;
	}
}

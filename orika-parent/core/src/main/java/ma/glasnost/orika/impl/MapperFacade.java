package ma.glasnost.orika.impl;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.metadata.MapperKey;

public class MapperFacade implements Mapper {

	private final MapperFactory mapperFactory;

	public MapperFacade(MapperFactory mapperFactory) {
		this.mapperFactory = mapperFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ma.glasnost.orika.impl.Mapper#map(S, java.lang.Class)
	 */
	public <D, S> D map(S sourceObject, Class<D> destinationClass) {
		return map(sourceObject, destinationClass, new MappingContext());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ma.glasnost.orika.impl.Mapper#map(S, java.lang.Class,
	 * ma.glasnost.orika.impl.MappingContext)
	 */
	@SuppressWarnings("unchecked")
	public <D, S> D map(S sourceObject, Class<D> destinationClass, MappingContext context) {
		if (sourceObject == null)
			throw new MappingException("Can not map a null object.");

		if (Modifier.isAbstract(destinationClass.getModifiers())) {
			destinationClass = (Class<D>) mapperFactory.lookupConcreteDestinationClass(sourceObject.getClass(), destinationClass,
					context);
		}

		// XXX when it's immutable it's ok to copy by ref
		if (isImmutable(sourceObject) && sourceObject.getClass().equals(destinationClass))
			return (D) sourceObject;

		D destinationObject = newObject(destinationClass);

		map(sourceObject, destinationObject);
		return destinationObject;
	}

	protected void map(Object sourceObject, Object destinationObject) {
		Class<?> sourceClass = sourceObject.getClass();
		Class<?> destinationClass = destinationObject.getClass();
		while (!destinationClass.equals(Object.class)) {
			mapDeclaredProperties(sourceObject, destinationObject, sourceClass, destinationClass);
			destinationClass = destinationClass.getSuperclass();
			sourceClass = sourceClass.getSuperclass();
		}
	}

	protected void mapDeclaredProperties(Object sourceObject, Object destinationObject, Class<?> sourceClass,
			Class<?> destinationClass) {
		MapperKey mapperKey = new MapperKey(sourceClass, destinationClass);
		GeneratedMapperBase mapper = mapperFactory.get(mapperKey);

		if (mapper == null) {
			throw new IllegalStateException(String.format("Can not create a mapper for classes : %s, %s", destinationClass,
					sourceObject.getClass()));
		}

		if (mapper.getAType().equals(sourceClass)) {
			mapper.mapAtoB(sourceObject, destinationObject);
		} else if (mapper.getAType().equals(destinationClass)) {
			mapper.mapBtoA(sourceObject, destinationObject);
		} else {
			throw new IllegalStateException(String.format("Source object type's must be one of '%s' or '%s'.", mapper.getAType(),
					mapper.getBType()));
		}
	}

	private <S> boolean isImmutable(S sourceObject) {
		// XXX need to support all immutable type
		return String.class.equals(sourceObject.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ma.glasnost.orika.impl.Mapper#mapAsSet(java.lang.Iterable,
	 * java.lang.Class)
	 */
	public final <D, S> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass) {
		return (Set<D>) mapAsCollection(source, destinationClass, new HashSet<D>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ma.glasnost.orika.impl.Mapper#mapAsList(java.lang.Iterable,
	 * java.lang.Class)
	 */
	public final <D, S> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass) {
		return (List<D>) mapAsCollection(source, destinationClass, new ArrayList<D>());
	}

	protected <D, S> Collection<D> mapAsCollection(Iterable<S> source, Class<D> destinationClass, Collection<D> destination) {
		for (S item : source) {
			destination.add(map(item, destinationClass));
		}
		return destination;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ma.glasnost.orika.impl.Mapper#mapAsArray(D[], java.lang.Iterable,
	 * java.lang.Class)
	 */
	public <D, S> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass) {
		int i = 0;
		for (S s : source) {
			destination[i++] = map(s, destinationClass);
		}
		return destination;
	}

	public <D, S> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass) {
		int i = 0;
		for (S s : source) {
			destination[i++] = map(s, destinationClass);
		}
		return destination;
	}

	private <D> D newObject(Class<D> destinationClass) {

		try {
			ObjectFactory<D> objectFactory = mapperFactory.lookupObjectFactory(destinationClass);
			if (objectFactory != null) {
				return objectFactory.create();
			} else {
				return destinationClass.newInstance();
			}
		} catch (InstantiationException e) {
			throw new MappingException(e);
		} catch (IllegalAccessException e) {
			throw new MappingException(e);
		}
	}
}

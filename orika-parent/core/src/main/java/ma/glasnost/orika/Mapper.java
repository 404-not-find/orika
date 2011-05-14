package ma.glasnost.orika;

import java.util.List;
import java.util.Set;

import ma.glasnost.orika.impl.MappingContext;

public interface Mapper {

	<D, S> D map(S sourceObject, Class<D> destinationClass);

	<D, S> D map(S sourceObject, Class<D> destinationClass, MappingContext context);

	<D, S> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass);

	<D, S> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass);
}
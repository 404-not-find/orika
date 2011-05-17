package ma.glasnost.orika;

import ma.glasnost.orika.impl.MappingContext;

public interface CustomizedMapper<A, B> {

	void mapAtoB(A a, B b, MappingContext context);

	void mapBtoA(B b, A a, MappingContext context);

	void setMapperFacade(Mapper mapper);
}
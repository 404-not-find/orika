package ma.glasnost.orika;

import ma.glasnost.orika.impl.MappingContext;

public class CustomizedMapperBase<A, B> implements CustomizedMapper<A, B> {

	protected Mapper mapperFacade;

	public void mapAtoB(A a, B b, MappingContext context) {
		/* */
	}

	public void mapBtoA(B b, A a, MappingContext context) {
		/* */
	}

	public void setMapperFacade(Mapper mapper) {
		this.mapperFacade = mapper;
	}

}

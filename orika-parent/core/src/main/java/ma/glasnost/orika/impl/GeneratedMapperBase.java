package ma.glasnost.orika.impl;

import ma.glasnost.orika.CustomizedMapper;

public abstract class GeneratedMapperBase {

	protected MapperFacade mapperFacade;
	protected CustomizedMapper<Object, Object> customizedMapper;

	public abstract Class<?> getAType();

	public abstract Class<?> getBType();

	public abstract void mapAtoB(Object s, Object b, MappingContext context);

	public abstract void mapBtoA(Object s, Object d, MappingContext context);

	public static String toString(int i) {
		return Integer.toString(i);
	}

	public void setMapperFacade(MapperFacade mapperFacade) {
		this.mapperFacade = mapperFacade;
	}

	public void setCustomizedMapper(CustomizedMapper<Object, Object> customizedMapper) {
		this.customizedMapper = customizedMapper;
	}

}

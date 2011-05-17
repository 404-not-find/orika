package ma.glasnost.orika.impl;

import ma.glasnost.orika.MapperBase;
import ma.glasnost.orika.Mapper;

public abstract class GeneratedMapperBase extends MapperBase<Object, Object> {

	protected Mapper<Object, Object> customMapper;

	public void setCustomMapper(Mapper<Object, Object> customMapper) {
		this.customMapper = customMapper;
	}

	public abstract Class<?> getAType();

	public abstract Class<?> getBType();

}

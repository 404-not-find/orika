package ma.glasnost.orika.metadata;

import java.util.HashSet;
import java.util.Set;

import ma.glasnost.orika.Mapper;

public class ClassMap<A, B> {

	Class<A> aType;
	Class<B> bType;
	Set<FieldMap> fieldsMapping;
	Mapper<A, B> customizedMapper;

	public ClassMap(Class<A> aType, Class<B> bType) {
		this.aType = aType;
		this.bType = bType;

		fieldsMapping = new HashSet<FieldMap>();
	}

	public void addFieldMapping(FieldMap fieldMap) {
		fieldsMapping.add(fieldMap);
	}

	public Class<?> getAType() {
		return aType;
	}

	public Class<?> getBType() {
		return bType;
	}

	public Set<FieldMap> getFieldsMapping() {
		return fieldsMapping;
	}

	public String getATypeName() {
		return aType.getSimpleName();
	}

	public String getBTypeName() {
		return bType.getSimpleName();
	}

	public Mapper<A, B> getCustomizedMapper() {
		return customizedMapper;
	}

	public void setCustomizedMapper(Mapper<A, B> customizedMapper) {
		this.customizedMapper = customizedMapper;
	}

	@Override
	public int hashCode() {
		int result = 31;
		result = result + ((aType == null) ? 0 : aType.hashCode());
		result = result + ((bType == null) ? 0 : bType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassMap<?, ?> other = (ClassMap<?, ?>) obj;
		if (aType == null) {
			if (other.aType != null)
				return false;
		} else if (!aType.equals(other.aType))
			return false;
		if (bType == null) {
			if (other.bType != null)
				return false;
		} else if (!bType.equals(other.bType))
			return false;
		return true;
	}

}

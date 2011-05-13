package ma.glasnost.orika.metadata;

import java.util.Map;

import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.util.PropertyUtil;

public final class ClassMapBuilder<A, B> {

	private final ClassMap<A, B> classMap;
	private final Map<String, Property> aProperties;
	private final Map<String, Property> bProperties;

	private ClassMapBuilder(ClassMap<A, B> classMap) {
		this.classMap = classMap;
		aProperties = PropertyUtil.getProperties(classMap.aType);
		bProperties = PropertyUtil.getProperties(classMap.bType);
	}

	/**
	 * Map a field two way
	 * 
	 * @param a
	 *            property name in type A
	 * @param b
	 *            property name in type B
	 * @return
	 */
	public ClassMapBuilder<A, B> field(String a, String b) {
		if (aProperties.containsKey(a) && bProperties.containsKey(b)) {
			classMap.fieldsMapping.add(new FieldMap(aProperties.get(a), bProperties.get(b), true, false));
		} else {
			throw new MappingException("Can not map " + a + ", " + b + ": They do not belongs to mapping types.");
		}
		return this;
	}

	/**
	 * Exclude a field two way
	 * 
	 * @param a
	 *            property name in type A
	 * @param b
	 *            property name in type B
	 * @return
	 */
	public ClassMapBuilder<A, B> exclude(String a, String b) {
		if (aProperties.containsKey(a) && bProperties.containsKey(b)) {
			classMap.fieldsMapping.add(new FieldMap(aProperties.get(a), bProperties.get(b), true, true));
		} else {
			throw new MappingException("Can not map " + a + ", " + b + ": They do not belongs to mapping types.");
		}
		return this;
	}

	public ClassMapBuilder<A, B> byDefault() {

		for (String propertyName : aProperties.keySet()) {
			if (bProperties.containsKey(propertyName)) {
				Property a = aProperties.get(propertyName);
				Property b = bProperties.get(propertyName);
				classMap.fieldsMapping.add(new FieldMap(a, b));
			}
		}

		return this;
	}

	public ClassMap<A, B> toClassMap() {
		return classMap;
	}

	public static <A, B> ClassMapBuilder<A, B> map(Class<A> aType, Class<B> bType) {
		return new ClassMapBuilder<A, B>(new ClassMap<A, B>(aType, bType));
	}
}

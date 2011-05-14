package ma.glasnost.orika.metadata;

public class NestedProperty extends Property {

	private final Property[] path;

	public NestedProperty(Property property, Property[] path) {
		this.setType(property.getType());
		this.setGetter(property.getGetter());
		this.setSetter(property.getSetter());
		this.setParameterizedType(property.getParameterizedType());
		this.setName(property.getName());
		this.path = path;
	}

	@Override
	public Property[] getPath() {
		return path;
	}

	@Override
	public boolean hasPath() {
		return true;
	}

}

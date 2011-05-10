package ma.glasnost.orika;

import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public interface CodeGenerator {

	boolean accept(FieldMap fieldMap);

	void generate(Property sp, Property dp, StringBuilder code) throws Exception;
}
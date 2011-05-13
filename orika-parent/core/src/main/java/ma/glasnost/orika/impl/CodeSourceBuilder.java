package ma.glasnost.orika.impl;

import java.util.List;
import java.util.Set;

import ma.glasnost.orika.metadata.Property;

public class CodeSourceBuilder {
    
    private final StringBuilder out = new StringBuilder();
    
    public CodeSourceBuilder assertType(String var, Class<?> clazz) {
        append("\nif(!(" + var + " instanceof ").append(clazz.getName()).append(
                ")) throw new IllegalStateException(\"[" + var + "] is not an instance of " + clazz.getName() + " \");");
        return this;
    }
    
    public CodeSourceBuilder set(Property d, Property s) {
        append("destination.%s(source.%s());", d.getSetter(), s.getGetter());
        return this;
    }
    
    public CodeSourceBuilder set(Property d, String s) {
        append("destination.%s(%s);", d.getSetter(), s);
        return this;
    }
    
    public CodeSourceBuilder setCollection(Property dp, Property sp) {
        Class<?> destinationElementClass = dp.getParameterizedType();
        String destinationCollection = "List";
        if (List.class.isAssignableFrom(dp.getType())) {
            destinationCollection = "List";
        } else if (Set.class.isAssignableFrom(dp.getType())) {
            destinationCollection = "Set";
        }
        
        append("destination.%s(mapperFacade.mapAs%s(source.%s(), %s.class));", dp.getSetter(), destinationCollection, sp.getGetter(),
                destinationElementClass.getName());
        
        return this;
    }
    
    public CodeSourceBuilder append(String str, Object... args) {
        out.append(String.format(str, args));
        return this;
    }
    
    public CodeSourceBuilder append(String str) {
        out.append(str);
        return this;
    }
    
    public CodeSourceBuilder ifSourceNotNull(Property p) {
        append("if(source.%s() != null)", p.getGetter());
        return this;
    }
    
    public CodeSourceBuilder then() {
        append("{");
        return this;
    }
    
    public CodeSourceBuilder end() {
        append("}\n");
        return this;
    }
    
    @Override
    public String toString() {
        return out.toString();
    }
    
    public CodeSourceBuilder setWrapper(Property dp, Property sp) {
        append("destination.%s(%s.valueOf((%s) source.%s()));\n", dp.getSetter(), dp.getType().getName(), getType(dp.getType()), sp
                .getGetter());
        return this;
    }
    
    private String getType(Class<?> clazz) {
        String type = clazz.getSimpleName().toLowerCase();
        if ("integer".equals(type)) {
            type = "int";
        } else if ("character".equals(type)) {
            type = "char";
        }
        return type;
    }
    
    public CodeSourceBuilder setPrimitive(Property dp, Property sp) {
        append("destination.%s(source.%s().%sValue());}\n", dp.getSetter(), sp.getGetter(), getType(dp.getType()));
        return this;
    }
    
    public void setArray(Property dp, Property sp) {
        String getSizeCode = sp.getType().isArray() ? "length" : "size()";
        String paramType = dp.getType().getComponentType().getName();
        
        ifSourceNotNull(sp).then().append("%s[] %s = new %s[source.%s().%s];", paramType, dp.getName(), paramType, sp.getGetter(),
                getSizeCode).append("mapperFacade.mapAsArray(%s, source.%s(), %s.class);", dp.getName(), sp.getGetter(), paramType).set(dp,
                dp.getName()).end();
        
    }
    
}

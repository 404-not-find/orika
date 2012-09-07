/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.glasnost.orika.metadata;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map;

// XXX must be immutable
public class Property {
    private static final Property[] EMPTY_PATH = new Property[0];
    private String expression;
    private String name;
    private String getter;
    private String setter;
    private Type<?> type;
    private Type<?> elementType;
    private boolean declared;
    
    
    public Property copy() {
        Property copy = new Property();
        copy.declared = this.declared;
        copy.expression = this.expression;
        copy.name = this.name;
        copy.getter = this.getter;
        copy.setter = this.setter;
        copy.type = this.type;
        copy.elementType = this.elementType;
        return copy;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Type<?> getType() {
        return type;
    }
    
    public void setType(Type<?> type) {
        this.type = type;
        this.elementType = null;
    }
    
    public String getGetter() {
        return getter;
    }
    
    public void setGetter(String getter) {
        this.getter = getter;
    }
    
    public String getSetter() {
        return setter;
    }
    
    public String getSetterName() {
        return setter.split("[\\( \\=]")[0];
    }
    
    public String getGetterName() {
        return getter.split("[\\( \\=]")[0];
    }
    
    public void setSetter(String setter) {
        this.setter = setter;
    }
    
    public Type<?> getElementType() {
        if (elementType==null && type.getActualTypeArguments().length>0) {
            elementType = (Type<?>)type.getActualTypeArguments()[0];
        }
        return elementType;
    }
    
    public Class<?> getRawType() {
    	return getType().getRawType();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        final Property property = (Property) o;
        
        if (!expression.equals(property.expression)) {
            return false;
        }
        if (getter != null ? !getter.equals(property.getter) : property.getter != null) {
            return false;
        }
        if (setter != null ? !setter.equals(property.setter) : property.setter != null) {
            return false;
        }
        return !(type != null && !type.equals(property.type));
    }
    
    public boolean isPrimitive() {
        return type.getRawType().isPrimitive();
    }
    
    public boolean isArray() {
        return type.getRawType().isArray();
    }
    
    public boolean isAssignableFrom(Property p) {
        return type.isAssignableFrom(p.type);
    }
    
    public boolean isCollection() {
        return Collection.class.isAssignableFrom(type.getRawType());
    }
    
    public boolean isSet() {
        return Set.class.isAssignableFrom(type.getRawType());
    }
    
    public boolean isList() {
        return List.class.isAssignableFrom(type.getRawType());
    }
    
    public boolean isMap() {
    	return Map.class.isAssignableFrom(type.getRawType());
    }
    
    public boolean hasPath() {
        return false;
    }
    
    public Property[] getPath() {
        return EMPTY_PATH;
    }
    
    public boolean isDeclared() {
        return declared;
    }
    
    public void setDeclared(boolean declared) {
        this.declared = declared;
    }
    
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (getter != null ? getter.hashCode() : 0);
        result = 31 * result + (setter != null ? setter.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return expression + "(" + type + ")";
    }

    public boolean isEnum() {
        return type.getRawType().isEnum();
    }
}

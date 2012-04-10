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

public class NestedProperty extends Property {
    
    private final Property[] path;
    
    public NestedProperty(String expression, Property property, Property[] path) {
        this.setExpression(expression);
        this.setType(property.getType());
        this.setGetter(property.getGetter());
        this.setSetter(property.getSetter());
        this.setName(property.getName());
        this.path = path;
    }
    
    @Override
    public NestedProperty copy() {
    	
    	Property[] copyPath = new Property[path.length];
    	for (int i=0, count = path.length; i < count; ++i) {
    		copyPath[i] = path[i].copy();
    	}
    	NestedProperty copy = new NestedProperty(this.getExpression(), super.copy(), copyPath);
        return copy;
    }
    
    @Override
    public Property[] getPath() {
        return path;
    }
    
    @Override
    public boolean hasPath() {
        return true;
    }
    
    public boolean equals(Object other) {
    	return super.equals(other);
    }
    
    public int hashCode() {
    	return super.hashCode();
    }
    
}

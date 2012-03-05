package ma.glasnost.orika.impl.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ma.glasnost.orika.metadata.Type;

public class UsedTypesContext {
    
    private Map<Type<Object>,Integer> usedTypes = new HashMap<Type<Object>,Integer>();
    private int usedTypeIndex = 0;
    
    @SuppressWarnings("unchecked")
    public int getUsedTypeIndex(Type<?> type) {
        if (type==null) {
            throw new NullPointerException("type must not be null");
        }
        Integer index = usedTypes.get(type);
        if (index == null) {
            index = Integer.valueOf(usedTypeIndex++);
            usedTypes.put((Type<Object>)type, index);
        }
        return index;
    }
    
    public Type<Object>[] getUsedTypesArray() {
        @SuppressWarnings("unchecked")
        Type<Object>[] types = new Type[usedTypes.size()];
        for (Entry<Type<Object>, Integer> entry: usedTypes.entrySet()) {
            types[entry.getValue()] = entry.getKey();
        }
        return types;
    }
}

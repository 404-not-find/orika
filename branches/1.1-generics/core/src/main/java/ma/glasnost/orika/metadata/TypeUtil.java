package ma.glasnost.orika.metadata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;

public class TypeUtil {
    
    static java.lang.reflect.Type[] resolveActualTypeArguments(ParameterizedType type, Type<?> reference) {
          
        return resolveActualTypeArguments(type.getActualTypeArguments(), reference);
    }
    
    static java.lang.reflect.Type[] resolveActualTypeArguments(java.lang.reflect.Type[] typeArguments, Type<?> reference) {
    	
		java.lang.reflect.Type[] actualTypeArguments = new java.lang.reflect.Type[typeArguments.length];
        for (int i=0, len=actualTypeArguments.length; i < len; ++i) {
            java.lang.reflect.Type typeArg = typeArguments[i];
            if (typeArg instanceof TypeVariable) {
                java.lang.reflect.Type resolvedVariable = null;
                if (reference!=null) {
                    resolvedVariable = reference.getTypeByVariable((TypeVariable<?>)typeArg);
                }
                if (resolvedVariable!=null) {
                    actualTypeArguments[i] = resolvedVariable;
                } else {
                    actualTypeArguments[i] = Type.valueOf(Object.class);
                }
            }
        }   
        return actualTypeArguments;
	
    }
}

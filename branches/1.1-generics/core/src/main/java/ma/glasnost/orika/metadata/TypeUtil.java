package ma.glasnost.orika.metadata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;

public class TypeUtil {
    
    static java.lang.reflect.Type[] resolveActualTypeArguments(ParameterizedType type, Type<?> reference) {
        java.lang.reflect.Type[] actualTypeArguments = type.getActualTypeArguments().clone(); 
        for (int i=0, len=actualTypeArguments.length; i < len; ++i) {
            java.lang.reflect.Type typeArg = actualTypeArguments[i];
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

package ma.glasnost.orika.impl;

import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.constructor.SimpleConstructorResolverStrategy;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.converter.DefaultConverterFactory;
import ma.glasnost.orika.impl.generator.CompilerStrategy;
import ma.glasnost.orika.impl.generator.JavassistCompilerStrategy;
import ma.glasnost.orika.property.IntrospectorPropertyResolver;
import ma.glasnost.orika.property.PropertyResolverStrategy;

public abstract class UtilityResolver {
    
    /**
     * Provides a default compiler strategy, favoring a type specified in the
     * appropriate system property if found.
     * 
     * @return
     */
    public static CompilerStrategy getDefaultCompilerStrategy() {
        return resolveStrategy(OrikaSystemProperties.COMPILER_STRATEGY, JavassistCompilerStrategy.class);
//        CompilerStrategy compilerStrategy = null;
//        String strategy = System.getProperty(OrikaSystemProperties.COMPILER_STRATEGY);
//        if (strategy != null) {
//            // User may specify the compiler strategy using a system property
//            try {
//                @SuppressWarnings("unchecked")
//                Class<? extends CompilerStrategy> strategyType = (Class<? extends CompilerStrategy>) Class.forName(strategy, true,
//                        Thread.currentThread().getContextClassLoader());
//                compilerStrategy = strategyType.newInstance();
//                
//            } catch (Exception e) {
//                throw new IllegalArgumentException("compiler strategy specified was invalid", e);
//            }
//            
//        } else {
//            compilerStrategy = new JavassistCompilerStrategy();
//        }
//        return compilerStrategy;
    }
    
    /**
     * Provides a default constructor resolver strategy, favoring a type
     * specified in the appropriate system property if found.
     * 
     * @return
     */
    public static ConverterFactory getDefaultConverterFactory() {
        return resolveStrategy(OrikaSystemProperties.CONVERTER_FACTORY, DefaultConverterFactory.class);
//        ConverterFactory converterFactory = null;
//        String strategy = System.getProperty(OrikaSystemProperties.CONVERTER_FACTORY);
//        if (strategy != null) {
//            
//            try {
//                @SuppressWarnings("unchecked")
//                Class<? extends ConverterFactory> strategyType = (Class<? extends ConverterFactory>) Class.forName(strategy, true,
//                        Thread.currentThread().getContextClassLoader());
//                converterFactory = strategyType.newInstance();
//                
//            } catch (Exception e) {
//                throw new IllegalArgumentException("converter factory specified was invalid", e);
//            }
//            
//        } else {
//            converterFactory = new DefaultConverterFactory();
//        }
//        return converterFactory;
    }
    
    /**
     * Provides a default constructor resolver strategy, favoring a type
     * specified in the appropriate system property if found.
     * 
     * @return
     */
    public static ConstructorResolverStrategy getDefaultConstructorResolverStrategy() {
        return resolveStrategy(OrikaSystemProperties.CONSTRUCTOR_RESOLVER_STRATEGY, SimpleConstructorResolverStrategy.class);
//        ConstructorResolverStrategy constructorResolverStrategy = null;
//        String strategy = System.getProperty(OrikaSystemProperties.CONSTRUCTOR_RESOLVER_STRATEGY);
//        if (strategy != null) {
//            
//            try {
//                @SuppressWarnings("unchecked")
//                Class<? extends ConstructorResolverStrategy> strategyType = (Class<? extends ConstructorResolverStrategy>) Class.forName(
//                        strategy, true, Thread.currentThread().getContextClassLoader());
//                constructorResolverStrategy = strategyType.newInstance();
//                
//            } catch (Exception e) {
//                throw new IllegalArgumentException("constructor resolver strategy specified was invalid", e);
//            }
//            
//        } else {
//            constructorResolverStrategy = new SimpleConstructorResolverStrategy();
//            // constructorResolverStrategy = new
//            // BestFitConstructorResolverStrategy();
//        }
//        return constructorResolverStrategy;
    }
    
    /**
     * Provides a default constructor resolver strategy, favoring a type
     * specified in the appropriate system property if found.
     * 
     * @return
     */
    public static PropertyResolverStrategy getDefaultPropertyResolverStrategy() {
        return resolveStrategy(
                OrikaSystemProperties.PROPERTY_RESOLVER_STRATEGY, 
                IntrospectorPropertyResolver.class);
//        PropertyResolverStrategy constructorResolverStrategy = null;
//        String strategy = System.getProperty(OrikaSystemProperties.CONSTRUCTOR_RESOLVER_STRATEGY);
//        if (strategy != null) {
//            
//            try {
//                @SuppressWarnings("unchecked")
//                Class<? extends PropertyResolverStrategy> strategyType = (Class<? extends PropertyResolverStrategy>) Class.forName(
//                        strategy, true, Thread.currentThread().getContextClassLoader());
//                constructorResolverStrategy = strategyType.newInstance();
//                
//            } catch (Exception e) {
//                throw new IllegalArgumentException("constructor resolver strategy specified was invalid", e);
//            }
//            
//        } else {
//            constructorResolverStrategy = new IntrospectionPropertyResolver();
//        }
//        return constructorResolverStrategy;
    }
    
    
    /**
     * @param systemProperty
     * @param defaultStrategy
     * @return
     */
    private static <S> S resolveStrategy(String systemProperty, Class<? extends S> defaultStrategy) {
        
        S strategy = null;
        String strategyClass = System.getProperty(systemProperty);
        if (strategyClass != null) {
            
            try {
                @SuppressWarnings("unchecked")
                Class<? extends S> strategyType = (Class<? extends S>) Class.forName(
                        strategyClass, true, Thread.currentThread().getContextClassLoader());
                strategy = strategyType.newInstance();
                
            } catch (Exception e) {
                throw new IllegalArgumentException("strategy implementation specified for " + systemProperty +" was invalid", e);
            }
            
        } else {
            try {
                strategy = defaultStrategy.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } 
        }
        return strategy;
    }
    
}

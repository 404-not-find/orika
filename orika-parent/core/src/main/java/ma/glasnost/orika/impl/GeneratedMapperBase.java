package ma.glasnost.orika.impl;


public abstract class GeneratedMapperBase {
    
    protected MapperFacade mapperFacade;
    
    public void setMapperFacade(MapperFacade mapperFacade) {
        this.mapperFacade = mapperFacade;
    }
    
    public abstract Class<?> getAType();
    
    public abstract Class<?> getBType();
    
    public abstract void mapAtoB(Object s, Object b);
    
    public abstract void mapBtoA(Object s, Object d);
    
    public static String toString(int i) {
        return Integer.toString(i);
    }
}

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

package ma.glasnost.orika.impl.mapping.strategy;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.impl.mapping.strategy.UseCustomMapperStrategy.DirectionalCustomMapperReference;
import ma.glasnost.orika.impl.mapping.strategy.UseCustomMapperStrategy.ForwardMapperReference;
import ma.glasnost.orika.impl.mapping.strategy.UseCustomMapperStrategy.ReverseMapperReference;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

/**
 * MappingStrategyRecorder is used to record the important details regarding
 * the branch logic and component lookups performed for a given set of input
 * types.<br>
 * After recording these details, it can be used to generate an appropriate
 * MappingStrategy instance which can be cached and reused for that particular
 * set of inputs.
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class MappingStrategyRecorder {
     
    private boolean copyByReference;
    private boolean mapReverse;
    private boolean unenhance;
    private boolean instantiate;
    
    private Mapper<Object, Object> resolvedMapper;
    private ObjectFactory<Object> resolvedObjectFactory;
    private Converter<Object, Object> resolvedConverter;
    private Type<Object> resolvedSourceType;
    private Type<Object> resolvedDestinationType;
    private MappingStrategy resolvedStrategy;
    
    private final UnenhanceStrategy unenhanceStrategy;
    private final MappingStrategyKey key;
    
    public MappingStrategyRecorder(MappingStrategyKey key, UnenhanceStrategy unenhanceStrategy) {
        this.unenhanceStrategy = unenhanceStrategy;
        this.key = key;
    }
    
    public boolean isUnenhance() {
        return unenhance;
    }

    public boolean isInstantiate() {
		return instantiate;
	}

	public void setInstantiate(boolean instantiate) {
		this.instantiate = instantiate;
	}

	public void setUnenhance(boolean unenhance) {
        this.unenhance = unenhance;
    }
    
    public Converter<Object, Object> getResolvedConverter() {
        return resolvedConverter;
    }

    public void setResolvedConverter(Converter<Object, Object> resolvedConverter) {
        this.resolvedConverter = resolvedConverter;
    }

    
    public Type<?> getResolvedSourceType() {
        return resolvedSourceType;
    }

    @SuppressWarnings("unchecked")
    public void setResolvedSourceType(Type<?> resolvedSourceType) {
        this.resolvedSourceType = (Type<Object>) resolvedSourceType;
    }

    public Type<?> getResolvedDestinationType() {
        return resolvedDestinationType;
    }

    @SuppressWarnings("unchecked")
    public void setResolvedDestinationType(Type<?> resolvedDestinationType) {
        this.resolvedDestinationType = (Type<Object>) resolvedDestinationType;
    }

    public boolean isCopyByReference() {
        return copyByReference;
    }

    public void setCopyByReference(boolean copyByReference) {
        this.copyByReference = copyByReference;
    }

    public boolean isMapReverse() {
        return mapReverse;
    }

    public void setMapReverse(boolean mapReverse) {
        this.mapReverse = mapReverse;
    }

    public Mapper<Object, Object> getResolvedMapper() {
        return resolvedMapper;
    }

    public void setResolvedMapper(Mapper<Object, Object> resolvedMapper) {
        this.resolvedMapper = resolvedMapper;
    }

    public ObjectFactory<Object> getResolvedObjectFactory() {
        return resolvedObjectFactory;
    }

    @SuppressWarnings("unchecked")
    public void setResolvedObjectFactory(ObjectFactory<?> resolvedObjectFactory) {
        this.resolvedObjectFactory = (ObjectFactory<Object>) resolvedObjectFactory;
    }

    /**
     * @return a new instance of the MappingStrategy which can "playback" the 
     * route taken to map a given set of inputs.
     */
    public MappingStrategy playback() {
       
        
        UnenhanceStrategy unenhanceStrategy; 
        if (unenhance) {
            unenhanceStrategy = this.unenhanceStrategy;
        } else {
            unenhanceStrategy = NoOpUnenhancer.getInstance();
        }
        
        if (copyByReference) {
            resolvedStrategy = CopyByReferenceStrategy.getInstance();
        } else if (resolvedConverter != null) {
            resolvedStrategy = new UseConverterStrategy(resolvedSourceType, resolvedDestinationType, resolvedConverter, unenhanceStrategy);
        } else {
        	
        	DirectionalCustomMapperReference directionalMapper = (mapReverse ? new ReverseMapperReference(resolvedMapper) : new ForwardMapperReference(resolvedMapper));
        	if (resolvedObjectFactory != null) {
        		resolvedStrategy = new InstantiateAndUseCustomMapperStrategy(resolvedSourceType, resolvedDestinationType, directionalMapper, resolvedObjectFactory, unenhanceStrategy);
        	} else if (instantiate) {
        		resolvedStrategy = new InstantiateByDefaultAndUseCustomMapperStrategy(resolvedSourceType, resolvedDestinationType, directionalMapper, unenhanceStrategy);
        	} else {
        		resolvedStrategy = new MapExistingAndUseCustomMapperStrategy(resolvedSourceType, resolvedDestinationType, directionalMapper, unenhanceStrategy);
        	}
        
        }
        return resolvedStrategy;
    }
    
    /**
     * Describes the details of the strategy chosen for this particular set of inputs
     * 
     * @return
     */
    public String describeDetails() {
        StringBuilder details = new StringBuilder();
        details
            .append("MappingStrategy resolved and cached:")
            .append("\n\tInputs:[ sourceClass: " + key.getRawSourceType().getCanonicalName())
            .append(", sourceType: " + key.getSourceType())
            .append(", destinationType: " + key.getDestinationType())
            .append("]\n\tResolved:[ strategy: " + resolvedStrategy.getClass().getSimpleName())
            .append(", sourceType: " + getResolvedSourceType())
            .append(", destinationType: " + getResolvedDestinationType());
        if (isCopyByReference()) {
            details.append(", copyByReference?: true");
        }
        
        if (getResolvedConverter() != null) {
            details.append(", converter: " + getResolvedConverter());
        }
        
        if (getResolvedMapper() != null) {
            details.append(", mapper: " + getResolvedMapper());
            details.append(", mapInverse?: " + mapReverse);
        }
        details.append("]");
        
        return details.toString();
    }
    
    static class NoOpUnenhancer implements UnenhanceStrategy {

        private static final NoOpUnenhancer INSTANCE = new NoOpUnenhancer(); 
        
        public static UnenhanceStrategy getInstance() {
            return INSTANCE;
        }

        public <T> Type<T> unenhanceType(T object, Type<T> type) {
            throw new UnsupportedOperationException();
        }

        public <T> T unenhanceObject(T object, Type<T> type) {
            return object;
        }
    }
}

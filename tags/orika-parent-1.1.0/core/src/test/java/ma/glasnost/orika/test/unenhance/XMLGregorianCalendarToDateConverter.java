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
package ma.glasnost.orika.test.unenhance;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import ma.glasnost.orika.converter.Converter;

public class XMLGregorianCalendarToDateConverter implements Converter<XMLGregorianCalendar, Date> {
    
    public Date convert(XMLGregorianCalendar source, Class<? extends Date> destinationClass) {
        Date target = null;
        if (source != null) {
            target = source.toGregorianCalendar().getTime();
        }
        return target;
    }
    
    public boolean canConvert(Class<XMLGregorianCalendar> sourceClass, Class<? extends Date> destinationClass) {
        return XMLGregorianCalendar.class.isAssignableFrom(sourceClass) && Date.class.equals(destinationClass);
    }
    
}

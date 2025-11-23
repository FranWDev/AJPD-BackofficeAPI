package org.dubini.backofficeAPI.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class StringToPGobjectConverter implements Converter<String, Object> {
    
    @Override
    public Object convert(String source) {
        if (source == null) {
            return null;
        }
        
        try {
            // Crear PGobject usando reflexión
            Class<?> pgObjectClass = Class.forName("org.postgresql.util.PGobject");
            Object pgObject = pgObjectClass.getDeclaredConstructor().newInstance();
            
            // Establecer tipo y valor
            pgObjectClass.getMethod("setType", String.class).invoke(pgObject, "jsonb");
            pgObjectClass.getMethod("setValue", String.class).invoke(pgObject, source);
            
            return pgObject;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo convertir String a PGobject", e);
        }
    }
}
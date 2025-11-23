package org.dubini.backofficeAPI.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
@WritingConverter
public class PostgresJsonbWritingConverter implements Converter<String, Object> {
    
    @Override
    public Object convert(String source) {
        try {
            // Crear PGobject para escritura
            Class<?> pgObjectClass = Class.forName("org.postgresql.util.PGobject");
            Object pgObject = pgObjectClass.getDeclaredConstructor().newInstance();
            
            pgObjectClass.getMethod("setType", String.class).invoke(pgObject, "jsonb");
            pgObjectClass.getMethod("setValue", String.class).invoke(pgObject, source);
            
            return pgObject;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo convertir String a PGobject para jsonb", e);
        }
    }
}
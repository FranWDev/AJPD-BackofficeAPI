package org.dubini.backofficeAPI.config;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@Component
@ReadingConverter
public class PGobjectToStringConverter implements Converter<PGobject, String> {
    
    @Override
    public String convert(PGobject source) {
        return source.getValue();
    }
}
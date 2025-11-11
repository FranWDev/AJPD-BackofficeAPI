package org.dubini.backofficeAPI.dto;

import java.util.Map;

import lombok.Data;

@Data
public class EditorJSBlock {

    private String type;
    private Map<String, Object> data;

}

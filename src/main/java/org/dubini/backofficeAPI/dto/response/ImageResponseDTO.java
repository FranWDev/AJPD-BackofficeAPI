package org.dubini.backofficeAPI.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageResponseDTO {
    private String fileName;
    private String url;
    private long size;
}
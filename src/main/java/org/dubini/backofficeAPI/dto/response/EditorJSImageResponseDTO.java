package org.dubini.backofficeAPI.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EditorJSImageResponseDTO {
    private boolean success;
    private FileData file;

    @Data
    @AllArgsConstructor
    public static class FileData {
        private String url;
        private String name;
        private long size;
    }

    public static EditorJSImageResponseDTO success(String url, String name, long size) {
        return new EditorJSImageResponseDTO(true, new FileData(url, name, size));
    }

    public static EditorJSImageResponseDTO error() {
        return new EditorJSImageResponseDTO(false, null);
    }
}
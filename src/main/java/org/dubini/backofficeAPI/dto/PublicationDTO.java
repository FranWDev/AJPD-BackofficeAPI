package org.dubini.backofficeAPI.dto;
import lombok.Data;

@Data
public class PublicationDTO {

    public String title;
    public String description;
    public String imageUrl;
    public String publishedAt;

    public EditorJSContentDTO editorContent;

    public PublicationDTO() {
    }

    public PublicationDTO(String title, String description, String imageUrl, EditorJSContentDTO editorContent) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.editorContent = editorContent;
    }
}

package org.dubini.backofficeAPI.dto;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PublicationDTO {

    public String title;
    public String description;
    public String imageUrl;
    public String publishedAt;
    public EditorJSContentDTO editorContent;
    private OffsetDateTime publishedAtDateTime;

    public PublicationDTO() {
    }

    public PublicationDTO(String title, String description, String imageUrl, EditorJSContentDTO editorContent) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.editorContent = editorContent;
    }

    public OffsetDateTime getPublishedAtDateTime() {
        if (publishedAtDateTime == null && publishedAt != null) {
            try {
                // Try ISO with offset first (e.g. 2025-10-31T09:22:47.631360900Z)
                publishedAtDateTime = OffsetDateTime.parse(publishedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (Exception e1) {
                try {
                    // Fallback: parse as local date-time and assume UTC
                    publishedAtDateTime = OffsetDateTime.of(
                            java.time.LocalDateTime.parse(publishedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            java.time.ZoneOffset.UTC);
                } catch (Exception e2) {
                    // If all parsing fails → leave null (don’t use OffsetDateTime.MIN)
                    publishedAtDateTime = null;
                }
            }
        }
        return publishedAtDateTime;
    }

}

package org.dubini.backofficeAPI.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Type;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "news")
public class News {

    @Id
    @Column(name = "title", nullable = false)
    private String title;

    @Type(JsonBinaryType.class)
    @Column(name = "content", columnDefinition = "jsonb", nullable = false)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
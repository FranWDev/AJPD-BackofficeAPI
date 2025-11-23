package org.dubini.backofficeAPI.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "news")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class News {

    @Id
    @Column("title")
    private String title;

    @Column("content")
    private String content; // JSON como String

    @Column("created_at")
    private LocalDateTime createdAt;
}
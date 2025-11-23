package org.dubini.backofficeAPI.repository;

import org.dubini.backofficeAPI.model.News;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsRepository extends CrudRepository<News, String> {
    
    @Query("SELECT * FROM news ORDER BY created_at DESC")
    List<News> findAllByOrderByCreatedAtDesc();
    
    // Método personalizado que hace UPSERT con CAST a jsonb
    @Modifying
    @Query("""
        INSERT INTO news (title, content, created_at) 
        VALUES (:title, CAST(:content AS jsonb), :createdAt)
        ON CONFLICT (title) 
        DO UPDATE SET 
            content = CAST(:content AS jsonb), 
            created_at = :createdAt
    """)
    void upsertNews(@Param("title") String title, 
                    @Param("content") String content, 
                    @Param("createdAt") LocalDateTime createdAt);
}
package org.dubini.backofficeAPI.repository;

import org.dubini.backofficeAPI.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, String> {

    List<News> findByTitle(String title);

    List<News> findAllByOrderByCreatedAtDesc();

}
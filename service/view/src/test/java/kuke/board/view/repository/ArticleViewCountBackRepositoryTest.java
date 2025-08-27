package kuke.board.view.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import kuke.board.view.entity.ArticleViewCount;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ArticleViewCountBackRepositoryTest {

    @Autowired
    ArticleViewCountBackRepository articleViewCountBackRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    @Transactional
    void updateViewCountTest() {
        // given
        articleViewCountBackRepository.save(
                ArticleViewCount.init(1L, 0L)
        );

        entityManager.flush();
        entityManager.clear();

        int result1 = articleViewCountBackRepository.updateViewCount(1L, 100L);
        int result2 = articleViewCountBackRepository.updateViewCount(1L, 300L);
        int result3 = articleViewCountBackRepository.updateViewCount(1L, 200L);

        assertThat(result1).isEqualTo(1);
        assertThat(result2).isEqualTo(1);
        assertThat(result3).isEqualTo(0);

        ArticleViewCount articleViewCount = articleViewCountBackRepository.findById(1L).get();
        assertThat(articleViewCount.getViewCount()).isEqualTo(300L);
    }
}
package kuke.board.like.repository;

import jakarta.persistence.LockModeType;
import kuke.board.like.entity.ArticleLikeCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleLikeCountRepository extends JpaRepository<ArticleLikeCount, Long> {

    // select..for update 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ArticleLikeCount> findLockedByArticleId(Long articleId);

    @Query(
            value = "update article_like_count set like_count = like_count + 1 where article_id = :articleId",
            nativeQuery = true
    )
    @Modifying // update 쿼리에는 이 어노테이션이 필요하다고 함
    int increase(@Param("articleId") Long articleId);

    @Query(
            value = "update article_like_count set like_count = like_count - 1 where article_id = :articleId",
            nativeQuery = true
    )
    @Modifying // update 쿼리에는 이 어노테이션이 필요하다고 함
    int decrease(@Param("articleId") Long articleId);
}

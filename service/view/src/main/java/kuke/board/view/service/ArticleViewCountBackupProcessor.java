package kuke.board.view.service;

import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.ArticleViewedEventPayload;
import kuke.board.common.outboxmessagerelay.OutboxEventPublisher;
import kuke.board.view.entity.ArticleViewCount;
import kuke.board.view.repository.ArticleViewCountBackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ArticleViewCountBackupProcessor {

    private final ArticleViewCountBackRepository articleViewCountBackRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public void backup(Long articleId, Long viewCount) {
        int result = articleViewCountBackRepository.updateViewCount(articleId, viewCount);

        if (result == 0) {
            articleViewCountBackRepository.findById((articleId))
                    .ifPresentOrElse(
                            ignored -> {}, // 데이터가 있을땐 무시
                            () -> articleViewCountBackRepository.save(ArticleViewCount.init(articleId, viewCount) // 데이터 없을 때 처리
                    ));
        }

        outboxEventPublisher.publish(
                EventType.ARTICLE_VIEWED,
                ArticleViewedEventPayload.builder()
                        .articleId(articleId)
                        .articleViewCount(viewCount)
                        .build(),
                articleId
        );
    }
}

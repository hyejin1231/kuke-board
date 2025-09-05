package kuke.board.articleread.service.event.handler;

import kuke.board.articleread.repository.ArticleIdListRepository;
import kuke.board.articleread.repository.ArticleQueryModel;
import kuke.board.articleread.repository.ArticleQueryModelRepository;
import kuke.board.articleread.repository.BoardArticleCountRepository;
import kuke.board.common.event.Event;
import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.ArticleCreatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ArticleCreateEventHandler implements EventHandler<ArticleCreatedEventPayload> {
    private final ArticleIdListRepository articleIdListRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        ArticleCreatedEventPayload payload = event.getPayload();

        // 여기서는 게시글을 먼저 생성하고, 목록을 생성해서 목록 조회 후 게시글이 보여 게시글 조회 누르지 못하게 생성
        articleQueryModelRepository.create(
                ArticleQueryModel.create(payload),
                Duration.ofDays(1)
        );

        articleIdListRepository.add(payload.getBoardId(), payload.getArticleId(), 1000L);
        boardArticleCountRepository.createOrUpdate(payload.getBoardId(), payload.getBoardArticleCount());
    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType();
    }
}

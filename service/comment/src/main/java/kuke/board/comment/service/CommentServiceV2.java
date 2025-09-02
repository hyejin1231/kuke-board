package kuke.board.comment.service;

import kuke.board.comment.entity.ArticleCommentCount;
import kuke.board.comment.entity.Comment;
import kuke.board.comment.entity.CommentPath;
import kuke.board.comment.entity.CommentV2;
import kuke.board.comment.repository.ArticleCommentCountRepository;
import kuke.board.comment.repository.CommentRepositoryV2;
import kuke.board.comment.service.request.CommentCreateRequestV2;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.CommentCreatedEventPayload;
import kuke.board.common.event.payload.CommentDeletedEventPayload;
import kuke.board.common.outboxmessagerelay.OutboxEventPublisher;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentServiceV2 {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepositoryV2 commentRepositoryV2;
    private final ArticleCommentCountRepository articleCommentCountRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public CommentResponse create(CommentCreateRequestV2 request) {
        CommentV2 parent = findParent(request);
        CommentPath parentCommentPath = parent == null ? CommentPath.create("") : parent.getCommentPath();

        // 하위 댓글 Top Path 를 구해서 +1
        CommentV2 comment = commentRepositoryV2.save(
                CommentV2.create(
                        snowflake.nextId(),
                        request.getContent(),
                        request.getArticleId(),
                        request.getWriterId(),
                        parentCommentPath.createChildCommentPath(
                                commentRepositoryV2.findDescendantsTopPath(request.getArticleId(), parentCommentPath.getPath())
                                        .orElse(null)
                        )
                )
        );

        int result = articleCommentCountRepository.increase(request.getArticleId());
        if (result == 0) {
            articleCommentCountRepository.save(
                    ArticleCommentCount.init(comment.getArticleId(), 1L)
            );
        }

        outboxEventPublisher.publish(
                EventType.COMMENT_CREATED,
                CommentCreatedEventPayload.builder()
                        .commentId(comment.getCommentId())
                        .content(comment.getContent())
                        .articleId(comment.getArticleId())
                        .writerId(comment.getWriterId())
                        .deleted(comment.getDeleted())
                        .createdAt(comment.getCreatedAt())
                        .articleCommentCount(count(comment.getArticleId()))
                        .build(),
                comment.getArticleId()
        );

        return CommentResponse.from(comment);
    }

    private CommentV2 findParent(CommentCreateRequestV2 request) {
        String parentPath = request.getParentPath();
        if (parentPath == null) return null;

        return commentRepositoryV2.findByPath(parentPath)
                .filter(not(CommentV2::getDeleted))
                .orElseThrow();
    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(
                commentRepositoryV2.findById(commentId).orElseThrow()
        );
    }

    @Transactional
    public void delete(Long commentId) {
        commentRepositoryV2.findById(commentId)
                .filter(not(CommentV2::getDeleted))
                .ifPresent(comment -> {
                    if (hasChildren(comment)) {
                        comment.delete(); // 하위 댓글 있으면 삭제 표시만
                    } else {
                        delete(comment);

                        outboxEventPublisher.publish(
                                EventType.COMMENT_DELETED,
                                CommentDeletedEventPayload.builder()
                                        .commentId(comment.getCommentId())
                                        .content(comment.getContent())
                                        .articleId(comment.getArticleId())
                                        .writerId(comment.getWriterId())
                                        .deleted(comment.getDeleted())
                                        .createdAt(comment.getCreatedAt())
                                        .articleCommentCount(count(comment.getArticleId()))
                                        .build(),
                                comment.getArticleId()
                        );
                    }
                });
    }

    private boolean hasChildren(CommentV2 comment) {
        return commentRepositoryV2.findDescendantsTopPath(comment.getCommentId(), comment.getCommentPath().getPath()).isPresent();
    }

    private void delete(CommentV2 comment) {
        commentRepositoryV2.delete(comment); // 실제 삭제
        articleCommentCountRepository.decrease(comment.getArticleId());

        if (!comment.isRoot()) {
            commentRepositoryV2.findByPath(comment.getCommentPath().getParentPath())
                    .filter(CommentV2::getDeleted) // 부모가 이미 소프트 삭제 상태라면
                    .filter(not(this::hasChildren)) // 부모가 더 이상 자식이 없다면
                    .ifPresent(this::delete); // 재귀적으로 부모도 삭제
        }
    }

    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponse.of(
                commentRepositoryV2.findAll(articleId, (page - 1) * pageSize, pageSize)
                        .stream().map(CommentResponse::from)
                        .toList(),
                commentRepositoryV2.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))
        );
    }

    public List<CommentResponse> readAllInfiniteScroll(Long articleId, String lastPath, Long pageSize) {
        List<CommentV2> comments = lastPath == null ?
                commentRepositoryV2.findAllInfiniteScroll(articleId, pageSize) :
                commentRepositoryV2.findAllInfiniteScroll(articleId, lastPath, pageSize);

        return comments.stream().map(CommentResponse::from).collect(Collectors.toList());
    }

    public Long count(Long articleId) {
        return articleCommentCountRepository.findById(articleId)
                .map(ArticleCommentCount::getCommentCount)
                .orElse(0L);
    }

}

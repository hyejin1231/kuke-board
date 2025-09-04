package kuke.board.articleread.service.response;

import kuke.board.articleread.repository.ArticleQueryModel;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class ArticleReadResponse {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long articleCommentCount;
    private Long articleLikeCount;
    private Long articleViewCount; // view service에서 직접 가져올 것

    public static ArticleReadResponse from(ArticleQueryModel article, Long viewCount) {
        ArticleReadResponse response = new ArticleReadResponse();
        response.articleId = article.getArticleId();
        response.title = article.getTitle();
        response.content = article.getContent();
        response.boardId = article.getBoardId();
        response.writerId = article.getWriterId();
        response.createdAt = article.getCreatedAt();
        response.modifiedAt = article.getModifiedAt();
        response.articleCommentCount = article.getArticleCommentCount();
        response.articleLikeCount = article.getArticleLikeCount();
        response.articleViewCount = viewCount;
        return response;
    }
}

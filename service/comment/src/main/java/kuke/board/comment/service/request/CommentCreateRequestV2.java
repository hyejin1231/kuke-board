package kuke.board.comment.service.request;

import lombok.Getter;

@Getter
public class CommentCreateRequestV2 {
    private Long articleId;
    private String content;
    private String parentPath; // 상위댓글
    private Long writerId;
}

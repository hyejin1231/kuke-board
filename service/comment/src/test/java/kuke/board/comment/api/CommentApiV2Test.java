package kuke.board.comment.api;

import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class CommentApiV2Test {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = create(new CommentCreateRequestV2(1L, "my comment 1", null, 1L));
        CommentResponse response2 = create(new CommentCreateRequestV2(1L, "my comment 1", response1.getPath(), 1L));
        CommentResponse response3 = create(new CommentCreateRequestV2(1L, "my comment 3", response2.getPath(), 1L));

        System.out.println("response1.getCommentId() = " + response1.getCommentId());
        System.out.println("\tresponse2.getCommentId() = " + response2.getCommentId());
        System.out.println("\t\tresponse3.getCommentId() = " + response3.getCommentId());

        System.out.println("response1.getPath() = " + response1.getPath());
        System.out.println("\tresponse2.getPath() = " + response2.getPath());
        System.out.println("\t\tresponse3.getPath() = " + response3.getPath());

        /**
         * response1.getCommentId() = 218182745979572224
         * 	response2.getCommentId() = 218182746365448192
         * 		response3.getCommentId() = 218182746470305792
         * response1.getPath() = 00001
         * 	response2.getPath() = 0000100000
         * 		response3.getPath() = 000010000000000
         */
    }

    CommentResponse create(CommentCreateRequestV2 request) {
        return restClient.post()
                .uri("/v2/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/v2/comments/{commentId}", 218182745979572224L)
                .retrieve()
                .body(CommentResponse.class);

        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        restClient.delete()
                .uri("/v2/comments/{commentId}", 218182745979572224L)
                .retrieve();
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/v2/comments?articleId=1&pageSize=10&page=1")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    /**
     * comment.getCommentId() = 218189595058978816
     * comment.getCommentId() = 218189595105116160
     * comment.getCommentId() = 218189595109310464
     * comment.getCommentId() = 218189595109310465
     * comment.getCommentId() = 218189595109310466
     * comment.getCommentId() = 218189595109310467
     * comment.getCommentId() = 218189595109310468
     * comment.getCommentId() = 218189595109310469
     * comment.getCommentId() = 218189595109310470
     * comment.getCommentId() = 218189595109310471
     */

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> response1 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });
        System.out.println("first page");
        for (CommentResponse commentResponse : response1) {
            System.out.println("commentResponse.getCommentId() = " + commentResponse.getCommentId());
        }

        String lastPath = response1.getLast().getPath();

        List<CommentResponse> response2 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5&lastPath=%s".formatted(lastPath))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("second page");
        for (CommentResponse commentResponse : response2) {
            System.out.println("commentResponse.getCommentId() = " + commentResponse.getCommentId());
        }

    }

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequestV2 {
        private Long articleId;
        private String content;
        private String parentPath; // 상위댓글
        private Long writerId;
    }
}

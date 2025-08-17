package kuke.board.article.api;

import kuke.board.article.service.response.ArticleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class ArticleApiTest {
    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createTest() {
        create(new ArticleCreateRequest(
                "hi", "My Content", 1L, 1L
        ));
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
                .uri("v1/articles")
                .body(request)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void readTest() {
        ArticleResponse response = read(215465451597508608L);
        System.out.println("response = " + response);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
                .uri("v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void updateTest() {
        update(215465451597508608L);
        ArticleResponse response = read(215465451597508608L);
        System.out.println("response = " + response);
    }

    void update(Long articleId) {
        restClient.put()
                .uri("v1/articles/{articleId}", articleId)
                .body(new ArticleUpdateRequest("hi 2", "my content 22 "))
                .retrieve();
    }

    @Test
    void deleteTest() {
        restClient.delete()
                .uri("v1/articles/{articleId}", 215465451597508608L)
                .retrieve();
    }

    @Getter
    @AllArgsConstructor
    static public class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    @AllArgsConstructor
    static public class ArticleUpdateRequest {
        private String title;
        private String content;
    }

}

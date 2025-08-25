package kuke.board.comment.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommentPathTest {

    @Test
    void createChildCommentTest() {
        // 00000 <- 생성
        createChildCommentTest(CommentPath.create(""), null, "00000");

        // 00000
        //      00000 <- 생성
        createChildCommentTest(CommentPath.create("00000"), null, "0000000000");

        // 00000
        // 00001
        createChildCommentTest(CommentPath.create(""), "00000", "00001");

        // 0000z
        //       abcdz
        //            zzzzz
        //                  zzzzz
        //        abce0 <- 생성
        createChildCommentTest(CommentPath.create("0000z"), "0000zabcdzzzzzzzzzzz", "0000zabce0");
    }

    void createChildCommentTest(CommentPath commentPath, String descendantsTopPath, String expectedChildPath) {
        CommentPath childCommentPath = commentPath.createChildCommentPath(descendantsTopPath);
        Assertions.assertThat(childCommentPath.getPath()).isEqualTo(expectedChildPath);
    }

    @Test
    void createChildCommentPathIfMaxDepthTest() {
        Assertions.assertThatThrownBy(()-> CommentPath.create("zzzzz".repeat(5)).createChildCommentPath(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createChildCommentPathIfChunkOverflowTest() {
        CommentPath commentPath = CommentPath.create("");

        Assertions.assertThatThrownBy(()-> commentPath.createChildCommentPath("zzzzz"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
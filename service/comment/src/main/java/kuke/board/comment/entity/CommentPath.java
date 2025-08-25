package kuke.board.comment.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Embeddable
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentPath {
    private String path;

    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int DEPTH_CHUNK_SIZE = 5;
    private static final int MAX_DEPTH = 5;

    // MIN_CHUNK = 00000, MAX_CHUNK = zzzzz
    private static final String MIN_CHUNK = String.valueOf(CHARSET.charAt(0)).repeat(DEPTH_CHUNK_SIZE);
    private static final String MAX_CHUNK = String.valueOf(CHARSET.charAt(CHARSET.length() -1)).repeat(DEPTH_CHUNK_SIZE);

    public static CommentPath create(String path) {
        if (isDepthOverflowed(path)) {
            throw new IllegalArgumentException("Depth overflow");
        }
        CommentPath commentPath = new CommentPath();
        commentPath.path = path;
        return commentPath;
    }

    private static boolean isDepthOverflowed(String path) {
        return celDepth(path) > MAX_DEPTH;
    }

    private static int celDepth(String path) {
        // ex) 25/ 5 = 5 depth
        return path.length() / DEPTH_CHUNK_SIZE;
    }

    public int getDepth() {
        return celDepth(path);
    }

    public boolean isRoot() {
        return celDepth(path) == 1;
    }

    public String getParentPath() {
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE);
    }

    public CommentPath createChildCommentPath(String descendantsTopPath) {
        if (descendantsTopPath == null) {
            return CommentPath.create(path + MIN_CHUNK);
        }
        String childrenTopPath = findChildrenTopPath(descendantsTopPath);

        return CommentPath.create(increase(childrenTopPath));
    }


    private String findChildrenTopPath(String descendantsTopPath) {
        return descendantsTopPath.substring(0, (getDepth() + 1) * DEPTH_CHUNK_SIZE);
    }

    private String increase(String path) {
        String lastChunk = path.substring(path.length() - DEPTH_CHUNK_SIZE);
        if (isChunkOverFlowed(lastChunk)) {
            throw new IllegalArgumentException("Chunk overflow");
        }

        int charsetLength = CHARSET.length(); // 62
        int value = 0;

        // 10진수 변환 코드
        // ex) 00000 -> 0, 00001 -> 1, 0000z -> 61, 00010 -> 62
        for (char ch : lastChunk.toCharArray()) {
            value = value * charsetLength + CHARSET.indexOf(ch);
        }

        // value 에 값 + 1 증가
        value = value + 1;

        // 다시 62진수로 변환
        String result = "";
        for (int i = 0; i < DEPTH_CHUNK_SIZE; i++) {
            result =CHARSET.charAt(value % charsetLength) + result;
            value /= charsetLength;
        }

        // 상위 댓글의 경로 정보 + lastChunk
        // 에서 1 더한 값
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE) + result;
    }

    private boolean isChunkOverFlowed(String lastChunk) {
        return MAX_CHUNK.equals(lastChunk);
    }
}

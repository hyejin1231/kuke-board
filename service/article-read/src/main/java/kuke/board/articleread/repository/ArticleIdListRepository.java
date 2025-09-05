package kuke.board.articleread.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ArticleIdListRepository {
    private final StringRedisTemplate redisTemplate;

    // article-read::board::{boardId}::article-list
    private static final String KEY_FORMAT = "article-read::board::%s::article-list";

    /**
     *
     * @param boardId : key
     * @param articleId
     * @param limit : 1000개 저장
     */
    public void add(Long boardId, Long articleId, Long limit) {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey(boardId);
            conn.zAdd(key, 0, toPaddedString(articleId)); // zAdd의 메서드는 double이라 Long 데이터 값이 유실될 수 있음
            conn.zRemRange(key, 0, -limit - 1); // 상위 리미트 개수만 유지
            return null;
        });
    }

    public void delete(Long boardId, Long articleId) {
        redisTemplate.opsForZSet().remove(generateKey(boardId), toPaddedString(articleId));
    }

    public List<Long> readAll(Long boardId, Long offset, Long limit) {
        return redisTemplate.opsForZSet()
                .reverseRange(generateKey(boardId), offset, offset + limit -1)
                .stream().map(Long::valueOf).collect(Collectors.toList());
    }

    public List<Long> readAllInfiniteScroll(Long boardId, Long lastArticleId, Long limit) {
        return redisTemplate.opsForZSet()
                .reverseRangeByLex(
                        generateKey(boardId),
                        lastArticleId == null ? Range.unbounded() : Range.leftUnbounded(Range.Bound.exclusive(toPaddedString(lastArticleId))),
                        Limit.limit().count(limit.intValue())
                        )
                .stream().map(Long::valueOf).collect(Collectors.toList());
    }

    /**
     * Long 값으로 받은 파라미터를 고정된 자릿수의 문자열로 바꾸는 메서드
     * @param articleId
     * @return
     */
    private String toPaddedString(Long articleId) {
        // 1234 -> 0000000000000001234
        return "%019d".formatted(articleId);
    }

    private String generateKey(Long boardId) {
        return KEY_FORMAT.formatted(boardId);
    }
}

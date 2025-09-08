package kuke.board.articleread.cache;

import kuke.board.common.dataserializer.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OptimizedCacheManager {
    private final StringRedisTemplate redisTemplate;
    private final OptimizedCacheLockProvider optimizedCacheLockProvider;

    private static final String DELIMITER = "::";

    /**
     *
     * @param type
     * @param ttlSeconds
     * @param args
     * @param returnType
     * @param originSupplier : 캐시가 만료됐을 때 원본데이터 요청할 수 있도록
     * @return
     */
    public Object process(String type, long ttlSeconds, Object[] args, Class<?> returnType,
                          OptimizedCacheOriginDataSupplier<?> originSupplier) throws Throwable {
        String key = generateKey(type, args);

        String cacheData = redisTemplate.opsForValue().get(key);

        if (cacheData == null) {
            return refresh(originSupplier, key, ttlSeconds);
        }

        OptimizedCache optimizedCache = DataSerializer.deserialize(cacheData, OptimizedCache.class);
        if (optimizedCache == null) {
            return refresh(originSupplier, key, ttlSeconds);
        }

        if (!optimizedCache.isExpired()) {
            return optimizedCache.parseData(returnType);
        }

        if (!optimizedCacheLockProvider.lock(key)) {
            return optimizedCache.parseData(returnType);
        }

        try {
            return refresh(originSupplier, key, ttlSeconds);
        }finally {
            optimizedCacheLockProvider.unlock(key);
        }
    }

    private Object refresh(OptimizedCacheOriginDataSupplier<?> originSupplier, String key, long ttlSeconds) throws Throwable  {
        Object result = originSupplier.get();
        OptimizedCacheTTL optimizedCacheTTL = OptimizedCacheTTL.of(ttlSeconds);
        OptimizedCache optimizeCache = OptimizedCache.of(result, optimizedCacheTTL.getLogicalTTL());

        redisTemplate.opsForValue()
                .set(
                        key,
                        DataSerializer.serialize(optimizeCache),
                        optimizedCacheTTL.getPhysicalTTL()
                );
        return result;
    }

    private String generateKey(String prefix, Object[] args) {
        return prefix + DELIMITER + Arrays.stream(args).map(String::valueOf).collect(Collectors.joining(DELIMITER));
    }
}

package kuke.board.articleread.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kuke.board.common.dataserializer.DataSerializer;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@ToString
public class OptimizedCache {
    private String data;
    private LocalDateTime expiredAt;

    /**
     * @param data
     * @param ttl : logical ttl
     * @return
     */
    public static OptimizedCache of(Object data, Duration ttl) {
        OptimizedCache optimizeCache = new OptimizedCache();
        optimizeCache.data = DataSerializer.serialize(data);
        optimizeCache.expiredAt = LocalDateTime.now().plus(ttl);

        return optimizeCache;
    }

    /**
     * logical ttl 만료됐는지
     * @return
     */
    @JsonIgnore
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    public <T> T parseData(Class<T> dataType) {
        return DataSerializer.deserialize(data, dataType);
    }
}

package kuke.board.articleread.cache;

import lombok.Getter;

import java.time.Duration;

@Getter
/* logicalTTL 과 physicalTTL 받아서 계산해주는 클래스 */
public class OptimizedCacheTTL {
    private Duration logicalTTL;
    private Duration physicalTTL;

    public static final long PHYSICAL_TTL_DELAY_SECONDS =5;

    public static OptimizedCacheTTL of(long ttlSeconds) {
        OptimizedCacheTTL optimizedCacheTTL = new OptimizedCacheTTL();
        optimizedCacheTTL.logicalTTL = Duration.ofSeconds(ttlSeconds);
        optimizedCacheTTL.physicalTTL = optimizedCacheTTL.logicalTTL.plusSeconds(PHYSICAL_TTL_DELAY_SECONDS);

        return optimizedCacheTTL;
    }

}

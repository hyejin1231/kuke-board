package kuke.board.common.outboxmessagerelay;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Getter
public class AssignedShard {
    private List<Long> shards; // 애플리케이션에 할당된 Shard 번호

    public static AssignedShard of(String appId, List<String> appIds, long shardCount) {
        AssignedShard assignedShard = new AssignedShard();
        assignedShard.shards = assign(appId, appIds, shardCount);

        return assignedShard;
    }

    private static List<Long> assign(String appId, List<String> appIds, long shardCount) {
        int appIndex = findAppIndex(appId, appIds);

        if (appIndex == -1) {
            return List.of();
        }

        long start = appIndex * shardCount / appIds.size();
        long end = (appIndex + 1) * shardCount / appIds.size() - 1;

        return LongStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
    }

    /**
     *
     * @param appId
     * @param appIds : 지금 실행된 애플리케이션 목록을 정렬된 상태로 가지고 있음
     * @return
     */
    private static int findAppIndex(String appId, List<String> appIds) {
        for (int i= 0; i < appIds.size(); i++) {
            if (appIds.get(i).equals(appId)) {
                return i;
            }
        }

        return -1;
    }
}

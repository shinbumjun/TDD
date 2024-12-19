package io.hhplus.tdd.util;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 사용자별 작업을 관리하는 클래스
 * - 사용자 ID(Long)를 키로 사용
 * - 각 사용자에 대해 작업 큐(BlockingQueue<Runnable>)를 생성
 * - 여러 쓰레드 환경에서 안전하게 작업 관리 가능
 */
// 예) 사용자 A는 작업 1, 2를 요청, 사용자 B는 작업 3, 4를 요청하면 작업은 각각 A와 B의 작업 큐에 따로 저장
public class UserRequestQueue {
    private final ConcurrentHashMap<Long, BlockingQueue<Runnable>> userQueueMap = new ConcurrentHashMap<>();

    /**
     * 작업 추가
     * - 주어진 사용자 ID에 작업(task)을 추가
     * - 사용자 ID에 해당하는 큐가 없으면 새로 생성 후 추가
     * @param userId 사용자 ID
     * @param task 추가할 작업(Runnable)
     */
    // 예) queue.addToQueue(101L, () -> System.out.println("사용자 101의 첫 번째 작업 실행"));
    public void addToQueue(Long userId, Runnable task) {
        userQueueMap.computeIfAbsent(userId, id -> new LinkedBlockingQueue<>()).add(task);
    }

    /**
     * 다음 작업 가져오기
     * - 주어진 사용자 ID의 작업 큐에서 맨 앞의 작업을 가져옴
     * - 작업이 없으면 대기 상태
     * @param userId 사용자 ID
     * @return 작업(Runnable) 또는 null (사용자 큐가 없을 경우)
     * @throws InterruptedException 작업 대기 중 인터럽트 발생 시
     */
    // Runnable task = queue.getNextTask(101L); // 사용자 101의 첫 번째 작업 가져오기
    // if (task != null) {
    //     task.run(); // 작업 실행 -> "사용자 101의 첫 번째 작업 실행"
    // }
    public Runnable getNextTask(Long userId) throws InterruptedException {
        BlockingQueue<Runnable> queue = userQueueMap.get(userId);
        return (queue != null) ? queue.take() : null;
    }

    /**
     * 작업 대기 여부 확인
     * - 주어진 사용자 ID에 대기 중인 작업이 있는지 확인
     * @param userId 사용자 ID
     * @return 대기 작업이 있으면 true, 없으면 false
     */
    // 예) boolean hasTasks = queue.hasPendingTasks(101L); // 사용자 101의 대기 작업 확인
    public boolean hasPendingTasks(Long userId) {
        BlockingQueue<Runnable> queue = userQueueMap.get(userId);
        return queue != null && !queue.isEmpty();
    }

    /**
     * 모든 사용자 ID 가져오기
     * - 현재 작업 큐에 등록된 사용자 ID를 반환
     * @return 사용자 ID의 Set
     */
    public Set<Long> getUserIds() {
        return userQueueMap.keySet();
    }

    /**
     * 모든 작업 수 가져오기
     * - 모든 사용자 큐의 작업 수를 합산하여 반환
     * @return 전체 작업 수
     */
    public int getTotalTaskCount() {
        return userQueueMap.values().stream()
                .mapToInt(BlockingQueue::size) // 각 큐의 작업 수를 합산
                .sum();
    }
}

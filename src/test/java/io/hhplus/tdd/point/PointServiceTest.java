package io.hhplus.tdd.point;

import io.hhplus.tdd.ErrorResponse;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.util.UserRequestQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

/*
    Mock란? 테스트를 위해 만들어진 가짜 객체

    왜 사용할까?
    1. 외부 의존성을 제거하기 위해 -> 테스트 대상 코드만 검증, 테스트의 독립성을 보장
    2. 예측 가능한 테스트를 위해 -> 미리 정의된 결과값을 반환하도록 설정
    3. 데이터베이스/네트워크 의존성을 제거 -> 가상의 환경에서 테스트

    ***중요
    Mock 객체는 항상 우리가 설정한 기본 데이터를 반환합니다.
    이 데이터를 기반으로 Service 계층의 로직이 제대로 동작하는지 확인합니다.
 */
class PointServiceTest {

    // UserPointTable과 PointHistoryTable을 Mock 객체로 설정
    @Mock
    private UserPointTable userPointTable;
    @Mock
    private PointHistoryTable pointHistoryTable;

    // PointService를 테스트 대상으로 설정
    @InjectMocks
    private PointService pointService;

    // Mock 객체 초기화
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        System.out.println("Mock 객체 초기화 완료");
    }

    // 1. 포인트 조회 기능 테스트
    @Test
    void 포인트_0으로_조회_실패케이스() {

        // 서비스에 0에 대한 로직이 없어서 가짜 객체의 설정값(point = 100L)이 반환되기 때문에 assertThat(result).isNull()에서 테스트가 실패합니다

        // given : 어떠한 데이터가 주어질 때.
        long userId = 0;

//        when(userPointTable.selectById(userId))
//                .thenReturn(new UserPoint(0L, 100L, System.currentTimeMillis()));
//        // when : 어떠한 기능을 실행하면
//        UserPoint result = pointService.getUserPoint(userId);
//        // then : 어떠한 결과를 기대한다.
//        assertThat(result).isNull();

        // when & then: 예외 검증
        assertThatThrownBy(() -> pointService.getUserPoint(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 사용자 ID입니다.");
    }

    @Test // ***** 포인트 조회 유저없음 실패 케이스를 올바르게 만드는 과정을 이해
    void 포인트_조회_유저없음_실패케이스() {

        // 유저 없는 경우를 처리하는 로직이 PointService에 없기 때문에, 가짜 객체(Mock)가 설정한 값이 반환되어 테스트가 실패
        // ***** 특정 ID 값이 중요한 것이 아니라, userPointTable.selectById()가 null을 반환하는 상황

        // given : 존재하지 않는 userId 설정
        long userId = 999L;
        // 초기 테스트와 문제점 (틀린 Red 테스트) -> 서비스 로직에서 특정 값에 의존하는 경우
        // when(userPointTable.selectById(userId))
                // .thenReturn(new UserPoint(999L, 10000L, System.currentTimeMillis()));
        when(userPointTable.selectById(userId))
                .thenReturn(null);

//        // when : 포인트 조회 실행
//        UserPoint result = pointService.getUserPoint(userId);
//        // then : 결과가 null이어야 함
//        assertThat(result).isNull();
        // when & then: 예외 검증
        assertThatThrownBy(() -> pointService.getUserPoint(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 사용자의 포인트 정보가 없습니다.");
    }

    @Test
    void 포인트_조회_ID가_음수인_실패케이스() {
        // given : 존재하지 않는 id 설정
        long userId = -1L;
        // when(userPointTable.selectById(userId))
        //         .thenReturn(new UserPoint(-1L, 10000L, System.currentTimeMillis()));
        when(userPointTable.selectById(userId))
                .thenReturn(null); // null 반환 설정

//        // when
//        UserPoint result = pointService.getUserPoint(userId);
//        // then
//        assertThat(result).isNull();
        assertThatThrownBy(() -> pointService.getUserPoint(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 사용자 ID입니다.");
    }

    // 2. 포인트 충전/사용 내역 조회 기능
    @Test
    void 포인트_내역_조회_유저없음_실패케이스() {
        // given: 존재하지 않는 userId 설정
        long userId = 999L; // 존재하지 않는 id
        // when(pointHistoryTable.selectAllByUserId(userId))
        //         .thenReturn(List.of(
        //                 new PointHistory(1L, userId, 10000, TransactionType.CHARGE, System.currentTimeMillis())
        //         )); // 유저가 없는 경우인데 잘못된 데이터 반환
        when(pointHistoryTable.selectAllByUserId(userId))
                .thenReturn(null); // 유저가 없는 경우 null 반환 설정

        // when(pointHistoryTable.selectAllByUserId(userId))
        //        .thenReturn(Collections.emptyList()); // 유저가 없으므로 빈 리스트 반환


//        // when: 포인트 내역 조회 실행
//        List<PointHistory> result = pointService.getUserHistories(userId);
//        // then: 결과가 빈 리스트여야 함
//        assertThat(result).isEmpty(); // 빈 리스트를 기대
        // when & then: 예외 검증
        assertThatThrownBy(() -> pointService.getUserHistories(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 사용자의 포인트 이력 정보가 없습니다.");
    }

    @ParameterizedTest // 하나의 테스트 케이스에 두가지 경우를 적용하려면? -> for문, @ParameterizedTest 사용
    @ValueSource(longs = {0L, -1L}) // 음수와 0 테스트
    void 유효하지_않은_ID_요청_충전과_사용_이력포함(long longs) {
        // given: Mock 데이터를 설정하여 CHARGE와 USE 트랜잭션 모두 반환
        // when(pointHistoryTable.selectAllByUserId(longs))
        //         .thenReturn(List.of(
        //                 new PointHistory(1L, longs, 10000, TransactionType.CHARGE, System.currentTimeMillis()), // 충전
        //                 new PointHistory(2L, longs, -5000, TransactionType.USE, System.currentTimeMillis()) // 사용
        //         ));

//        when(pointHistoryTable.selectAllByUserId(longs))
//                .thenReturn(Collections.emptyList()); // 음수나 0 입력 시 빈 리스트 반환
//        // when: 포인트 내역 조회 실행
//        List<PointHistory> result = pointService.getUserHistories(longs);
//
//        // then: CHARGE와 USE 트랜잭션이 포함된 결과를 검증
//        // assertThat(result).hasSize(2); // 두 개의 트랜잭션이 반환되었는지 확인
//        assertThat(result).isEmpty();

        // when & then: 예외 검증
        assertThatThrownBy(() -> pointService.getUserHistories(longs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 사용자 ID입니다.");
    }

    // 3. 포인트 충전 기능
    @Test
    void 포인트_충전_최대_초과_예외() {
        // given: 사용자 ID와 초과 충전 금액 설정
        long userId = 1L;
        long amount = 1_000_001L; // 최대 잔고 초과 금액

        // 사용자의 현재 포인트를 9,000,000으로 Mock 설정
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, 9_000_000L, System.currentTimeMillis()));

        // when & then: 포인트 충전 시 예외 발생 검증
        assertThatThrownBy(() -> pointService.chargePoint(userId, amount))
                .isInstanceOf(IllegalArgumentException.class) // 예외 타입
                .hasMessageContaining("보유 포인트는 1000만원 이상일 수 없습니다."); // 예외 메시지 검증
    }

    @Test
    void 충전_금액이_0_이하인_경우_예외_발생() {
        // given
        long userId = 1L;
        long amount = 0L; // 0 이하로 충전

        // when & then 예외 발생 검증
        assertThatThrownBy(() -> pointService.chargePoint(userId, amount))
                .isInstanceOf(IllegalArgumentException.class) // 예외 타입
                .hasMessageContaining("충전 요청 포인트는 0원 이하일 수 없습니다."); // 예외 메시지 검증
    }

    // 4. 포인트 사용 기능
    @Test
    void 사용_금액이_0_이하인_경우_예외_발생() {
        // given
        long userId = 1L;
        long amount = -1; // 사용 금액 0이하

        // when & then : 포인트 사용 요청 시 예외 발생 검증증
        assertThatThrownBy(() -> pointService.usePoint(userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용 요청 포인트는 0원 이하일 수 없습니다.");
    }
    @Test
    void 사용_금액이_잔고보다_많은_경우_예외_발생() {
        // given
        long userId = 1L;
        long amount = 20_000L; // 잔고보다 많은 금액

        // 사용자의 현재 포인트를 10_000L 으로 설정
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, 10_000L, System.currentTimeMillis()));

        // when & then: 포인트 충전 시 예외 발생 검증
        assertThatThrownBy(() -> pointService.usePoint(userId, amount))
                .isInstanceOf(IllegalArgumentException.class) // 예외 타입
                .hasMessageContaining("보유 포인트는 0원 이하일 수 없습니다."); // 예외 메시지 검증
    }

    // 5. 동시성 큐방식
    @Test
    void 동시성_요청_순서대로_처리_확인_로그만으로_확인() throws InterruptedException {

        // 1. 사용자별 요청 큐 생성 -> FIFO(선입선출)
        UserRequestQueue queue = new UserRequestQueue();

        // 2. 뒤죽박죽 랜덤으로 작업 생성
        Random random = new Random();
        int totalRandomTasks = random.nextInt(20) + 10; // 랜덤 작업 수 (10~30개)
        List<String> generatedRequestsLog = new ArrayList<>(); // 요청 생성 로그
        Map<Long, List<String>> userTaskLogs = new ConcurrentHashMap<>(); // 사용자별 처리 로그

        for (int i = 0; i < totalRandomTasks; i++) {
            // 랜덤 사용자 ID (1~3)
            long userId = random.nextInt(3) + 1;

            // 랜덤 작업 내용 생성
            String action = switch (random.nextInt(3)) {
                case 0 -> "조회";
                case 1 -> "사용";
                default -> "충전";
            };

            // 요청 로그 저장
            String logEntry = "생성된 요청 -> 사용자: " + userId + ", 작업: " + action;
            generatedRequestsLog.add(logEntry); // 요청 로그 저장

            // 작업 추가
            queue.addToQueue(userId, () -> {
                userTaskLogs.computeIfAbsent(userId, k -> new ArrayList<>())
                        .add("처리된 요청 -> 사용자: " + userId + ", 작업: " + action);
            });
        }

        // 3. 사용자별 독립 작업 쓰레드 생성
        Set<Long> userIds = queue.getUserIds();
        List<Thread> threads = new ArrayList<>();

        for (Long userId : userIds) {
            Thread thread = new Thread(() -> {
                try {
                    while (queue.hasPendingTasks(userId)) {
                        Runnable task = queue.getNextTask(userId);
                        if (task != null) {
                            task.run();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(thread);
            thread.start();
        }

        // 4. 모든 쓰레드 종료 대기
        for (Thread thread : threads) {
            thread.join();
        }

        // 5. 생성된 요청과 처리된 요청 비교
        System.out.println("\n=== 요청 생성 순서 ===");
        generatedRequestsLog.forEach(System.out::println);

        System.out.println("\n=== 처리된 요청 순서 ===");
        userTaskLogs.forEach((userId, tasks) -> tasks.forEach(System.out::println));
    }

    @Test
    void 동시성_요청_순서대로_처리_확인() throws InterruptedException {
        // 1. 사용자별 요청 큐 생성, FIFO(선입선출) -> 동시에 여러 요청이 들어오더라도 순서대로
        UserRequestQueue queue = new UserRequestQueue();

        // 2. Mock 데이터 준비
        when(userPointTable.selectById(anyLong())).thenAnswer(invocation -> {
            long userId = invocation.getArgument(0);
            return new UserPoint(userId, 100_000L * userId, System.currentTimeMillis());
        });

        // 3. 요청 큐에 작업 추가
        queue.addToQueue(1L, () -> pointService.chargePoint(1L, 20_000L));
        queue.addToQueue(2L, () -> pointService.usePoint(2L, 10_000L));
        queue.addToQueue(3L, () -> pointService.getUserPoint(3L));
        queue.addToQueue(1L, () -> pointService.usePoint(1L, 5_000L));
        queue.addToQueue(2L, () -> pointService.chargePoint(2L, 15_000L));

        // 4. 사용자별 독립 작업 쓰레드 생성 (동시성 제어에 대한 통합 테스트 작성)
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        Set<Long> userIds = queue.getUserIds();
        CountDownLatch latch = new CountDownLatch(queue.getTotalTaskCount());

        for (Long userId : userIds) {
            executorService.submit(() -> {
                try {
                    while (queue.hasPendingTasks(userId)) {
                        Runnable task = queue.getNextTask(userId);
                        if (task != null) {
                            task.run();
                            latch.countDown();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // 5. 모든 작업 완료 대기
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue(completed, "모든 요청이 처리되지 않았습니다!");

        // 6. 쓰레드 풀 종료
        executorService.shutdown();

        // 7. 검증 (내부 의존성 호출 검증)
        verify(userPointTable, atLeastOnce()).selectById(1L);
        verify(userPointTable, atLeastOnce()).selectById(2L);
        verify(userPointTable, atLeastOnce()).selectById(3L);
    }










}
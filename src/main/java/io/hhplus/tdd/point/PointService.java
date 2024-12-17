package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.database.PointHistoryTable;
import org.springframework.stereotype.Service;

import java.util.List;

    /*
    - 필드 주입 방식의 단점:
    1. 테스트 코드에서 Mock 객체 주입이 어렵다. (Reflection 사용 필요)
    2. 의존성 불변성을 보장하지 않는다. (final 선언 불가)
    3. Spring 컨테이너 없이는 객체를 독립적으로 생성할 수 없다. (POJO가 아님)
    4. 순환 의존성 문제를 컴파일 타임에 확인할 수 없다. (런타임 예외 발생)

    @Autowired
    private PointHistoryTable pointHistoryTable; // 포인트 내역 관리 객체

    @Autowired
    private UserPointTable userPointTable; // 사용자 포인트 관리 객체
     */
/**
 * 포인트 비즈니스 로직을 담당하는 서비스 클래스
 */
@Service
public class PointService {

    private final UserPointTable userPointTable;         // 사용자 포인트 데이터 접근
    private final PointHistoryTable pointHistoryTable;   // 포인트 이력 데이터 접근

    // 생성자
    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    /**
     * 포인트 조회 기능
     */
    public UserPoint getUserPoint(long userId) {
        UserPoint userPoint = userPointTable.selectById(userId);
        System.out.println("S)포인트 조회 = " + userPoint);

        return userPoint;
    }

    /**
     * 포인트 충전/사용 내역 조회 기능
     */
    public List<PointHistory> getUserHistories(long userId) {
        List<PointHistory> pointHistory = pointHistoryTable.selectAllByUserId(userId);
        System.out.println("S)포인트 충전/사용 내역 조회 = " + pointHistory);
        return pointHistory;
    }

    /**
     * 포인트 충전 기능
     */
    public UserPoint chargePoint(long userId, long amount) {
        // 포인트 업데이트 및 이력 저장
        UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, amount);
        System.out.println("포인트 충전 = " + updatedPoint);

        PointHistory pointHistory2 = pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
        System.out.println("이력 저장 = " + pointHistory2);
        return updatedPoint;
    }

    /**
     * 포인트 사용 기능
     */
    public UserPoint usePoint(long userId, long amount) {
        // 포인트 사용 로직과 이력 저장
        UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, -amount);
        System.out.println("포인트 사용 = " + updatedPoint);

        PointHistory pointHistory3 = pointHistoryTable.insert(userId, -amount, TransactionType.USE, System.currentTimeMillis());
        System.out.println("이력 저장 = " + pointHistory3);
        return updatedPoint;
    }

    // -> UserPointTable & PointHistoryTable: 데이터 저장 및 조회를 담당

}

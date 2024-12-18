package io.hhplus.tdd.point;

import io.hhplus.tdd.ErrorResponse;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.database.PointHistoryTable;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

    private static final long MAX_POINT_BALANCE = 10_000_000; // 최대 포인트 제한

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
        // if (userId <= 0) { // 포인트_0으로_조회_실패케이스 + 포인트_조회_ID가_음수인_실패케이스 = 이게 리펙토링?
        //     return null;
        //     throw new CustomException("유효하지 않은 사용자 ID입니다.");
        // }
        if (userId <= 0) { // 유효하지 않은 ID
            throw new IllegalArgumentException(
                    new ErrorResponse("INVALID_ID", "유효하지 않은 사용자 ID입니다.").toString());
        }


        UserPoint userPoint = userPointTable.selectById(userId);

        // if(userPoint == null || userId == 999L || userId == -1L) { // 특정된 값이 들어간 잘못된 서비스 로직 -> 반환 값에 집중해야함
        // if(userPoint == null){
        //     return null;
        //     throw new CustomException("해당 사용자의 포인트 정보가 없습니다.");
        // }
        if (userPoint == null) { // 포인트 정보 없음
            throw new IllegalArgumentException(
                    new ErrorResponse("USER_POINT_NOT_FOUND", "해당 사용자의 포인트 정보가 없습니다.").toString());
        }

        return userPoint;
    }

    /**
     * 포인트 충전/사용 내역 조회 기능
     */
    public List<PointHistory> getUserHistories(long userId) {

        // 특정 ID에 의존한 임시 로직
        // if (userId == 999 || userId <= 0) {
        //     return Collections.emptyList(); // 빈 리스트 반환
        // }
        if (userId <= 0) { // 유효하지 않은 ID
            throw new IllegalArgumentException(
                    new ErrorResponse("INVALID_ID", "유효하지 않은 사용자 ID입니다.").toString());
        }

        List<PointHistory> pointHistory = pointHistoryTable.selectAllByUserId(userId);

        // null인 경우 처리
        // if (pointHistory == null) {
        //     return Collections.emptyList(); // 빈 리스트 반환
        // }
        if (pointHistory == null || pointHistory.isEmpty()) { // 사용 내역 없음
            throw new IllegalArgumentException(
                    new ErrorResponse("HISTORY_NOT_FOUND", "해당 사용자의 포인트 이력 정보가 없습니다.").toString());
        }

        return pointHistory;
    }

    /**
     * 포인트 충전 기능
     */
    public UserPoint chargePoint(long userId, long amount) {
        if (amount <= 0) { // 충전 금액이 음수나 0이 될 수 없다
            throw new IllegalArgumentException("충전 요청 포인트는 0원 이하일 수 없습니다.");
        }

        UserPoint userPoint = userPointTable.selectById(userId); // 특정 사용자 포인트 조회 (*****중복 코드)
        long updatedBalance;

        if (userPoint == null) { // 새 사용자
            updatedBalance = amount; // 입력된 금액이 잔고로 설정
            userPoint = userPointTable.insertOrUpdate(userId, updatedBalance); // 새로운 포인트 정보 저장
        } else { // 해당 사용자의 포인트가 존재
            updatedBalance = userPoint.point() + amount; // 현재 포인트(가짜 객체에 9_000_000L) + 충전 포인트
            if (updatedBalance > 10_000_000) { // 최대 잔고 초과
                throw new IllegalArgumentException("보유 포인트는 1000만원 이상일 수 없습니다.");
            }
            userPoint = userPointTable.insertOrUpdate(userId, updatedBalance);
        }

        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
        return userPoint;
    }

    /**
     * 포인트 사용 기능
     */
    public UserPoint usePoint(long userId, long amount) {

        if(amount <= 0) { // 사용 금액이 0이하
            throw new IllegalArgumentException("사용 요청 포인트는 0원 이하일 수 없습니다.");
        }

        UserPoint userPoint = userPointTable.selectById(userId); // 특정 사용자 포인트 조회 (*****중복 코드)

        // 현재 잔액이 이게 없거나 사용금액 보다 적으면 에러
        if(userPoint == null || userPoint.point() < amount){
            throw new IllegalArgumentException("보유 포인트는 0원 이하일 수 없습니다.");
        }

        // 잔액에서 사용 금액 차감
        long updatedBalance = userPoint.point() - amount;

        // 포인트 사용 로직과 이력 저장
        UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, -amount);

        // 새로운 잔액으로 업데이트
        userPoint = userPointTable.insertOrUpdate(userId, updatedBalance);

        // 사용 내역 기록
        pointHistoryTable.insert(userId, -amount, TransactionType.USE, System.currentTimeMillis());
        return userPoint;
    }

    // -> UserPointTable & PointHistoryTable: 데이터 저장 및 조회를 담당

}

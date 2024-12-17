package io.hhplus.tdd.point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final PointService pointService;
    public PointController(PointService pointService) {
        this.pointService = pointService;
    }
    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public UserPoint point(@PathVariable long id) {
        UserPoint userpoint = pointService.getUserPoint(id);
        System.out.println("C)포인트 조회 = " + userpoint);

        return userpoint;
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(@PathVariable long id) {
        List<PointHistory> histories = pointService.getUserHistories(id);
        System.out.println("C)포인트 충전/사용 내역 조회 = " + histories);
        return histories;
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(@PathVariable long id, @RequestBody long amount) {
        UserPoint updatedPoint = pointService.chargePoint(id, amount);
        System.out.println("C)포인트 충전 = " + updatedPoint);
        return updatedPoint;
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public UserPoint use(@PathVariable long id, @RequestBody long amount) {
        UserPoint updatedPoint = pointService.usePoint(id, amount);
        System.out.println("C)포인트 사용 = " + updatedPoint);
        return updatedPoint;
    }
}

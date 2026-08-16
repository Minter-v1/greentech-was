package com.greentech.payroll.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 급여 산정 규칙
 *
 * FIXME: 아래 값은 계산 로직 검증용 기본값
 *        실제 운영 적용 전 취업규칙·근로계약 기준으로 재정의 필요
 */
public final class PayrollPolicy {

    /** 월 소정근로시간 - 통상시급 산출 기준 */
    public static final BigDecimal MONTHLY_WORK_HOURS = new BigDecimal("209");

    /** 비과세 식대 월 정액 */
    public static final BigDecimal MEAL_ALLOWANCE = new BigDecimal("200000");

    /** 연장근로 가산 배율 */
    public static final BigDecimal RATE_EXTENDED = new BigDecimal("1.5");

    /** 야간근로 배율 - 연장 1.5 에 야간 가산 0.5 합산 */
    public static final BigDecimal RATE_NIGHT = new BigDecimal("2.0");

    /** 휴일근로 가산 배율 */
    public static final BigDecimal RATE_HOLIDAY = new BigDecimal("1.5");

    private PayrollPolicy() {
    }

    /** 통상시급 */
    public static BigDecimal hourlyRate(BigDecimal basePay) {
        if (basePay == null || basePay.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return basePay.divide(MONTHLY_WORK_HOURS, 2, RoundingMode.HALF_UP);
    }

    /**
     * 직책수당
     *
     * NOTE: 직위 서열 기준 정액 지급
     */
    public static BigDecimal positionAllowance(Integer positionLevel) {
        if (positionLevel == null) {
            return BigDecimal.ZERO;
        }
        if (positionLevel >= 6) {
            return new BigDecimal("300000");
        }
        if (positionLevel >= 4) {
            return new BigDecimal("150000");
        }
        return BigDecimal.ZERO;
    }

    /** 원 단위 반올림 */
    public static BigDecimal toWon(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP);
    }

    /** 분 단위를 시간으로 환산 */
    public static BigDecimal minutesToHours(long minutes) {
        return BigDecimal.valueOf(minutes).divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);
    }
}

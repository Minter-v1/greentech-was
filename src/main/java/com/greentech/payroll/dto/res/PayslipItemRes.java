package com.greentech.payroll.dto.res;

import com.greentech.payroll.domain.PayItem;
import com.greentech.payroll.domain.PayslipDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "급여 명세서 항목")
public record PayslipItemRes(
        @Schema(description = "항목코드", example = "BASE")
        String itemCode,

        @Schema(description = "항목명", example = "기본급")
        String itemName,

        @Schema(description = "항목 구분", example = "EARNING")
        PayItem.ItemType itemType,

        @Schema(description = "금액", example = "4000000")
        BigDecimal amount,

        @Schema(description = "비고")
        String note) {

    public static PayslipItemRes from(PayslipDetail entity) {
        return new PayslipItemRes(
                entity.getItemCode(),
                entity.getItemName(),
                entity.getItemType(),
                entity.getAmount(),
                entity.getNote());
    }
}

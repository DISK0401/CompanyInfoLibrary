package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.CompanyProcurement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.time.LocalDate;

@Schema(description = "調達情報（政府調達契約）")
@Getter
public class ProcurementDto {

    @Schema(description = "調達件名", example = "システム開発業務委託")
    private final String title;

    @Schema(description = "契約金額（円）", example = "15000000")
    private final Long amount;

    @Schema(description = "発注日", example = "2023-04-01")
    private final LocalDate dateOfOrder;

    @Schema(description = "発注府省", example = "デジタル庁")
    private final String governmentDepartments;

    @Schema(description = "備考")
    private final String note;

    public ProcurementDto(CompanyProcurement entity) {
        this.title = entity.getTitle();
        this.amount = entity.getAmount();
        this.dateOfOrder = entity.getDateOfOrder();
        this.governmentDepartments = entity.getGovernmentDepartments();
        this.note = entity.getNote();
    }
}

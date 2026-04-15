package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.CompanyProcurement;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class ProcurementDto {
    private final String title;
    private final Long amount;
    private final LocalDate dateOfOrder;
    private final String governmentDepartments;
    private final String note;

    public ProcurementDto(CompanyProcurement entity) {
        this.title = entity.getTitle();
        this.amount = entity.getAmount();
        this.dateOfOrder = entity.getDateOfOrder();
        this.governmentDepartments = entity.getGovernmentDepartments();
        this.note = entity.getNote();
    }
}

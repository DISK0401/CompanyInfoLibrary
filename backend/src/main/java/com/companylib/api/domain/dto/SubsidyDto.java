package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.CompanySubsidy;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class SubsidyDto {
    private final String title;
    private final Long amount;
    private final LocalDate dateOfApproval;
    private final String governmentDepartments;
    private final String subsidyResource;
    private final String target;
    private final String note;

    public SubsidyDto(CompanySubsidy entity) {
        this.title = entity.getTitle();
        this.amount = entity.getAmount();
        this.dateOfApproval = entity.getDateOfApproval();
        this.governmentDepartments = entity.getGovernmentDepartments();
        this.subsidyResource = entity.getSubsidyResource();
        this.target = entity.getTarget();
        this.note = entity.getNote();
    }
}

package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.CompanyCertification;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class CertificationDto {
    private final String title;
    private final String category;
    private final LocalDate dateOfApproval;
    private final LocalDate expireDate;
    private final String governmentDepartments;
    private final String target;

    public CertificationDto(CompanyCertification entity) {
        this.title = entity.getTitle();
        this.category = entity.getCategory();
        this.dateOfApproval = entity.getDateOfApproval();
        this.expireDate = entity.getExpireDate();
        this.governmentDepartments = entity.getGovernmentDepartments();
        this.target = entity.getTarget();
    }
}

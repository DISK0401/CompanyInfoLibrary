package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.CompanyCommendation;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class CommendationDto {
    private final String title;
    private final LocalDate dateOfCommendation;
    private final String governmentDepartments;
    private final String category;
    private final String note;
    private final String target;

    public CommendationDto(CompanyCommendation entity) {
        this.title = entity.getTitle();
        this.dateOfCommendation = entity.getDateOfCommendation();
        this.governmentDepartments = entity.getGovernmentDepartments();
        this.category = entity.getCategory();
        this.note = entity.getNote();
        this.target = entity.getTarget();
    }
}

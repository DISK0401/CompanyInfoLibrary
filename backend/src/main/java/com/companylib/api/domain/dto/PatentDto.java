package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.CompanyPatent;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class PatentDto {
    private final String patentType;
    private final String title;
    private final LocalDate dateOfPatent;
    private final String patentNumber;
    private final String classifications;
    private final String url;

    public PatentDto(CompanyPatent entity) {
        this.patentType = entity.getPatentType();
        this.title = entity.getTitle();
        this.dateOfPatent = entity.getDateOfPatent();
        this.patentNumber = entity.getPatentNumber();
        this.classifications = entity.getClassifications();
        this.url = entity.getUrl();
    }
}

package com.companylib.api.service;

public class CompanyNotFoundException extends RuntimeException {
    public CompanyNotFoundException(String corporateNumber) {
        super("法人番号 " + corporateNumber + " の企業が見つかりません");
    }
}

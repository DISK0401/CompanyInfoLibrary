package com.companylib.batch.infrastructure.gbizinfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class UpdateInfoResponse {
    @JsonProperty("hojin-infos")
    private List<UpdatedHojinInfo> hojinInfos;

    @Data
    public static class UpdatedHojinInfo {
        @JsonProperty("corporate_number")
        private String corporateNumber;

        @JsonProperty("update_date")
        private String updateDate;
    }
}

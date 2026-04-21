package com.companylib.batch.infrastructure.gbizinfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class UpdateInfoResponse {
    @JsonProperty("hojin-infos")
    private List<HojinInfo> hojinInfos;

    @JsonProperty("pageNumber")
    private Integer pageNumber;

    @JsonProperty("totalCount")
    private Integer totalCount;

    @JsonProperty("totalPage")
    private Integer totalPage;
}

package com.companylib.batch.infrastructure.gbizinfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class HojinInfoResponse {
    @JsonProperty("hojin-infos")
    private List<HojinInfo> hojinInfos;
}

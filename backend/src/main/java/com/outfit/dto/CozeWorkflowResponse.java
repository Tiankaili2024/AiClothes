package com.outfit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CozeWorkflowResponse {
    private Integer code;
    private String msg;
    private String data;

    @JsonProperty("debug_url")
    private String debugUrl;

    @JsonProperty("execute_id")
    private String executeId;
}

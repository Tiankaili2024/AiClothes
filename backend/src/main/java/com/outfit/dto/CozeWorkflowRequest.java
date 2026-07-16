package com.outfit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class CozeWorkflowRequest {
    @JsonProperty("workflow_id")
    private String workflowId;
    private Map<String, Object> parameters;
}
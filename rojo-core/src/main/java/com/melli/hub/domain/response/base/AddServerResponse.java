package com.melli.hub.domain.response.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class AddServerResponse extends BaseResponse {

    @JsonProperty("countAllRow")
    private String countAllRow;
    @JsonProperty("countSuccessRow")
    private String countSuccessRow;
    @JsonProperty("countFailRow")
    private String countFailRow;
}

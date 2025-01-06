package com.melli.hub.domain.response.base;

import lombok.*;

/**
 * Created by shirinabadi on 1/23/2017.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginResponse {
    private String accessToken;
    private long expireTime;
}

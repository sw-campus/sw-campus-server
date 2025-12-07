package com.swcampus.api.auth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailStatusResponse {

    private String email;
    private boolean verified;

    public static EmailStatusResponse of(String email, boolean verified) {
        return new EmailStatusResponse(email, verified);
    }
}

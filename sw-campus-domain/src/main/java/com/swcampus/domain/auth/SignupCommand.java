package com.swcampus.domain.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupCommand {
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String phone;
    private String location;
}

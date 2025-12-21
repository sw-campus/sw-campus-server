package com.swcampus.domain.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrganizationSignupCommand {
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String phone;
    private String location;
    private String organizationName;
    private byte[] certificateImage;
    private String certificateFileName;
    private String certificateContentType;
    private Long organizationId;  // 기존 기관 선택 시 사용 (null이면 신규 기관)
}

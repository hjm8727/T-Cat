package com.kh.finalproject.dto.member;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class SigninRequestDTO {
    private String id;
    private String password;
//    @NotNull(message = "이메일은 필수입니다")
    private String email;
    @NotNull(message = "providerType은 필수입니다")
    private String providerType;
}

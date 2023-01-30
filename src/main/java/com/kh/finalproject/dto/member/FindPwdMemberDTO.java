package com.kh.finalproject.dto.member;

import com.kh.finalproject.entity.Member;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class FindPwdMemberDTO {

    @NotNull(message = "아이디는 필수 입력 값")
    private String id;
    @NotNull(message = "이름은 필수 입력 값")
    private String name;
    @NotNull(message = "이메일은 필수 입력 값")
    private String email;
    @NotNull(message = "회원 주체는 필수값")
    private String providerType;
}

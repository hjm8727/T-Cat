package com.kh.finalproject.exception;

import com.kh.finalproject.response.StatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 사용자 정의 예외 처리 상수 클래스
 */
@Getter
@AllArgsConstructor
public enum CustomErrorCode {
    DUPLI_MEMBER_ID(StatusCode.BAD_REQUEST, "M001", "이미 가입된 ID가 있습니다"),
    EMPTY_MEMBER(StatusCode.BAD_REQUEST, "M002", "조회한 회원이 없습니다"),
    ERROR_UPDATE_UNREGISTER_MEMBER(StatusCode.BAD_REQUEST, "M003", "회원의 탈퇴 상태 변환이 실패했습니다"),
    EMPTY_MEMBER_ACTIVE_LIST(StatusCode.BAD_REQUEST, "M004", "조회한 활성 상태 회원 목록이 없습니다"),
    EMPTY_MEMBER_BLAK_LIST(StatusCode.BAD_REQUEST, "M005", "조회한 블랙 상태 회원 목록이 없습니다"),
    UNREGISTER_MEMBER_SIGN(StatusCode.BAD_REQUEST, "M005", "이미 탈퇴한지 1주일이 지난 회원입니다. (재가입 불가)"),
    BLACKLIST_MEMBER_SIGN(StatusCode.BAD_REQUEST, "M005", "저희 사이트에 블랙리스트 대상입니다."),
    DUPLI_EMAIL(StatusCode.BAD_REQUEST, "M006", "이미 가입된 이메일이 있습니다"),
    DUPLI_EMAIL_NAME(StatusCode.BAD_REQUEST, "M007", "이름과 이메일로 조회되는 회원이 있습니다"),
    NOT_MATCH_ID_EMAIL_NAME(StatusCode.BAD_REQUEST, "M008", "아이디 혹은 이메일 혹은 이름이 올바르지 않습니다"),
    ERROR_UPDATE_MEMBER_INFO(StatusCode.BAD_REQUEST, "M009", "회원 정보가 올바르게 업데이트되지 않았습니다"),
    EMPTY_PASSWORD(StatusCode.BAD_REQUEST, "M010", "비밀번호가 없습니다"),
    EMPTY_EMAIL(StatusCode.BAD_REQUEST, "M011", "이메일이 없습니다"),
    NOT_MATCH_PROVIDER_TYPE(StatusCode.BAD_REQUEST, "M012", "회원 정보 주체가 올바르지 않습니다"),
    ERROR_POINT_INFO(StatusCode.BAD_REQUEST, "M013", "포인트 값이 잘못 설정되어있습니다"),
    ERROR_UNREGISTER(StatusCode.BAD_REQUEST, "M014", "영구정지 회원입니다"),
    ERROR_MEMBER_ACCUSED_OVER_FIVE(StatusCode.BAD_REQUEST, "M015", "신고 횟수가 5회 이상 회원이 존재하지 않습니다"),
    DUPLI_NAME(StatusCode.BAD_REQUEST, "M016", "이름으로 조회되는 회원이 있습니다"),
    DELETE_MEMBER_SIGN(StatusCode.BAD_REQUEST, "M017", "삭제 신청 회원입니다"),
    ACTIVE_MEMBER_SIGN(StatusCode.BAD_REQUEST, "M018", "현재 강비 회원입니다"),
    EMPTY_ID(StatusCode.BAD_REQUEST, "M019", "아이디가 없습니다"),
    ERROR_BLACKLIST(StatusCode.BAD_REQUEST, "M020", "이미 해당 회원은 블랙리스트 회원입니다"),
    NOT_SEARCH_ID(StatusCode.BAD_REQUEST, "M021", "해당 아이디로 조회된 회원이 없습니다"),
    EMPTY_MEMBER_ACCUSE_COUNT(StatusCode.BAD_REQUEST, "M021", "5회 이상 신고 누적된 회원이 없습니다"),

    EMPTY_REVIEW_COMMENT(StatusCode.BAD_REQUEST, "RC001", "조회한 후기가 없습니다"),
    OVERLAP_REVIEW_COMMENT(StatusCode.BAD_REQUEST, "RC002", "중복 신고가 되었습니다"),
    EMPTY_NOTICE(StatusCode.BAD_REQUEST, "N001", "조회한 공지사항이 존재하지 않습니다"),
    ERROR_UPDATE_NOTICE_INFO(StatusCode.BAD_REQUEST, "N002", "공지사항이 올바르게 업데이트되지 않았습니다"),

    ERROR_EMPTY_PRODUCT_CODE(StatusCode.BAD_REQUEST, "P001", "해당 상품코드를 찾을 수 없습니다"),
    ERROR_EMPTY_STATIC_BY_PRODUCT_CODE(StatusCode.BAD_REQUEST, "ST001", "해당 상품코드의 통계를 찾을 수 없습니다"),
    ERROR_EMPTY_QNA(StatusCode.BAD_REQUEST, "Q001", "조회한 문의가 없습니다"),
    EMPTY_CASTING(StatusCode.BAD_REQUEST, "CA001", "조회한 캐스팅 정보가 없습니다"),
    EMPTY_RESERVE_TIME(StatusCode.BAD_REQUEST, "RT001", "조회한 예매 정보가 없습니다"),
    ERROR_LAYER(StatusCode.BAD_REQUEST, "RC001", "레이어값이 올바르지 않습니다"),
    ERROR_RATE(StatusCode.BAD_REQUEST, "RC002", "후기 작성이 평점 입력은 필수입니다"),

    EMPTY_RESERVE_TIME_CASTING(StatusCode.BAD_REQUEST, "RTC001", "조회한 시간별 예매 정보가 없습니다"),
    ERROR_REMAIN_QUANTITY(StatusCode.BAD_REQUEST, "RTC002", "잔여 좌석이 부족합니다"),
    EMPTY_KAKAO_TID(StatusCode.BAD_REQUEST, "RTC003", "카카오페이 TID가 없습니다"),
    EMPTY_KAKAO_TAX_FREE(StatusCode.BAD_REQUEST, "RTC004", "카카오페이 면세 금액이 없습니다"),

    ERROR_KAKAO_LOGIN(StatusCode.BAD_REQUEST, "SOCIAL02", "카카오 API가 실패했습니다"),

    ERROR_SOCIAL_LOGIN(StatusCode.BAD_REQUEST, "SOCIAL01", "소셜 로그인이 실패했습니다"),
    ERROR_RESERVE_TIME_SEAT_PRICE(StatusCode.BAD_REQUEST, "RTSP01", "상세 예매 좌석 가격 조회가 실패했습니다"),

    EMPTY_RESERVE(StatusCode.BAD_REQUEST, "R001", "예매 내역이 존재하지 않습니다"),
    EMPTY_CHART(StatusCode.BAD_REQUEST, "C001", "차트 내역이 존재하지 않습니다"),

    DUPLI_REVIEW_LIKE(StatusCode.BAD_REQUEST, "RL001", "해당 회원은 이미 이 후기에 좋아요를 눌렀습니다"),
    DUPLI_WISH_PRODUCT(StatusCode.BAD_REQUEST, "WP001", "해당 회원은 이미 해당 상품에 찜하기를 했습니다"),
    EMPTY_WISH_PRODUCT(StatusCode.BAD_REQUEST, "WP002", "해당 상품에 찜하기를 누르지 않았습니다"),

    EMPTY_RESERVE_TIME_SEAT_PRICE(StatusCode.BAD_REQUEST, "RTSP001", "상세 예매 좌석 정보가 없습니다"),
    NOT_MATCH_PASSWORD_ID(StatusCode.BAD_REQUEST, "J003", "이메일 혹은 아이디가 일치하지 않습니다"),
    NOT_MATCH_ID(StatusCode.BAD_REQUEST, "J004", "비밀번호가 일치하지 않습니다"),
    NOT_MATCH_EMAIL_NAME(StatusCode.BAD_REQUEST, "J006", "이메일 혹은 이름이 올바르지 않습니다"),
    NOT_SEARCH_DATA(StatusCode.BAD_REQUEST, "J007", "검색한 결과가 없습니다"),
    NOT_MOVIE_DATA(StatusCode.BAD_REQUEST, "J008", "조회한 결과가 없습니다"),
    INVALID_TYPE_VALUE(StatusCode.BAD_REQUEST, "C001", "입력 타입이 올바르지 않습니다"),
    INVALID_INPUT_VALUE(StatusCode.BAD_REQUEST, "C002", "입력 값이 올바르지 않습니다"),
    METHOD_NOT_ALLOWED(StatusCode.METHOD_NOT_ALLOWED, "C003", "지원하지않은 Http Method입니다"),
    HANDLE_ACCESS_DENIED(StatusCode.FORBIDDEN, "C004", "접근이 거부되었습니다"),
    INTERNAL_SERVER_ERROR(StatusCode.INTERNAL_SERVER_ERROR, "S001", "내부 서버 오류입니다");

    private final int status;
    private final String code;
    private final String message;
}

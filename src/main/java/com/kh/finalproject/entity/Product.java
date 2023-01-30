package com.kh.finalproject.entity;

import com.kh.finalproject.entity.enumurate.ProductCategory;
import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 상품 테이블과 연결된 엔티티
 */
@Getter
@Entity
@Table(name = "product")
public class Product {
    @Id
    @Column(name = "product_code")
    private String code;

    @Column(name = "product_url", nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_category", nullable = false)
    private ProductCategory category;

    @Column(name = "product_title", nullable = false)
    private String title;

    @Column(name = "product_thumb_poster_url", nullable = false)
    private String thumbPosterUrl;

    @Column(name = "product_detail_poster_url", nullable = false)
    private String detailPosterUrl;

    @Column(name = "product_casting_poster_url")
    private String castingPosterUrl;

    @Column(name = "product_location", nullable = false)
    private String location;

    @Column(name = "product_detail_location")
    private String detailLocation;

    @Column(name = "product_period_start", nullable = false)
    private String periodStart;

    @Column(name = "product_period_end")
    private String periodEnd;

    @Column(name = "product_age", nullable = false)
    private Integer age;

    @Column(name = "product_age_is_korean", nullable = false)
    private Boolean ageIsKorean;

    @Column(name = "product_time_min", nullable = false)
    private Integer timeMin;

    @Column(name = "product_time_break", nullable = false)
    private Integer timeBreak;

    @Column(name = "product_is_info_casting", nullable = false)
    private Boolean isInfoCasting;

    @Column(name = "product_is_info_time_casting", nullable = false)
    private Boolean isInfoTimeCasting;

    @Column(name = "product_rate_average", nullable = false)
    private Float rateAverage;

    @Column(name = "product_rate_total")
    private Float rateTotal;

    @Column(name = "product_rate_count")
    private Integer rateMemberCount;

    @OneToMany(mappedBy = "product")
    private List<Casting> castingList = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<SeatPrice> seatPrice;

    @OneToMany(mappedBy = "product")
    private List<ReserveTime> reserveTimeList = new ArrayList<>();

    @OneToOne(mappedBy = "product")
    private Statistics statistics;

//    @OneToMany(mappedBy = "product")
//    private List<RankingWeek> rankingWeekList = new ArrayList<>();

//    @OneToMany(mappedBy = "product")
//    private List<RankingMonth> rankingMonthList = new ArrayList<>();

//    @OneToMany(mappedBy = "product")
//    private List<RankingCloseSoon> rankingCloseSoonList = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<WishProduct> wishProductList = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<ReviewComment> reviewCommentList = new ArrayList<>();

//    public void code(Statistics statistics) {
//        this.statistics = statistics;
//    }

    /**
     * 예매 관련 데이터가 null인지 확인하고
     * 평점 회원수, 총 평점, 평균 평점 갱신
     */
    public void updateRate(Float rateTotal) {
        //null값이면 0으로 초기화
        updateInit();
        ++this.rateMemberCount;
        this.rateTotal += rateTotal;
        this.rateAverage = this.rateTotal / this.rateMemberCount;
    }

    /**
     * 현재 rateTotal이 null이면 0으로 초기화
     * nullable이 아니라서 초기화 필요
     */
    public void updateInit() {
        if (Objects.isNull(this.rateTotal)) {
            this.rateTotal = (float) 0;
            this.rateMemberCount = 0;
        }
    }

    /**
     * 평점 변경시(후기 변경 등) 평균 평점 다시 계산
     * @param changeRate: 이전 평점 - 변경 평점
     */
    public void updateChangeRate(Float changeRate) {
        this.rateTotal += changeRate;
        this.rateAverage = this.rateTotal / this.rateMemberCount;
    }
}
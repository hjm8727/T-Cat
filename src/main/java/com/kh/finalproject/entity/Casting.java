package com.kh.finalproject.entity;

import lombok.Getter;

import javax.persistence.*;
import java.util.List;

/**
 * 캐스팅 테이블과 연결된 엔티티
 */
@Getter
@Entity
@Table(name = "casting")
public class Casting {
    @Id
    @Column(name = "casting_id")
    private String id;

    @Column(name = "casting_info_url")
    private String infoUrl;

    @Column(name = "casting_img_url")
    private String imageUrl;

    @Column(name = "casting_character", nullable = false)
    private String character;

    @Column(name = "casting_actor", nullable = false)
    private String actor;

    @Column(name = "casting_order", nullable = false)
    private Integer order;

    @OneToMany(mappedBy = "casting")
    private List<ReserveTimeCasting> reserveTimeCastingList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code")
    private Product product;
}

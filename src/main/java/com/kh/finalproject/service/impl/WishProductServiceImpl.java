package com.kh.finalproject.service.impl;

import com.kh.finalproject.dto.product.ProductDTO;
import com.kh.finalproject.dto.wishProduct.AddWishProductDTO;
import com.kh.finalproject.dto.wishProduct.DeleteWishProductDTO;
import com.kh.finalproject.dto.wishProduct.WishProductDTO;
import com.kh.finalproject.entity.Member;
import com.kh.finalproject.entity.Product;
import com.kh.finalproject.entity.WishProduct;
import com.kh.finalproject.exception.CustomErrorCode;
import com.kh.finalproject.exception.CustomException;
import com.kh.finalproject.repository.MemberRepository;
import com.kh.finalproject.repository.ProductRepository;
import com.kh.finalproject.repository.WishProductRepository;
import com.kh.finalproject.service.WishProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WishProductServiceImpl implements WishProductService {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final WishProductRepository wishProductRepository;

    /**
     * 찜하기 추가 서비스
     */
    @Transactional
    @Override
    public void addWish(AddWishProductDTO addWishProductDTO) {
        Member wishMember = memberRepository.findByIndex(addWishProductDTO.getMemberIndex())
                .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_MEMBER));
        Product product = productRepository.findByCode(addWishProductDTO.getProductCode())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ERROR_EMPTY_PRODUCT_CODE));

        //중복 찜하기 확인
        if (isNotDupliWishProduct(wishMember, product)) {

            //엔티티 생성
            WishProduct wishProduct = new WishProduct().toEntity(wishMember, product);

            //DB 저장
            wishProductRepository.save(wishProduct);
        } else throw new CustomException(CustomErrorCode.DUPLI_WISH_PRODUCT); //예외 생성
    }

    /**
     * 동일한 회원이 후기/댓글에 좋아요 눌렀는지 확인
     */
    public Boolean isNotDupliWishProduct(Member wishMember,
                                  Product product) {
        return wishProductRepository.findByMemberAndProduct(wishMember, product)
                .isEmpty();
    }

    @Override
    @Transactional
    public void cancelWish(DeleteWishProductDTO deleteWishProductDTO) {
        Member wishMember = memberRepository.findByIndex(deleteWishProductDTO.getMemberIndex())
                .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_MEMBER));
        Product product = productRepository.findByCode(deleteWishProductDTO.getProductCode())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ERROR_EMPTY_PRODUCT_CODE));

        //찜하기 확인
        WishProduct wishProduct = wishProductRepository.findByMemberAndProduct(wishMember, product)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_WISH_PRODUCT));

        //찜하기 삭제
        wishProductRepository.delete(wishProduct);
    }

    /**
     * 회원 인덱스로 찜한 상품 조회 서비스
     * @param memberIndex: 회원 인덱스
     * @return 회원이 찜한 상품 리스트
     */
    @Override
    public List<WishProductDTO> selectByMember(Long memberIndex) {
        //회원 조회
        Member findMember = memberRepository.findByIndex(memberIndex)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_MEMBER));
        //찜한 상품 조회
        List<WishProduct> wishProductList = wishProductRepository.findByMember(findMember)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_WISH_PRODUCT));

        //찜한 상품 목록 생성 후 추가 및 반환
        List<WishProductDTO> wishProductDTOList = new LinkedList<>();

        for (WishProduct wishProduct : wishProductList) {
            wishProductDTOList.add(new WishProductDTO().toDTO(wishProduct.getProduct()));
        }

        return wishProductDTOList;
    }

    @Override
    public List<WishProductDTO> selectAll() {
        return null;
    }
}

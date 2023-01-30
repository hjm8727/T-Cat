package com.kh.finalproject.service.impl;

import com.kh.finalproject.dto.member.*;
import com.kh.finalproject.entity.Address;
import com.kh.finalproject.entity.Chart;
import com.kh.finalproject.entity.Member;
import com.kh.finalproject.entity.Reserve;
import com.kh.finalproject.entity.enumurate.MemberProviderType;
import com.kh.finalproject.entity.enumurate.MemberStatus;
import com.kh.finalproject.exception.CustomErrorCode;
import com.kh.finalproject.exception.CustomException;
import com.kh.finalproject.repository.AddressRepository;
import com.kh.finalproject.repository.ChartRepository;
import com.kh.finalproject.repository.MemberRepository;
import com.kh.finalproject.repository.ReserveRepository;
import com.kh.finalproject.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;

    private final ChartRepository chartRepository;

    private final ReserveRepository reserveRepository;

    /**
     * 회원 가입 메서드
     * 완전 탈퇴(UNREGISTER)와 블랙 리스트(BLACKLIST)의 가입을 막고
     * 홈 가입 회원의 아이디 중복 검사
     * 동일 주체 회원의 이메일 중복 검사
     * 소셜 로그인 회원이면 랜덤 ID 생성
     * 회원과 주소 정보 저장
     */
    @Override
    @Transactional
    public void signup(SignupDTO signupDto) {
        //회원의 가입 주체
        MemberProviderType signupProviderType = MemberProviderType.valueOf(signupDto.getProviderType());

        //홈 가입 회원인데 아이디가 없다면
        if (signupProviderType == MemberProviderType.HOME && Objects.isNull(signupDto.getId())) {
            throw new CustomException(CustomErrorCode.EMPTY_ID);
        }

        //홈 가입 회원인데 비밀번호가 없다면
        if (signupProviderType == MemberProviderType.HOME && Objects.isNull(signupDto.getPassword())) {
            throw new CustomException(CustomErrorCode.EMPTY_PASSWORD);
        }

        //소셜 연동 회원인데 이메일 정보가 없으면 예외 처리
        if (signupProviderType != MemberProviderType.HOME && Objects.isNull(signupDto.getEmail())) {
            throw new CustomException(CustomErrorCode.EMPTY_EMAIL);
        }

        // DTO -> ENTITY 변환
        Member signMember = new Member().toEntity(signupDto, signupProviderType);

        // 해당 하는 회원 정보를 가져옴 재가입일 수도 있기 때문에 아이디 중복처리는 아직.
        Optional<Member> findMember = memberRepository.findByIdAndStatusNotAndProviderType(signMember.getId(), MemberStatus.UNREGISTER, signMember.getProviderType());

        // 아이디가 있다면 그 회원의 상태가 블랙리스트 또는 영구탈퇴라면 재가입 방지.
        if(findMember.isPresent()) {
            if (findMember.get().getStatus() == MemberStatus.UNREGISTER) {
                throw new CustomException(CustomErrorCode.UNREGISTER_MEMBER_SIGN);
            } else if(findMember.get().getStatus() == MemberStatus.BLACKLIST) {
                throw new CustomException(CustomErrorCode.BLACKLIST_MEMBER_SIGN);
            }
        }

        //ProviderType 및 아이디 중복 확인 (홈 가입 회원만)
        if (signMember.getProviderType() == MemberProviderType.HOME) {
            validateDuplicateById(signMember.getId(), signMember.getProviderType());
        }

        //ProviderType 및 이메일 중복 확인
        validateDuplicateByEmail(signMember.getEmail(), signMember.getProviderType());

        //소셜 로그인이면
        if (signMember.getProviderType() != MemberProviderType.HOME) {
            //랜덤 ID 생성 및 DB에 존재하지 않을 때 까지 조회
            String randomId;
            do {
                randomId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
            } while (memberRepository.findById(randomId).isPresent());
            //Id 갱신
            signMember.updateId(randomId);
        }

        log.info("signMember.toString() = {}", signMember.toString());

        //회원 가입 후 불러오기 (동일한 가입 주체 내 아이디 조회)
        memberRepository.save(signMember);
        Member savedMember = memberRepository.findByIdAndProviderType(signMember.getId(), signMember.getProviderType())
                .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_MEMBER));

        //주소 정보 저장
        Address signAddress = new Address().toEntity(signupDto, savedMember);
        addressRepository.save(signAddress);


        //관리자 차트 메서드
        int nowYear = LocalDateTime.now().getYear();
        int nowMonth = LocalDateTime.now().getMonthValue();

        //관리자 차트 이름 생성
        String charId;
        if (nowMonth < 10) charId = nowYear + "/0" + nowMonth;
        else charId = nowYear + "/" + nowMonth;

        LocalDateTime todayTime = LocalDateTime.now();
        LocalDateTime beforeMonthTime = todayTime.minusMonths(0);
        beforeMonthTime = LocalDateTime.of(beforeMonthTime.getYear(), beforeMonthTime.getMonth(), beforeMonthTime.with(lastDayOfMonth()).getDayOfMonth(), 23, 59, 59);

        //현재 개월의 차트 존재 유무
        boolean isChartExist = chartRepository.findById(charId).isPresent();

        //현재 월의 차트 정보가 없다면
        if (!isChartExist) {
            //차트 생성
            processCreateChart(charId, beforeMonthTime);
        }

        //현재 월의 차트 정보가 있다면
        else {
            //차트 조회
            Chart chart = chartRepository.findById(charId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_RESERVE));

            //해당 차트 정보 갱신
            chart.updateMember(1L);
        }
    }

    public void processCreateChart(String charId, LocalDateTime beforeMonthTime) {

        //지난 거래내역 존재하는지 확인
        boolean isReserveExist = reserveRepository.findAllByCreateTimeBefore(beforeMonthTime)
                .isEmpty();

        //UNREGISTER 상태가 아니고 조회 이전 월 이전 총 회원수 조회
//        log.info("debug!!! beforMonthTime = {}", beforeMonthTime);
        Integer memberCount = memberRepository.countAllByCreateTimeBefore(beforeMonthTime);

        //거래내역이 존재하지 않다면
        if (isReserveExist) {
            //현재 회원수만 기록한 관리자 차트 생성 및 저장
            Chart chart = new Chart().toEntity(charId, 0L, 0L, 0L, (long) memberCount, 0L);
            chartRepository.save(chart);
        }
        //거래내역이 존재한다면
        else {
            //이전 모든 거래내역 조회
            List<Reserve> findAllReserveListBefore = reserveRepository.findAllByCreateTimeBefore(beforeMonthTime)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_RESERVE));

            Long cumuAmount = 0L;
            Long cumuDiscount = 0L;
            Long finalAmount = 0L;
            Long totalMember = (long) memberCount;
            Long totalReserve = (long) findAllReserveListBefore.size();

            for (Reserve reserve : findAllReserveListBefore) {
                cumuAmount += reserve.getAmount();
                cumuDiscount += reserve.getDiscount();
                finalAmount += reserve.getAmount() - reserve.getDiscount();
            }

            //차트 생성 및 저장
            Chart chart = new Chart().toEntity(charId, cumuAmount, cumuDiscount, finalAmount, totalMember, totalReserve);
            chartRepository.save(chart);
        }
    }

    /**
     * 회원 정보 수정 메서드
     * 아이디와 동일한 가입 주체로 회원을 조회하되 UNREGISTER는 조회하지 않는다
     * 홈 가입 회원은 아이디, 비밀번호 공백 여부 확인 -> 홈 가입 회원 내 해당 회원 아이디 조회 -> 회원 정보 갱신
     * 소셜 가입 회원은 이메일 공백 여부 확인 -> 동일 소셜 가입 회원 내 이메일로 회원 조회 -> 회원 정보 갱신
     */
    @Override
    @Transactional
    public void editMemberInfoByHome(EditMemberInfoDTO memberInfoDTO) {
        //주어진 ID로 회원 조회
//        Member findMember = memberRepository.findByIdAndStatusNotAndProviderType(memberInfoDTO.getId(), MemberStatus.UNREGISTER, MemberProviderType.valueOf(memberInfoDTO.getProviderType()))
//                .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_MEMBER));
//
//        //주어진 회원과 연결된 주소 조회
//        Address findAddress = addressRepository.findByMember(findMember);
//
//        //회원 정보 갱신 (단순 값 교체 + 연관관계 편의 메서드)
//        findMember.updateMember(findAddress, memberInfoDTO);

        MemberProviderType editProviderType = MemberProviderType.valueOf(memberInfoDTO.getProviderType());
        //홈페이지 가입 회원일 시
        if (editProviderType  == MemberProviderType.HOME) {
            //아이디가 없으면 예외처리
            if (Objects.isNull(memberInfoDTO.getId())) {
                throw new CustomException(CustomErrorCode.EMPTY_ID);
            }
            //비밀번호 없으면 예외 처리
            if (Objects.isNull(memberInfoDTO.getPassword())) {
                throw new CustomException(CustomErrorCode.EMPTY_PASSWORD);
            }
            //회원 조회
            Member findMember = memberRepository.findByIdAndStatusNotAndProviderType(memberInfoDTO.getId(),
                            MemberStatus.UNREGISTER, MemberProviderType.HOME)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_SEARCH_ID));
            //주어진 회원과 연결된 주소 조회
            Address findAddress = addressRepository.findByMember(findMember);
            //정보 갱신
            findMember.updateMemberHome(findAddress, memberInfoDTO);
        } else {
            //이메일 정보가 없으면 예외 처리
            if (Objects.isNull(memberInfoDTO.getEmail())) {
                throw new CustomException(CustomErrorCode.EMPTY_EMAIL);
            }
            //회원 조회
            Member findMember = memberRepository.findByEmailAndStatusNotAndProviderType(
                            memberInfoDTO.getEmail(), MemberStatus.UNREGISTER, editProviderType)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_MEMBER));
            //주어진 회원과 연결된 주소 조회
            Address findAddress = addressRepository.findByMember(findMember);
            //정보 갱신
            findMember.updateMemberSocial(findAddress, memberInfoDTO);
        }
    }

    @Override
    public Boolean unregister(UnregisterDTO unregisterDTO) {
        return null;
    }

    /**
     * 중복 아이디 확인 메서드
     * 아이디와 동일한 가입 주체로 회원을 조회하되 UNREGISTER는 조회하지 않는다
     */
    @Transactional
    @Override
    public void validateDuplicateById(String id, MemberProviderType providerType) {
        //중복 회원 조회
        Optional<Member> findId = memberRepository.findByIdAndStatusNotAndProviderType(id, MemberStatus.UNREGISTER, providerType);

        //있다면 예외처리
        if(findId.isPresent()) {
            throw new CustomException(CustomErrorCode.DUPLI_MEMBER_ID);
        }
    }

    /**
     * 중복 이메일 확인 메서드
     * 이메일과 동일한 가입 주체로 회원을 조회하되 UNREGISTER는 조회하지 않는다
     */
    @Override
    @Transactional
    public void validateDuplicateByEmail(String email, MemberProviderType providerType) {

        Optional<Member> findEmail = memberRepository.findByEmailAndStatusNotAndProviderType(email, MemberStatus.UNREGISTER, providerType);

        //중복 이메일 존재 시 예외 처리
        if (findEmail.isPresent()) {
            throw new CustomException(CustomErrorCode.DUPLI_EMAIL);
        }
    }

    /**
     * 아이디로 회원 조회
     * 이 서비스는 홈페이지에서 가입한 회원에게만 제공됩니다
     * 따라서, 동일한 아이디와 홈페이지에서 가입한 회원만 조회하되 UNREGISTER는 조회하지 않습니다
     */
    @Transactional
    @Override
    public MemberDTO searchById(SearchByIdDTO searchByIdDTO) {
        log.info("searchByIdDTO.getProviderType()={}", searchByIdDTO.getProviderType());
        MemberProviderType loginProviderType = MemberProviderType.valueOf(searchByIdDTO.getProviderType());
        //홈페이지 가입 회원일 시
        if (loginProviderType  == MemberProviderType.HOME) {
            //아이디가 없으면 예외처리
            if (Objects.isNull(searchByIdDTO.getId())) {
                throw new CustomException(CustomErrorCode.EMPTY_ID);
            }
            //회원 조회
            Member findMember = memberRepository.findByIdAndStatusNotAndProviderType(searchByIdDTO.getId(),
                            MemberStatus.UNREGISTER, MemberProviderType.HOME)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_MEMBER));

            Address findAddress = addressRepository.findByMember(findMember);
            return new MemberDTO().toDTO(findMember, findAddress);
            //소셜 로그인 가입 회원일 시
        } else {
            //이메일 정보가 없으면 예외 처리
            if (Objects.isNull(searchByIdDTO.getEmail())) {
                throw new CustomException(CustomErrorCode.EMPTY_EMAIL);
            }
            //회원 조회
            Member findMember = memberRepository.findByEmailAndStatusNotAndProviderType(
                            searchByIdDTO.getEmail(), MemberStatus.UNREGISTER, loginProviderType)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_MEMBER));
            Address findAddress = addressRepository.findByMember(findMember);
            return new MemberDTO().toDTO(findMember, findAddress);
        }
        //홈에서 가입 회원이고 완전 탈퇴된 회원 제외
//        Member findId = memberRepository.findByIdAndStatusNotAndProviderType(id, MemberStatus.UNREGISTER, MemberProviderType.HOME)
//                .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_MEMBER));
//
//        Address memberAddress = addressRepository.findByMember(findId);
//
//        return new MemberDTO().toDTO(findId, memberAddress);
    }

    /**
     * 소셜 로그인 이메일 가입여부 확인
     * 해당 기능은 validateDuplicateByEmail와 중복되지만 반환값이 달라 작성했습니다
     * 해당 이메일과 동일 가입 주체로 조회하되 UNREGISTER가 아닌 회원만 조회합니다.
     * 조회된다면 True, 없다면 False
     */
    @Override
    public Boolean searchByEmailSocialLogin(String email, MemberProviderType providerType) {
        return memberRepository.findByEmailAndStatusNotAndProviderType(email, MemberStatus.UNREGISTER, providerType)
                .isPresent();
    }

    /**
     * 이름과 이메일로 아이디 찾는 기능
     * 이 서비스는 홈페이지에서 가입한 회원에게만 제공됩니다
     * 따라서, 동일한 이름과 이메일로 홈페이지에서 가입한 회원만 조회하되 UNREGISTER는 조회하지 않습니다
     * @return 조회 회원 ID
     */
    @Override
    @Transactional
    public Map<String, String> findMemberId(String name, String email) {
        //이름과 이메일로 회원 조회
        Member findNameAndEmail = memberRepository.findByNameAndEmailAndStatusNotAndProviderType(name, email, MemberStatus.UNREGISTER, MemberProviderType.HOME)
                .orElseThrow(() -> new CustomException(CustomErrorCode.DUPLI_EMAIL_NAME));

        Map<String, String> memberId = new LinkedHashMap<>();

        FindIdMemberDTO searchMemberId = new FindIdMemberDTO().toDTO(findNameAndEmail);

        memberId.put("member_id", searchMemberId.getId());

        return memberId;
    }

    /**
     * 아이디와 이름과 이메일로 비밀번호 조회
     * 이 서비스는 홈페이지에서 가입한 회원에게만 제공됩니다
     * 따라서, 이름과 이메일로 홈페이지에서 가입한 회원만 조회하되 UNREGISTER는 조회하지 않습니다
     * @return 조회 회원 비밀번호
     */
    @Transactional
    @Override
    public Map<String, String> findPassword(FindPwdMemberDTO findPwdMemberDTO) {
        //ProviderType과 맞지 않다면 예외
        if (MemberProviderType.valueOf(findPwdMemberDTO.getProviderType()) != MemberProviderType.HOME)
            throw new CustomException(CustomErrorCode.NOT_MATCH_PROVIDER_TYPE);

        //아이디, 이름, 이메일, Unregister가 아니고 상태가 Home인 회원 조회
        Member findIdNameEmail = memberRepository.findByIdAndNameAndEmailAndStatusNotAndProviderType(
                findPwdMemberDTO.getId(), findPwdMemberDTO.getName(), findPwdMemberDTO.getEmail(), MemberStatus.UNREGISTER, MemberProviderType.HOME)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_MATCH_ID_EMAIL_NAME));

        Map<String, String> memberPassword = new LinkedHashMap<>();

        memberPassword.put("password", findIdNameEmail.getPassword());

        return memberPassword;
    }

    /**
     * 회원이 탈퇴 버튼 누르면 상태 변경 1주일 후에는 영구 탈퇴
     */
    @Override
    @Transactional
    public Boolean deleteChangeMember(DeleteMemberDTO deleteMemberDTO) {
        MemberProviderType providerType = MemberProviderType.valueOf(deleteMemberDTO.getProviderType());
        if (providerType != MemberProviderType.HOME) {
            throw new CustomException(CustomErrorCode.NOT_MATCH_PROVIDER_TYPE);
        }

        // 아이디 패스워드로 조회 성공하면
        Member findMember = memberRepository.findByIdAndPasswordAndStatusNotAndProviderType(
                deleteMemberDTO.getId(), deleteMemberDTO.getPassword(), MemberStatus.UNREGISTER, MemberProviderType.valueOf(deleteMemberDTO.getProviderType()))
                .orElseThrow(() -> new CustomException(CustomErrorCode.ERROR_UPDATE_UNREGISTER_MEMBER));

        // 조회한 회원의 정보를 DELETE 업데이트 !!
        findMember.changeMemberStatusToUnregister(deleteMemberDTO);
        return true;
    }

    /**
     * DELETE인 회원 중에 update_time 이 1주일 지난 회원은 UNREGISTER
     */
    @Override
    @Transactional
    public void unregisterCheck() {
        // 현재 시간
        LocalDateTime now = LocalDateTime.now();

        // DELETE 회원 중 1주일이 지난 회원을 체크
        List<Member> deleteListMember = memberRepository.findAllByStatus(MemberStatus.DELETE)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_MEMBER));
        // 찾은 회원이 있으면 그 회원만 UNREGISTER 변경 !!
        for(Member deleteMember : deleteListMember) {
            if (now.isAfter(deleteMember.getUnregister().plusDays(7))) {
                // 1주일이 지난 회원들은 다 unregister status Change
                deleteMember.changeMemberStatusToUnregister();
            }
        }
    }

    @Override
    public MemberDTO searchByIdPassword(SearchByIdPasswordDTO searchByIdPasswordDTO) {
        return null;
    }

    @Override
    public MemberDTO searchByIndex(Long index) {
        return null;
    }

    @Override
    public void updatePassword(UpdatePasswordDTO updatePasswordDTO) {

    }
    /**
     * 전체 일반 회원 조회 서비스
     */
    @Override
    public PagingMemberDTO searchAllActiveMember(Pageable pageable) {
        //전체 회원 리스트 생성
        List<MemberDTO> memberDTOList = new ArrayList<>();

        //활성 상태 회원 조회
        Page<Member> pageMemberList = memberRepository.findByStatus(MemberStatus.ACTIVE, pageable);

        if (Objects.isNull(pageMemberList)) {
            throw new CustomException(CustomErrorCode.EMPTY_MEMBER_ACTIVE_LIST);
        }

        List<Member> memberList = pageMemberList.getContent();
        Integer totalPages = pageMemberList.getTotalPages();
        System.out.println(totalPages);
        Integer page = pageMemberList.getNumber()+1;
        Long totalResults = pageMemberList.getTotalElements();

        //리스트에 조회한 회원 추가
        for(Member member : memberList){
            Address memberAddress = addressRepository.findByMember(member);
            MemberDTO memberDTO = new MemberDTO().toDTO(member, memberAddress);
            memberDTOList.add(memberDTO);
        }

        return new PagingMemberDTO().toPageDTO(page,totalPages,totalResults,memberDTOList);
    }

    /**
     * 전체 블랙리스트 회원 조회
     */
    @Override
    public PagingMemberDTO searchAllBlackMember(Pageable pageable) {
        //회원 목록
        List<MemberDTO> memberDTOList = new ArrayList<>();

        //블랙 상태 회원 조회
        Page<Member> pageMemberList = memberRepository.findByStatus(MemberStatus.BLACKLIST,pageable);

        //리스트에 조회한 회원 추가
        List<Member> memberList = pageMemberList.getContent();
        Integer totalPages = pageMemberList.getTotalPages();
        Integer page = pageMemberList.getNumber()+1;
        Long totalResults = pageMemberList.getTotalElements();

        //리스트에 조회한 회원 추가
        for(Member member : memberList){
            Address memberAddress = addressRepository.findByMember(member);
            MemberDTO memberDTO = new MemberDTO().toDTO(member, memberAddress);
            memberDTOList.add(memberDTO);
        }

        return new PagingMemberDTO().toPageDTO(page,totalPages,totalResults,memberDTOList);
    }

    @Override
    public void updateTotalMemberInChart(Integer count) {
    }

    @Override
    public void updateStatusInReviewComment(Long index) {
    }

    /**
     * 회원 상태 변환 메서드
     */
    /*체크박스로 회원 강제 탈퇴*/
    @Transactional
    public void changeMemberStatusToUnregister(List<CheckMemberDTO> checkMemberDTOList){
        List<Member> blackMemberList = new ArrayList<>();
        for (CheckMemberDTO memberDTO : checkMemberDTOList) {
            //회원 조회, 없다면 예외
            Member findMember = memberRepository.findByIndex(memberDTO.getIndex())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_MEMBER));
            blackMemberList.add(findMember);
            log.info("확인 회원" + findMember);
        }
        //강제탈퇴 되는 회원으로 전환
        for (Member member : blackMemberList) {
//            member.updateStatus(MemberStatus.BLACKLIST);
            member.updateStatus(MemberStatus.UNREGISTER);

//            member.changeMemberStatusToUnregister();
        }
    }

    /*신고누적 5회인 회원 블랙리스트로 전환*/
    @Override
    @Transactional
    public List<MemberDTO> updateStatusByCount() {
        //신고횟수 5회 이상 회원 조회(일반회원인 경우에만 조회 => 아니면 unregister 회원도 같이 계산되는거 같음=>아님 그래도 오류)
        Optional<List<Member>> findMemberList = memberRepository.findAllByStatusAndAccuseCountGreaterThan(MemberStatus.ACTIVE,4);

        if (findMemberList.isEmpty())
            throw new CustomException(CustomErrorCode.EMPTY_MEMBER_ACCUSE_COUNT);

        //전체 블랙리스트 회원 리스트에 저장 및 반환
        List<MemberDTO> memberDTOList = new ArrayList<>();
        for (Member member : findMemberList.get()) {
            memberDTOList.add(new MemberDTO().toDTOByCount(member));
            member.updateBlackByCount();
        }
        return memberDTOList;
    }

    /**
     * 로그인 서비스
     * 로그인 DTO로 로그인 요청을 할 때
     * ProviderType은 반드시 담겨야합니다.
     * 홈페이지 가입 회원은 아이디, 비밀번호, 이름이 반드시 있어야하고
     * 소셜 로그인 회원은 이메일, 이름이 반드시 있어야합니다
     * @param signinRequestDTO: 로그인 요청 DTO
     * @return
     */
    @Override
    public SigninResponseDTO signIn(SigninRequestDTO signinRequestDTO) {
        MemberProviderType loginProviderType = MemberProviderType.valueOf(signinRequestDTO.getProviderType());
        log.info("signinRequestDTO.getProviderType() = {}", signinRequestDTO.getProviderType());
        //홈페이지 가입 회원일 시
        if (loginProviderType  == MemberProviderType.HOME) {
            //아이디가 없으면 예외처리
            if (Objects.isNull(signinRequestDTO.getId())) {
                throw new CustomException(CustomErrorCode.EMPTY_ID);
            }
            //비밀번호 없으면 예외 처리
            if (Objects.isNull(signinRequestDTO.getPassword())) {
                throw new CustomException(CustomErrorCode.EMPTY_PASSWORD);
            }
            //회원 조회
//            Member findMember = memberRepository.findByIdAndPasswordAndStatusNotAndProviderType(signinRequestDTO.getId(),
//                            signinRequestDTO.getPassword(), MemberStatus.UNREGISTER, MemberProviderType.HOME)
//                     .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_MEMBER));
//            return new SigninResponseDTO().toDTO(findMember);

            // 회원 조회 멤버 상태는 따로 구분해서 조회 안해도 프론트에서 막아놔서 이렇게 수정했습니다
            // 언레지스터인 사람이 들어오면 영구탈퇴라고 보여주게 해놨습니다. 혹시나 수정했는데 에러사항 있으시면 말씀 부탁드립니다.
            Member findMember = memberRepository.findByIdAndPasswordAndProviderType(signinRequestDTO.getId(),
                            signinRequestDTO.getPassword(), MemberProviderType.HOME)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_MEMBER));
            return new SigninResponseDTO().toDTO(findMember);
        //소셜 로그인 가입 회원일 시
        } else {
            //이메일 정보가 없으면 예외 처리
            if (Objects.isNull(signinRequestDTO.getEmail())) {
                throw new CustomException(CustomErrorCode.EMPTY_EMAIL);
            }
            //회원 조회
            Member findMember = memberRepository.findByEmailAndStatusNotAndProviderType(
                    signinRequestDTO.getEmail(), MemberStatus.UNREGISTER, loginProviderType)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.EMPTY_MEMBER));
            return new SigninResponseDTO().toDTO(findMember);
        }
    }

    /**
     * 회원탈퇴 신청 1주일이 지나지 않은 회원 복구 메서드
     * 먼저 1주일이 지난 회원을 완전 탈퇴 시켰음으로 아이디, 비밀번호, 상태가 Delete, Home가입 유저만 조회
     *
     */
    @Override
    @Transactional
    public void deleteCancelMember(DeleteCancelDTO deleteCancelDTO) {
        MemberProviderType providerType = MemberProviderType.valueOf(deleteCancelDTO.getProviderType());
        //가입 주체가 HOME이 아니면
        if (providerType != MemberProviderType.HOME) {
            throw new CustomException(CustomErrorCode.NOT_MATCH_PROVIDER_TYPE);
        }
        // DELETE 상태인 회원만 조회
        Member deleteMember = memberRepository.findByIdAndPasswordAndStatusAndProviderType
                        (deleteCancelDTO.getId(), deleteCancelDTO.getPassword(), MemberStatus.DELETE, MemberProviderType.HOME)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_SEARCH_ID));
        // ACTIVE 변경
        deleteMember.cancelDelete(deleteCancelDTO);
    }
}
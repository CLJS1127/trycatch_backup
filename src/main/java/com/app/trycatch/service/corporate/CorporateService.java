package com.app.trycatch.service.corporate;

import com.app.trycatch.common.pagination.Criteria;
import com.app.trycatch.domain.corporate.CorpInviteVO;
import com.app.trycatch.domain.member.CorpVO;
import com.app.trycatch.domain.corporate.CorpTeamMemberVO;
import com.app.trycatch.domain.corporate.CorpWelfareRelVO;
import com.app.trycatch.domain.member.MemberVO;
import com.app.trycatch.dto.corporate.CorpTeamMemberDTO;
import com.app.trycatch.dto.corporate.ApplicantWithPagingDTO;
import com.app.trycatch.dto.corporate.CorpTeamMemberWithPagingDTO;
import com.app.trycatch.dto.experience.ApplyDTO;
import com.app.trycatch.dto.experience.ExperienceProgramDTO;
import com.app.trycatch.dto.corporate.CorpProgramWithPagingDTO;
import com.app.trycatch.dto.member.CorpMemberDTO;
import com.app.trycatch.dto.member.MemberDTO;
import com.app.trycatch.dto.corporate.ParticipantWithPagingDTO;
import com.app.trycatch.dto.experience.ChallengerDTO;
import com.app.trycatch.dto.experience.FeedbackDTO;
import com.app.trycatch.repository.corporate.CorpInviteDAO;
import com.app.trycatch.repository.corporate.CorpTeamMemberDAO;
import com.app.trycatch.repository.experience.AddressProgramDAO;
import com.app.trycatch.repository.experience.ApplyDAO;
import com.app.trycatch.repository.experience.ChallengerDAO;
import com.app.trycatch.repository.experience.ExperienceProgramDAO;
import com.app.trycatch.repository.experience.ExperienceProgramFileDAO;
import com.app.trycatch.repository.experience.FeedbackDAO;
import com.app.trycatch.domain.experience.ExperienceProgramFileVO;
import com.app.trycatch.common.enumeration.file.FileContentType;
import com.app.trycatch.dto.file.FileDTO;
import com.app.trycatch.dto.member.AddressDTO;
import com.app.trycatch.domain.experience.AddressProgramVO;
import com.app.trycatch.repository.corporate.CorpWelfareRelDAO;
import com.app.trycatch.repository.file.CorpLogoFileDAO;
import com.app.trycatch.repository.file.FileDAO;
import com.app.trycatch.dto.qna.QnaDTO;
import com.app.trycatch.repository.member.AddressDAO;
import com.app.trycatch.repository.member.CorpMemberDAO;
import com.app.trycatch.repository.member.MemberDAO;
import com.app.trycatch.repository.qna.QnaDAO;
import com.app.trycatch.repository.skilllog.SkillLogDAO;
import com.app.trycatch.dto.skilllog.SkillLogDTO;
import com.app.trycatch.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CorporateService {

    private final CorpMemberDAO corpMemberDAO;
    private final MemberDAO memberDAO;
    private final AddressDAO addressDAO;
    private final AddressProgramDAO addressProgramDAO;
    private final ExperienceProgramDAO experienceProgramDAO;
    private final CorpInviteDAO corpInviteDAO;
    private final CorpTeamMemberDAO corpTeamMemberDAO;
    private final FileDAO fileDAO;
    private final CorpLogoFileDAO corpLogoFileDAO;
    private final ExperienceProgramFileDAO experienceProgramFileDAO;
    private final QnaDAO qnaDAO;
    private final CorpWelfareRelDAO corpWelfareRelDAO;
    private final ApplyDAO applyDAO;
    private final ChallengerDAO challengerDAO;
    private final FeedbackDAO feedbackDAO;
    private final SkillLogDAO skillLogDAO;
    private final MailService mailService;

    // ── 기업회원 여부 확인 ──────────────────────────────────────────────

    /** 해당 memberId가 tbl_corp에 존재하는지 확인 */
    public boolean isCorpMember(Long memberId) {
        return corpMemberDAO.findById(memberId).isPresent();
    }

    // ── 기업 정보 ──────────────────────────────────────────────────────

    /** 기업 + 회원 통합 정보 조회 */
    public CorpMemberDTO getCorpInfo(Long corpId) {
        return corpMemberDAO.findById(corpId)
                .orElseThrow(() -> new IllegalArgumentException("기업 정보를 찾을 수 없습니다. id=" + corpId));
    }

    /** 기업 정보(tbl_corp) + 주소(tbl_address) + 복리후생 수정 */
    public void updateCorpInfo(CorpMemberDTO dto) {
        corpMemberDAO.update(dto.toCorpVO());
        if (dto.getAddressId() != null) {
            // 기존 주소가 있으면 UPDATE
            addressDAO.update(dto.toAddressVO());
        } else if (dto.getAddressZipcode() != null && !dto.getAddressZipcode().isEmpty()) {
            // 주소가 없는데 새로 입력했으면 INSERT + member 업데이트
            dto.setAddressId(dto.getId());
            addressDAO.save(dto.toAddressVO());
            memberDAO.updateAddressIdById(dto.getId());
        }

        // 복리후생 저장 (delete-all + re-insert)
        corpWelfareRelDAO.deleteByCorpId(dto.getId());
        if (dto.getWelfareData() != null && !dto.getWelfareData().isEmpty()
                && !dto.getWelfareData().equals("{}")) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> welfareMap = objectMapper.readValue(
                        dto.getWelfareData(), new TypeReference<Map<String, String>>() {});

                List<CorpWelfareRelVO> welfareList = welfareMap.entrySet().stream()
                        .map(entry -> CorpWelfareRelVO.builder()
                                .corpId(dto.getId())
                                .welfareCode(entry.getKey())
                                .welfareName(entry.getValue())
                                .build())
                        .collect(Collectors.toList());

                corpWelfareRelDAO.saveAll(welfareList);
            } catch (Exception e) {
                log.error("복리후생 데이터 파싱 실패: {}", dto.getWelfareData(), e);
            }
        }
    }

    /** 회원 정보(tbl_member) 수정 — 비밀번호 빈값이면 UPDATE 제외 */
    public void updateMemberInfo(CorpMemberDTO dto) {
        memberDAO.update(dto.toMemberVO());
    }

    // ── 로고 업로드 ────────────────────────────────────────────────────

    /** 기업 로고 업로드 — 기존 로고가 있으면 교체 */
    public String uploadCorpLogo(Long corpId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // 기존 로고 삭제
        corpLogoFileDAO.findByCorpId(corpId).ifPresent(existing -> {
            corpLogoFileDAO.deleteByCorpId(corpId);
            fileDAO.delete(((Number) existing.get("id")).longValue());
        });

        // 파일 저장
        String todayPath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String rootPath = "C:/file/";
        String path = rootPath + todayPath;

        String uuid = UUID.randomUUID().toString();
        String fileName = uuid + "_" + file.getOriginalFilename();

        File directory = new File(path);
        if (!directory.exists()) directory.mkdirs();
        file.transferTo(new File(path, fileName));

        // tbl_file INSERT
        FileDTO fileDTO = new FileDTO();
        fileDTO.setFilePath(todayPath);
        fileDTO.setFileName(fileName);
        fileDTO.setFileOriginalName(file.getOriginalFilename());
        fileDTO.setFileSize(String.valueOf(file.getSize()));
        fileDTO.setFileContentType(FileContentType.IMAGE);
        fileDAO.save(fileDTO);

        // tbl_corp_logo_file INSERT
        corpLogoFileDAO.save(fileDTO.getId(), corpId);

        return "/api/files/display?filePath=" + todayPath + "&fileName=" + fileName;
    }

    /** 기업 로고 삭제 */
    public void deleteCorpLogo(Long corpId) {
        corpLogoFileDAO.findByCorpId(corpId).ifPresent(existing -> {
            corpLogoFileDAO.deleteByCorpId(corpId);
            fileDAO.delete(((Number) existing.get("id")).longValue());
        });
    }

    // ── 홈 대시보드 ────────────────────────────────────────────────────

    /** 상태별 프로그램 수 Map 반환 (draft/recruiting/closed/cancelled) */
    public Map<String, Long> getProgramStats(Long corpId) {
        return experienceProgramDAO.countByStatus(corpId);
    }

    /** 최신 프로그램 N개 */
    public List<ExperienceProgramDTO> getRecentPrograms(Long corpId, int limit) {
        return experienceProgramDAO.findLatestByCorpId(corpId, limit);
    }

    // ── 팀원 관리 ──────────────────────────────────────────────────────

    /** 팀원 목록 (5명씩 페이징) */
    public CorpTeamMemberWithPagingDTO getTeamMembers(Long corpId, int page) {
        final int rowCount = 5;
        int total = corpTeamMemberDAO.countByCorpId(corpId);

        Criteria criteria = new Criteria(page, total);
        recalcCriteria(criteria, rowCount);

        List<CorpTeamMemberDTO> list = corpTeamMemberDAO.findByCorpId(corpId, criteria);
        boolean hasMore = list.size() > rowCount;
        if (hasMore) list = list.subList(0, rowCount);

        return new CorpTeamMemberWithPagingDTO(list, criteria, hasMore);
    }

    /** 팀원 초대 — invite_code 생성 → tbl_corp_invite 저장 → 이메일 발송 */
    public void inviteTeamMember(Long corpId, String email) {
        String inviteCode = mailService.createCode();

        CorpInviteVO vo = CorpInviteVO.builder()
                .corpId(corpId)
                .inviteEmail(email)
                .inviteCode(inviteCode)
                .build();
        corpInviteDAO.save(vo);

        // 기업명 조회 후 이메일 발송
        String corpName = corpMemberDAO.findById(corpId)
                .map(CorpMemberDTO::getCorpCompanyName)
                .orElse("TRY-CATCH 기업");
        mailService.sendInviteMail(email, inviteCode, corpName);
    }

    /** 팀원 가입 — 회원 생성 + 기업 정보 복사 + 팀원 연결 + 초대 상태 변경 */
    public void joinTeamMember(String inviteCode, MemberDTO memberDTO) {
        CorpInviteVO invite = corpInviteDAO.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));

        // tbl_member INSERT (DTO — useGeneratedKeys로 id 세팅)
        memberDAO.saveTeamMember(memberDTO);

        // 초대한 기업의 corp 정보 조회 → 팀원용 tbl_corp INSERT
        CorpMemberDTO inviterCorp = corpMemberDAO.findById(invite.getCorpId())
                .orElseThrow(() -> new IllegalArgumentException("초대 기업 정보를 찾을 수 없습니다."));
        CorpVO teamMemberCorpVO = CorpVO.builder()
                .id(memberDTO.getId())
                .corpCompanyName(inviterCorp.getCorpCompanyName())
                .corpBusinessNumber(inviterCorp.getCorpBusinessNumber())
                .corpCeoName(inviterCorp.getCorpCeoName())
                .build();
        corpMemberDAO.save(teamMemberCorpVO);

        // tbl_corp_team_member INSERT (active 상태)
        CorpTeamMemberVO teamMemberVO = CorpTeamMemberVO.builder()
                .id(memberDTO.getId())
                .corpId(invite.getCorpId())
                .corpTeamMemberStatus("active")
                .build();
        corpTeamMemberDAO.save(teamMemberVO);

        // tbl_corp_invite 상태 변경
        corpInviteDAO.updateStatus(inviteCode, "accepted");
    }

    /** 팀원 제거 */
    public void removeTeamMember(Long memberId, Long corpId) {
        corpTeamMemberDAO.delete(memberId, corpId);
    }

    // ── 프로그램 등록 ──────────────────────────────────────────────────

    /** 새 프로그램 등록 */
    public Long createProgram(ExperienceProgramDTO dto, List<MultipartFile> files, AddressDTO addressDTO) {
        experienceProgramDAO.save(dto);
        Long programId = dto.getId();

        // 주소 저장
        if (addressDTO != null && addressDTO.getAddressAddress() != null && !addressDTO.getAddressAddress().isEmpty()) {
            addressDAO.saveForProgram(addressDTO);
            AddressProgramVO apVO = AddressProgramVO.builder()
                    .addressId(addressDTO.getId())
                    .experienceProgramId(programId)
                    .build();
            addressProgramDAO.save(apVO);
        }

        // 파일이 있으면 저장
        if (files != null && !files.isEmpty()) {
            String todayPath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String rootPath = "C:/file/";
            String path = rootPath + todayPath;

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                String uuid = UUID.randomUUID().toString();
                String fileName = uuid + "_" + file.getOriginalFilename();

                // tbl_file INSERT
                FileDTO fileDTO = new FileDTO();
                fileDTO.setFilePath(todayPath);
                fileDTO.setFileName(fileName);
                fileDTO.setFileOriginalName(file.getOriginalFilename());
                fileDTO.setFileSize(String.valueOf(file.getSize()));
                fileDTO.setFileContentType(
                        file.getContentType() != null && file.getContentType().contains("image")
                                ? FileContentType.IMAGE : FileContentType.OTHER);
                fileDAO.save(fileDTO);

                // tbl_experience_program_file INSERT
                ExperienceProgramFileVO epfVO = ExperienceProgramFileVO.builder()
                        .id(fileDTO.getId())
                        .experienceProgramId(programId)
                        .build();
                experienceProgramFileDAO.save(epfVO);

                // 디스크에 파일 저장
                File directory = new File(path);
                if (!directory.exists()) directory.mkdirs();
                try {
                    file.transferTo(new File(path, fileName));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return programId;
    }

    // ── 프로그램 수정 ──────────────────────────────────────────────────

    /** 프로그램 상세 조회 (수정 폼용) */
    public ExperienceProgramDTO getProgramDetail(Long programId) {
        ExperienceProgramDTO dto = experienceProgramDAO.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("프로그램을 찾을 수 없습니다. id=" + programId));
        dto.setExperienceProgramFiles(experienceProgramFileDAO.findAllByExperienceProgramId(programId));
        return dto;
    }

    /** 프로그램 주소 조회 */
    public AddressDTO getProgramAddress(Long programId) {
        return addressProgramDAO.findByExperienceProgramId(programId).orElse(null);
    }

    /** 프로그램 수정 */
    public void updateProgram(ExperienceProgramDTO dto, List<MultipartFile> files, String deleteFileIds, AddressDTO addressDTO) {
        // 프로그램 정보 수정
        experienceProgramDAO.setExperienceProgram(dto.toVO());

        // 주소 수정 (기존 삭제 후 새로 저장)
        if (addressDTO != null && addressDTO.getAddressAddress() != null && !addressDTO.getAddressAddress().isEmpty()) {
            // 기존 주소 삭제
            addressProgramDAO.findByExperienceProgramId(dto.getId()).ifPresent(existing -> {
                addressProgramDAO.deleteByExperienceProgramId(dto.getId());
                addressDAO.delete(existing.getId());
            });
            // 새 주소 저장
            addressDAO.saveForProgram(addressDTO);
            AddressProgramVO apVO = AddressProgramVO.builder()
                    .addressId(addressDTO.getId())
                    .experienceProgramId(dto.getId())
                    .build();
            addressProgramDAO.save(apVO);
        }

        // 기존 파일 삭제 처리
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            for (String fileIdStr : deleteFileIds.split(",")) {
                Long fileId = Long.parseLong(fileIdStr.trim());
                experienceProgramFileDAO.delete(fileId);
                fileDAO.delete(fileId);
            }
        }

        // 새 파일 저장
        if (files != null && !files.isEmpty()) {
            String todayPath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String rootPath = "C:/file/";
            String path = rootPath + todayPath;

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                String uuid = UUID.randomUUID().toString();
                String fileName = uuid + "_" + file.getOriginalFilename();

                // tbl_file INSERT
                FileDTO fileDTO = new FileDTO();
                fileDTO.setFilePath(todayPath);
                fileDTO.setFileName(fileName);
                fileDTO.setFileOriginalName(file.getOriginalFilename());
                fileDTO.setFileSize(String.valueOf(file.getSize()));
                fileDTO.setFileContentType(
                        file.getContentType() != null && file.getContentType().contains("image")
                                ? FileContentType.IMAGE : FileContentType.OTHER);
                fileDAO.save(fileDTO);

                // tbl_experience_program_file INSERT
                ExperienceProgramFileVO epfVO = ExperienceProgramFileVO.builder()
                        .id(fileDTO.getId())
                        .experienceProgramId(dto.getId())
                        .build();
                experienceProgramFileDAO.save(epfVO);

                // 디스크에 파일 저장
                File directory = new File(path);
                if (!directory.exists()) directory.mkdirs();
                try {
                    file.transferTo(new File(path, fileName));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // ── 프로그램 관리 ──────────────────────────────────────────────────

    /** 프로그램 목록 (페이징 + 상태 필터 + 키워드 검색) */
    public CorpProgramWithPagingDTO getPrograms(Long corpId, int page, int rowCount,
                                                       String status, String keyword, String sort,
                                                       Long selectedProgramId) {
        int total = experienceProgramDAO.countByCorpId(corpId, status, keyword, selectedProgramId);

        Criteria criteria = new Criteria(page, total);
        recalcCriteria(criteria, rowCount);

        List<ExperienceProgramDTO> list =
                experienceProgramDAO.findByCorpId(corpId, criteria, status, keyword, sort, selectedProgramId);
        boolean hasMore = list.size() > rowCount;
        if (hasMore) list = list.subList(0, rowCount);

        return new CorpProgramWithPagingDTO(list, criteria, hasMore);
    }

    // ── 참여자 관리 ──────────────────────────────────────────────────

    /** 참여자 목록 (페이징 + 상태 필터 + 상태별 카운트) */
    public ParticipantWithPagingDTO getParticipants(Long programId, Long corpId, String status, int page) {
        final int rowCount = 10;
        int total = challengerDAO.countByProgramId(programId, status);

        Criteria criteria = new Criteria(page, total);
        recalcCriteria(criteria, rowCount);

        List<ChallengerDTO> list = challengerDAO.findByProgramId(programId, status, criteria);
        boolean hasMore = list.size() > rowCount;
        if (hasMore) list = list.subList(0, rowCount);

        Map<String, Long> statusCounts = challengerDAO.countStatusByProgramId(programId);

        return new ParticipantWithPagingDTO(list, criteria, hasMore, statusCounts);
    }

    /** 참여자 상태 변경 (승급 = completed, 중도탈락 = out_of_process) */
    public void updateParticipantStatus(Long applyId, Long corpId, String newStatus) {
        challengerDAO.save(applyId);
        challengerDAO.setStatus(applyId, newStatus);
    }

    /** 참여자 탈락 처리 + 피드백 저장 */
    public void rejectParticipant(Long applyId, Long corpId, String feedback) {
        challengerDAO.save(applyId);
        challengerDAO.setStatus(applyId, "step_failed");

        Long challengerId = challengerDAO.findIdByApplyId(applyId);
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setId(challengerId);
        feedbackDTO.setFeedbackContent(feedback);
        feedbackDAO.save(feedbackDTO);
    }

    // ── 복리후생 조회 ──────────────────────────────────────────────────

    /** 기업별 복리후생 조회 */
    public List<CorpWelfareRelVO> getWelfareByCorpId(Long corpId) {
        return corpWelfareRelDAO.findByCorpId(corpId);
    }

    // ── 홈 하단: QNA ──────────────────────────────────────────────────

    /** 최신 QNA N개 */
    public List<QnaDTO> getRecentQnas(int limit) {
        return qnaDAO.findLatest(limit);
    }

    // ── 홈 하단: 기술 블로그 ────────────────────────────────────────────

    /** 최신 기술 블로그 N개 */
    public List<SkillLogDTO> getRecentSkillLogs(int limit) {
        return skillLogDAO.findLatest(limit);
    }

    // ── 지원자 관리 ──────────────────────────────────────────────────

    /** 지원자 목록 (페이징 + 상태 필터 + 키워드/학력/성별 검색 + 상태별 카운트) */
    public ApplicantWithPagingDTO getApplicants(Long programId, String status, String keyword,
                                                String education, String gender, int page) {
        final int rowCount = 10;
        int total = applyDAO.countByProgramId(programId, status, keyword, education, gender);

        Criteria criteria = new Criteria(page, total);
        recalcCriteria(criteria, rowCount);

        List<ApplyDTO> list = applyDAO.findByProgramId(programId, status, keyword, education, gender, criteria);
        boolean hasMore = list.size() > rowCount;
        if (hasMore) list = list.subList(0, rowCount);

        Map<String, Long> statusCounts = applyDAO.countStatusByProgramId(programId);

        return new ApplicantWithPagingDTO(list, criteria, hasMore, statusCounts);
    }

    /** 지원자 반려 처리 (체크된 ID 배열) */
    public void rejectApplicants(List<Long> applyIds) {
        applyIds.forEach(id -> applyDAO.setStatus(id, "document_fail"));
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────────

    /**
     * Criteria 생성자는 rowCount=20 기본값으로 pagination을 계산하므로,
     * 커스텀 rowCount 사용 시 startPage/endPage/realEnd를 재계산해야 한다.
     */
    private void recalcCriteria(Criteria c, int rowCount) {
        c.setRowCount(rowCount);
        c.setCount(rowCount + 1);
        c.setOffset((c.getPage() - 1) * rowCount);
        int realEnd = (int) Math.ceil(c.getTotal() / (double) rowCount);
        realEnd = Math.max(1, realEnd);
        int endPage = (int) (Math.ceil(c.getPage() / (double) c.getPageCount()) * c.getPageCount());
        int startPage = endPage - c.getPageCount() + 1;
        endPage = Math.min(endPage, realEnd);
        startPage = Math.max(1, startPage);
        c.setRealEnd(realEnd);
        c.setEndPage(endPage);
        c.setStartPage(startPage);
    }
}

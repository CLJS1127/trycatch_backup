package com.app.trycatch.controller.corporate;

import com.app.trycatch.common.enumeration.experience.ExperienceProgramStatus;
import com.app.trycatch.dto.experience.ExperienceProgramDTO;
import com.app.trycatch.dto.member.AddressDTO;
import com.app.trycatch.dto.member.CorpMemberDTO;
import com.app.trycatch.dto.member.IndividualMemberDTO;
import com.app.trycatch.dto.member.MemberDTO;
import com.app.trycatch.service.corporate.CorporateService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/corporate")
@RequiredArgsConstructor
@Slf4j
public class CorporateController {

    private final HttpSession session;
    private final CorporateService corporateService;

    private static final String LOGIN_REDIRECT = "redirect:/main/log-in";
    private static final String MAIN_REDIRECT = "redirect:/qna/list";

    /**
     * 세션에서 회원 ID 추출 (MemberDTO / IndividualMemberDTO / CorpMemberDTO 모두 지원)
     * 로그인하지 않았으면 null 반환
     */
    private Long getMemberId() {
        Object member = session.getAttribute("member");
        if (member instanceof MemberDTO dto) return dto.getId();
        if (member instanceof IndividualMemberDTO dto) return dto.getId();
        if (member instanceof CorpMemberDTO dto) return dto.getId();
        return null;
    }

    /** 로그인 여부 확인 — 비로그인이면 true */
    private boolean notLoggedIn() {
        return getMemberId() == null;
    }

    /** 로그인했지만 기업회원이 아니면 true */
    private boolean notCorpMember() {
        Long memberId = getMemberId();
        if (memberId == null) return true;
        return !corporateService.isCorpMember(memberId);
    }

    /** 세션의 회원이 팀원이면 true */
    private boolean isTeamMember() {
        Object member = session.getAttribute("member");
        if (member instanceof CorpMemberDTO dto) return dto.isTeamMember();
        return false;
    }

    /** 팀원이면 대표의 corp_id, 대표이면 자기 id 반환 */
    private Long getCorpId() {
        Object member = session.getAttribute("member");
        if (member instanceof CorpMemberDTO dto) return dto.getCorpId();
        return getMemberId();
    }

    // ── 홈 대시보드 ────────────────────────────────────────────────────

    @GetMapping("/home")
    public String home(Model model) {
        Long memberId = getMemberId();
        if (memberId != null) {
            // 로그인했는데 기업회원이 아니면 메인으로
            if (!corporateService.isCorpMember(memberId)) {
                return MAIN_REDIRECT;
            }
            Long corpId = getCorpId();
            model.addAttribute("corpInfo", corporateService.getCorpInfo(corpId));
            model.addAttribute("programStats", corporateService.getProgramStats(corpId));
            model.addAttribute("recentPrograms", corporateService.getRecentPrograms(corpId, 6));
        }
        model.addAttribute("recentQnas", corporateService.getRecentQnas(5));
        model.addAttribute("recentSkillLogs", corporateService.getRecentSkillLogs(5));
        model.addAttribute("loginMember", session.getAttribute("member"));
        return "corporate/home";
    }

    // ── 로고 업로드 ───────────────────────────────────────────────────

    @PostMapping("/logo")
    @ResponseBody
    public Map<String, Object> uploadLogo(@RequestParam("file") MultipartFile file) throws IOException {
        if (notCorpMember()) return Map.of("success", false, "message", "기업회원만 접근할 수 있습니다.");
        Long corpId = getCorpId();
        String logoUrl = corporateService.uploadCorpLogo(corpId, file);
        return Map.of("success", true, "logoUrl", logoUrl);
    }

    @DeleteMapping("/logo")
    @ResponseBody
    public Map<String, Object> deleteLogo() {
        if (notCorpMember()) return Map.of("success", false, "message", "기업회원만 접근할 수 있습니다.");
        Long corpId = getCorpId();
        corporateService.deleteCorpLogo(corpId);
        return Map.of("success", true);
    }

    // ── 기업정보관리 ───────────────────────────────────────────────────

    @GetMapping("/profile")
    public String profileForm(Model model) {
        if (notLoggedIn()) return LOGIN_REDIRECT;
        if (notCorpMember()) return MAIN_REDIRECT;
        if (isTeamMember()) return "redirect:/corporate/home";
        Long corpId = getMemberId();
        CorpMemberDTO corpInfo = corporateService.getCorpInfo(corpId);
        corpInfo.setWelfareList(corporateService.getWelfareByCorpId(corpId));
        model.addAttribute("corpInfo", corpInfo);
        model.addAttribute("loginMember", session.getAttribute("member"));
        return "corporate/profile";
    }

    @PostMapping("/profile")
    public String profileSave(CorpMemberDTO dto) {
        if (notCorpMember()) return MAIN_REDIRECT;
        if (isTeamMember()) return "redirect:/corporate/home";
        Long corpId = getMemberId();
        dto.setId(corpId);
        // addressId는 폼에 없으므로 기존 데이터에서 조회
        CorpMemberDTO existing = corporateService.getCorpInfo(corpId);
        dto.setAddressId(existing.getAddressId());
        corporateService.updateCorpInfo(dto);
        return "redirect:/corporate/profile";
    }

    // ── 회원정보관리 ───────────────────────────────────────────────────

    @GetMapping("/member-info")
    public String memberInfoForm(Model model) {
        if (notLoggedIn()) return LOGIN_REDIRECT;
        if (notCorpMember()) return MAIN_REDIRECT;
        Long corpId = getMemberId();
        model.addAttribute("corpInfo", corporateService.getCorpInfo(corpId));
        model.addAttribute("loginMember", session.getAttribute("member"));
        return "corporate/member-info";
    }

    @PostMapping("/member-info")
    public String memberInfoSave(CorpMemberDTO dto) {
        if (notCorpMember()) return MAIN_REDIRECT;
        dto.setId(getMemberId());
        corporateService.updateMemberInfo(dto);
        return "redirect:/corporate/member-info";
    }

    // ── 팀원관리 ───────────────────────────────────────────────────────

    @GetMapping("/team-member")
    public String teamMember(@RequestParam(defaultValue = "1") int page, Model model) {
        if (notLoggedIn()) return LOGIN_REDIRECT;
        if (notCorpMember()) return MAIN_REDIRECT;
        if (isTeamMember()) return "redirect:/corporate/home";
        Long corpId = getMemberId();
        model.addAttribute("teamWithPaging", corporateService.getTeamMembers(corpId, page));
        model.addAttribute("corpInfo", corporateService.getCorpInfo(corpId));
        model.addAttribute("loginMember", session.getAttribute("member"));
        return "corporate/team-member";
    }

    @PostMapping("/team-member/invite")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inviteMember(@RequestParam String invitation_mail) {
        if (notCorpMember()) return ResponseEntity.status(403).body(Map.of("success", false, "message", "권한이 없습니다."));
        if (isTeamMember()) return ResponseEntity.status(403).body(Map.of("success", false, "message", "팀원은 접근할 수 없습니다."));
        try {
            corporateService.inviteTeamMember(getMemberId(), invitation_mail);
            return ResponseEntity.ok(Map.of("success", true, "message", "팀원 초대가 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/team-member/remove")
    public String removeMember(@RequestParam Long memberId) {
        if (notCorpMember()) return MAIN_REDIRECT;
        if (isTeamMember()) return "redirect:/corporate/home";
        corporateService.removeTeamMember(memberId, getMemberId());
        return "redirect:/corporate/team-member";
    }

    // ── 프로그램 등록 ──────────────────────────────────────────────────

    @GetMapping("/program-apply")
    public String programApplyForm(Model model) {
        if (notLoggedIn()) return LOGIN_REDIRECT;
        if (notCorpMember()) return MAIN_REDIRECT;
        Long corpId = getCorpId();
        model.addAttribute("corpInfo", corporateService.getCorpInfo(corpId));
        model.addAttribute("loginMember", session.getAttribute("member"));
        return "corporate/program-apply";
    }

    @PostMapping("/program-apply")
    public String programApplySave(ExperienceProgramDTO dto, AddressDTO addressDTO,
                                   @RequestParam(value = "programFiles", required = false) List<MultipartFile> files) {
        if (notCorpMember()) return MAIN_REDIRECT;
        Long corpId = getCorpId();
        dto.setCorpId(corpId);
        dto.setExperienceProgramStatus(ExperienceProgramStatus.RECRUITING);
        log.info("프로그램 등록 DTO: {}", dto);
        Long createdProgramId = corporateService.createProgram(dto, files != null ? files : new ArrayList<>(), addressDTO);
        return "redirect:/corporate/program-management?selectedProgramId=" + createdProgramId;
    }

    // ── 프로그램 수정 ──────────────────────────────────────────────────

    @GetMapping("/program-update")
    public String programUpdateForm(@RequestParam Long id, Model model) {
        if (notLoggedIn()) return LOGIN_REDIRECT;
        if (notCorpMember()) return MAIN_REDIRECT;
        Long corpId = getCorpId();
        ExperienceProgramDTO program = corporateService.getProgramDetail(id);
        if (!program.getCorpId().equals(corpId)) return MAIN_REDIRECT;
        model.addAttribute("program", program);
        model.addAttribute("programAddress", corporateService.getProgramAddress(id));
        model.addAttribute("corpInfo", corporateService.getCorpInfo(corpId));
        model.addAttribute("loginMember", session.getAttribute("member"));
        return "corporate/program-update";
    }

    @PostMapping("/program-update")
    public String programUpdateSave(ExperienceProgramDTO dto, AddressDTO addressDTO,
                                    @RequestParam(value = "programFiles", required = false) List<MultipartFile> files,
                                    @RequestParam(value = "deleteFileIds", required = false) String deleteFileIds) {
        if (notCorpMember()) return MAIN_REDIRECT;
        Long corpId = getCorpId();
        dto.setCorpId(corpId);
        log.info("프로그램 수정 DTO: {}", dto);
        corporateService.updateProgram(dto, files != null ? files : new ArrayList<>(), deleteFileIds, addressDTO);
        return "redirect:/corporate/program-management";
    }

    // ── 프로그램관리 ───────────────────────────────────────────────────

    @GetMapping("/program-management")
    public String programManagement(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String SrchKeyword,
            @RequestParam(defaultValue = "10") int TopCount,
            @RequestParam(required = false) Long selectedProgramId,
            @RequestParam(defaultValue = "created_desc") String sort,
            Model model) {
        if (notLoggedIn()) return LOGIN_REDIRECT;
        if (notCorpMember()) return MAIN_REDIRECT;
        Long corpId = getCorpId();
        model.addAttribute("programWithPaging",
                corporateService.getPrograms(corpId, page, TopCount, status, SrchKeyword, sort, selectedProgramId));
        model.addAttribute("programStats", corporateService.getProgramStats(corpId));
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentKeyword", SrchKeyword);
        model.addAttribute("currentTopCount", TopCount);
        model.addAttribute("currentSort", sort);
        model.addAttribute("selectedProgramId", selectedProgramId);
        model.addAttribute("corpInfo", corporateService.getCorpInfo(corpId));
        model.addAttribute("loginMember", session.getAttribute("member"));
        return "corporate/program-management";
    }

    // ── 참여자관리 (tbl_challenger 구현 후 완성 예정) ───────────────────

    @GetMapping("/participant-list")
    public String participantList(
            @RequestParam(required = false) Long programId,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        if (notLoggedIn()) return LOGIN_REDIRECT;
        if (notCorpMember()) return MAIN_REDIRECT;
        if (programId == null) return "redirect:/corporate/program-management";
        Long corpId = getCorpId();
        model.addAttribute("participantWithPaging",
                corporateService.getParticipants(programId, corpId, status, page));
        model.addAttribute("programId", programId);
        model.addAttribute("currentStatus", status);
        model.addAttribute("corpInfo", corporateService.getCorpInfo(corpId));
        model.addAttribute("loginMember", session.getAttribute("member"));
        return "corporate/participant-list";
    }

    // ── 지원자관리 ───────────────────────────────────────────────────

    @GetMapping("/applicant-list")
    public String applicantList(
            @RequestParam(required = false) Long programId,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String education,
            @RequestParam(defaultValue = "") String gender,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        if (notLoggedIn()) return LOGIN_REDIRECT;
        if (notCorpMember()) return MAIN_REDIRECT;
        if (programId == null) return "redirect:/corporate/program-management";
        Long corpId = getCorpId();
        model.addAttribute("applicantWithPaging",
                corporateService.getApplicants(programId, status, keyword, education, gender, page));
        model.addAttribute("programId", programId);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("currentEducation", education);
        model.addAttribute("currentGender", gender);
        model.addAttribute("corpInfo", corporateService.getCorpInfo(corpId));
        model.addAttribute("loginMember", session.getAttribute("member"));
        return "corporate/applicant-list";
    }

    @PostMapping("/applicant/reject")
    @ResponseBody
    public Map<String, Object> rejectApplicants(@RequestBody List<Long> applyIds) {
        if (notCorpMember()) return Map.of("success", false, "message", "기업회원만 접근할 수 있습니다.");
        corporateService.rejectApplicants(applyIds);
        return Map.of("success", true);
    }

    @PostMapping("/participant/promote")
    @ResponseBody
    public Map<String, Object> promote(@RequestParam Long participantId) {
        if (notCorpMember()) return Map.of("success", false, "message", "기업회원만 접근할 수 있습니다.");
        corporateService.updateParticipantStatus(participantId, getMemberId(), "completed");
        return Map.of("success", true);
    }

    @PostMapping("/participant/reject")
    @ResponseBody
    public Map<String, Object> reject(@RequestParam Long participantId,
                                      @RequestParam String feedback) {
        if (notCorpMember()) return Map.of("success", false, "message", "기업회원만 접근할 수 있습니다.");
        corporateService.rejectParticipant(participantId, getMemberId(), feedback);
        return Map.of("success", true);
    }

    @PostMapping("/participant/withdraw")
    @ResponseBody
    public Map<String, Object> withdraw(@RequestParam Long participantId) {
        if (notCorpMember()) return Map.of("success", false, "message", "기업회원만 접근할 수 있습니다.");
        corporateService.updateParticipantStatus(participantId, getMemberId(), "out_of_process");
        return Map.of("success", true);
    }
}

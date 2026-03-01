package com.app.trycatch.controller.member;

import com.app.trycatch.dto.member.CorpMemberDTO;
import com.app.trycatch.dto.member.IndividualMemberDTO;
import com.app.trycatch.dto.member.MemberDTO;
import com.app.trycatch.dto.mypage.ExperienceProgramRankDTO;
import com.app.trycatch.dto.qna.QnaDTO;
import com.app.trycatch.dto.skilllog.SkillLogDTO;
import com.app.trycatch.service.member.CorpService;
import com.app.trycatch.service.member.IndividualMemberService;
import com.app.trycatch.service.main.MainHomeService;
import com.app.trycatch.service.oauth.KakaoService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/main")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    private final IndividualMemberService individualMemberService;
    private final CorpService corpService;
    private final KakaoService kakaoService;
    private final MainHomeService mainHomeService;
    private final HttpSession session;

    @GetMapping("individual-join")
    public String goIndividualJoinForm(){
        return "main/individual-join";
    }

    @GetMapping("company-join")
    public String goCompanyJoinForm(){
        return "main/company-join";
    }

    @PostMapping("individual-join")
    public RedirectView individualJoin(IndividualMemberDTO individualMemberDTO){
        log.info("========== individualJoin() 호출됨 ==========");
        individualMemberService.joinIndividual(individualMemberDTO);
        return new RedirectView("/main/log-in");
    }
    @PostMapping("company-join")
    public RedirectView companyJoin(CorpMemberDTO  corpMemberDTO){
        corpService.joinCorp(corpMemberDTO);
        return  new RedirectView("/main/log-in");
    }

    @GetMapping("kakao-join")
    public String kakaoCallback(@RequestParam("code") String code, Model model) {
        IndividualMemberDTO kakaoInfo = kakaoService.kakaoLogin(code);

        if (kakaoInfo.getId() != null) {
            // 기존 회원: DB에서 전체 프로필 조회 (레벨, 글 수, 프로필 이미지 등)
            IndividualMemberDTO fullMember = individualMemberService.findById(kakaoInfo.getId());
            // DB에 프로필 이미지가 없으면 카카오 프로필 이미지 사용
            if (fullMember.getMemberProfileImageUrl() == null && kakaoInfo.getMemberProfileImageUrl() != null) {
                fullMember.setMemberProfileImageUrl(kakaoInfo.getMemberProfileImageUrl());
            }
            session.setAttribute("member", fullMember);
            String reUrl = (String) session.getAttribute("re_url");
            session.removeAttribute("re_url");
            return "redirect:" + (reUrl != null ? reUrl : "/main/main");
        }

        model.addAttribute("memberEmail", kakaoInfo.getMemberEmail());
        model.addAttribute("memberName", kakaoInfo.getMemberName());
        return "main/kakao-join";
    }

    @PostMapping("kakao-join")
    public RedirectView kakaoJoin(IndividualMemberDTO individualMemberDTO) {
        log.info("받은 데이터: email={}, name={}, birth={}",
            individualMemberDTO.getMemberEmail(),
            individualMemberDTO.getMemberName(),
            individualMemberDTO.getIndividualMemberBirth());
        individualMemberService.kakaoJoin(individualMemberDTO);
        return new RedirectView("/main/log-in");
    }

    @GetMapping("log-out")
    public RedirectView logout() {
        session.invalidate();
        return new RedirectView("/main/log-in");
    }

    @GetMapping("service-introduce")
    public String goServiceIntroduce(Model model){
        List<ExperienceProgramRankDTO> programs = mainHomeService.getTopPrograms(10);

        model.addAttribute("programs", programs);

        Object member = session.getAttribute("member");
        if (member instanceof IndividualMemberDTO individualMember) {
            Set<Long> scrapProgramIds = mainHomeService.getActiveScrapProgramIds(individualMember);
            model.addAttribute("scrapProgramIds", scrapProgramIds);
        }

        return "main/service-introduce";
    }

    @GetMapping("main")
    public String goMainPage(Model model) {
        List<ExperienceProgramRankDTO> featuredPrograms = mainHomeService.getTopPrograms(6);
        List<QnaDTO> latestQnas = mainHomeService.getLatestQnas(6);
        List<SkillLogDTO> latestSkillLogs = mainHomeService.getLatestSkillLogs(6);

        model.addAttribute("featuredPrograms", featuredPrograms);
        model.addAttribute("latestQnas", latestQnas);
        model.addAttribute("latestSkillLogs", latestSkillLogs);
        model.addAttribute("loginMember", session.getAttribute("member"));

        Object member = session.getAttribute("member");
        if (member instanceof IndividualMemberDTO individualMember) {
            Set<Long> scrapProgramIds = mainHomeService.getActiveScrapProgramIds(individualMember);
            model.addAttribute("scrapProgramIds", scrapProgramIds);
        }

        return "main/main";
    }
//  로그인완료
    @GetMapping("log-in")
    public String goLoginForm(
            @CookieValue(name = "remember", required = false) boolean remember,
            @CookieValue(name = "remember-member-id", required = false) String rememberMemberId,
            @RequestParam(value = "re_url", required = false) String reUrl,
            Model model) {
        model.addAttribute("remember", remember);
        model.addAttribute("rememberMemberId", rememberMemberId);
        model.addAttribute("reUrl", reUrl);
        if (reUrl != null && !reUrl.isBlank()) {
            session.setAttribute("re_url", reUrl);
        }
        return "main/log-in";
    }

    @PostMapping("corp-log-in")
    public RedirectView corpLogin(MemberDTO memberDTO, @RequestParam(value = "re_url", defaultValue = "/corporate/home") String reUrl) {
        session.setAttribute("member", corpService.login(memberDTO));
        return new RedirectView(reUrl);
    }

    @PostMapping("log-in")
    public RedirectView login(MemberDTO memberDTO, @RequestParam(value = "re_url", defaultValue = "/main/main") String reUrl, HttpServletResponse response) {
        session.setAttribute("member", individualMemberService.login(memberDTO));

        Cookie rememberMemberIdCookie = new Cookie("remember-member-id", memberDTO.getMemberId());
        Cookie rememberCookie = new Cookie("remember", String.valueOf(memberDTO.isRemember()));

        rememberMemberIdCookie.setPath("/");
        rememberCookie.setPath("/");

        if (memberDTO.isRemember()) {
            rememberMemberIdCookie.setMaxAge(60 * 60 * 24 * 30);
            rememberCookie.setMaxAge(60 * 60 * 24 * 30);
        } else {
            rememberMemberIdCookie.setMaxAge(0);
            rememberCookie.setMaxAge(0);
        }

        response.addCookie(rememberMemberIdCookie);
        response.addCookie(rememberCookie);

        return new RedirectView(reUrl);
    }

    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(String memberEmail) {
        return individualMemberService.checkEmail(memberEmail);
    }

    @GetMapping("/check-id")
    @ResponseBody
    public boolean checkId(String memberId) {
        return individualMemberService.checkMemberId(memberId);
    }

    @GetMapping("/check-company-name")
    @ResponseBody
    public boolean checkCompanyName(String corpCompanyName) {
        return corpService.checkCompanyName(corpCompanyName);
    }

    @GetMapping("/check-business-number")
    @ResponseBody
    public boolean checkBusinessNumber(String corpBusinessNumber) {
        return corpService.checkBusinessNumber(corpBusinessNumber);
    }

//    기업 로그인에서 개인 회원 로그인 되는 것, 개인 로그인에서 기업 회원 로그인 되는 것 디버깅 완료.
}

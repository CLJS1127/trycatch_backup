package com.app.trycatch.controller.experience;

import com.app.trycatch.service.experience.ExperienceProgramService;
import com.app.trycatch.service.mypage.MyPageService;
import com.app.trycatch.dto.member.IndividualMemberDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/experience")
@RequiredArgsConstructor
public class ExperienceProgramController {
    private final ExperienceProgramService experienceProgramService;
    private final MyPageService myPageService;
    private final HttpSession session;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "all") String status,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "") String job,
                       @RequestParam(defaultValue = "latest") String sort,
                       Model model) {
        status = normalizeStatus(status);
        sort = normalizeSort(sort);
        keyword = normalizeText(keyword);
        job = normalizeText(job);

        model.addAttribute("programWithPaging", experienceProgramService.getList(page, status, keyword, job, sort));
        model.addAttribute("jobs", experienceProgramService.getDistinctJobs());
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("job", job);
        model.addAttribute("sort", sort);
        model.addAttribute("loginMember", session.getAttribute("member"));
        return "experience/list";
    }

    @GetMapping("/program/{id}")
    public String detailRedirect(@PathVariable Long id) {
        return "redirect:/experience/training-program/" + id;
    }

    @GetMapping("/training-program/{id}")
    public String detail(@PathVariable Long id) {
        return "experience/training-program";
    }

    @GetMapping("/training-program")
    public String detailWithQuery(@RequestParam(required = false) Long id) {
        if (id == null) {
            return "redirect:/experience/list";
        }
        return "redirect:/experience/training-program/" + id;
    }

    @GetMapping("/program/{id}/detail-data")
    @ResponseBody
    public Map<String, Object> detailData(@PathVariable Long id) {
        Object loginMember = session.getAttribute("member");
        var program = experienceProgramService.getDetail(id);

        if (loginMember instanceof IndividualMemberDTO individualMember) {
            myPageService.addLatestWatchPosting(individualMember.getId(), id);
        }

        boolean canApply = loginMember instanceof IndividualMemberDTO;
        boolean hasApplied = canApply
                && experienceProgramService.hasApplied(id, ((IndividualMemberDTO) loginMember).getId());

        Map<String, Object> response = new HashMap<>();
        response.put("program", program);
        response.put("isLoggedIn", loginMember != null);
        response.put("canApply", canApply);
        response.put("hasApplied", hasApplied);
        if (loginMember instanceof IndividualMemberDTO individualMember) {
            response.put("memberPhone", individualMember.getMemberPhone());
            response.put("memberEmail", individualMember.getMemberEmail());
        }
        return response;
    }

    @GetMapping("/detail")
    public String detailLegacy(@RequestParam Long id) {
        return "redirect:/experience/training-program/" + id;
    }

    private String normalizeStatus(String status) {
        List<String> allowed = Arrays.asList("all", "recruiting", "draft", "closed", "cancelled");
        if (status == null) {
            return "all";
        }
        String normalized = status.trim().toLowerCase();
        return allowed.contains(normalized) ? normalized : "all";
    }

    private String normalizeSort(String sort) {
        List<String> allowed = Arrays.asList("latest", "views", "deadline");
        if (sort == null) {
            return "latest";
        }
        String normalized = sort.trim().toLowerCase();
        return allowed.contains(normalized) ? normalized : "latest";
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }
}

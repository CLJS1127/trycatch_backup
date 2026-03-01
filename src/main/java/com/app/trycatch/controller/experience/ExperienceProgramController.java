package com.app.trycatch.controller.experience;

import com.app.trycatch.service.experience.ExperienceProgramService;
import com.app.trycatch.dto.member.IndividualMemberDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/experience")
@RequiredArgsConstructor
public class ExperienceProgramController {
    private final ExperienceProgramService experienceProgramService;
    private final HttpSession session;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "all") String status,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "") String job,
                       @RequestParam(defaultValue = "latest") String sort,
                       Model model) {
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
    public String detail(@PathVariable Long id, Model model) {
        Object loginMember = session.getAttribute("member");
        boolean canApply = loginMember instanceof IndividualMemberDTO;
        boolean hasApplied = canApply
                && experienceProgramService.hasApplied(id, ((IndividualMemberDTO) loginMember).getId());

        model.addAttribute("program", experienceProgramService.getDetail(id));
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("canApply", canApply);
        model.addAttribute("hasApplied", hasApplied);
        return "experience/training-program";
    }

    @GetMapping("/training-program/{id}")
    public String detailRedirect(@PathVariable Long id) {
        return "redirect:/experience/program/" + id;
    }

    @GetMapping("/detail")
    public String detailLegacy(@RequestParam Long id) {
        return "redirect:/experience/program/" + id;
    }
}

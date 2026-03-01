package com.app.trycatch.service.main;

import com.app.trycatch.common.enumeration.member.Status;
import com.app.trycatch.dto.experience.ExperienceProgramFileDTO;
import com.app.trycatch.dto.member.IndividualMemberDTO;
import com.app.trycatch.dto.mypage.ExperienceProgramRankDTO;
import com.app.trycatch.dto.mypage.ScrapPostingDTO;
import com.app.trycatch.dto.qna.QnaDTO;
import com.app.trycatch.dto.skilllog.SkillLogDTO;
import com.app.trycatch.repository.experience.ExperienceProgramFileDAO;
import com.app.trycatch.repository.mypage.ExperienceProgramRankDAO;
import com.app.trycatch.repository.mypage.ScrapPostingDAO;
import com.app.trycatch.repository.qna.QnaDAO;
import com.app.trycatch.repository.skilllog.SkillLogDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainHomeService {
    private final ExperienceProgramRankDAO experienceProgramRankDAO;
    private final ExperienceProgramFileDAO experienceProgramFileDAO;
    private final ScrapPostingDAO scrapPostingDAO;
    private final QnaDAO qnaDAO;
    private final SkillLogDAO skillLogDAO;

    public List<ExperienceProgramRankDTO> getTopPrograms(int limit) {
        List<ExperienceProgramRankDTO> programs = experienceProgramRankDAO.findTopByViewCount(limit);
        programs.forEach(program -> {
            List<ExperienceProgramFileDTO> files = experienceProgramFileDAO.findAllByExperienceProgramId(program.getExperienceProgramId());
            program.setExperienceProgramFiles(files);
        });
        return programs;
    }

    public List<QnaDTO> getLatestQnas(int limit) {
        return qnaDAO.findLatest(limit);
    }

    public List<SkillLogDTO> getLatestSkillLogs(int limit) {
        return skillLogDAO.findLatest(limit);
    }

    public Set<Long> getActiveScrapProgramIds(IndividualMemberDTO member) {
        if (member == null) {
            return Set.of();
        }

        List<ScrapPostingDTO> scraps = scrapPostingDAO.findAllByMemberId(member.getId());
        return scraps.stream()
                .filter(scrap -> scrap.getScrapStatus() == Status.ACTIVE)
                .map(ScrapPostingDTO::getExperienceProgramId)
                .collect(Collectors.toSet());
    }
}

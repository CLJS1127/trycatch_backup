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
        return getPopularPrograms(limit);
    }

    public List<ExperienceProgramRankDTO> getPopularPrograms(int limit) {
        List<ExperienceProgramRankDTO> programs = experienceProgramRankDAO.findTopByViewCount(limit);
        attachProgramFiles(programs);
        return programs;
    }

    public List<ExperienceProgramRankDTO> getLatestPrograms(int limit) {
        List<ExperienceProgramRankDTO> programs = experienceProgramRankDAO.findTopByUpdatedDatetime(limit);
        attachProgramFiles(programs);
        return programs;
    }

    public List<ExperienceProgramRankDTO> getDeadlineSoonPrograms(int limit) {
        List<ExperienceProgramRankDTO> programs = experienceProgramRankDAO.findTopByDeadlineSoon(limit);
        attachProgramFiles(programs);
        return programs;
    }

    public List<ExperienceProgramRankDTO> getHighRecruitmentPrograms(int limit) {
        List<ExperienceProgramRankDTO> programs = experienceProgramRankDAO.findTopByRecruitmentCount(limit);
        attachProgramFiles(programs);
        return programs;
    }

    public List<ExperienceProgramRankDTO> getLowApplyRatePrograms(int limit) {
        List<ExperienceProgramRankDTO> programs = experienceProgramRankDAO.findTopByApplyRateAsc(limit);
        attachProgramFiles(programs);
        return programs;
    }

    public List<ExperienceProgramRankDTO> getNewestPrograms(int limit) {
        List<ExperienceProgramRankDTO> programs = experienceProgramRankDAO.findTopByCreatedDatetime(limit);
        attachProgramFiles(programs);
        return programs;
    }

    public List<ExperienceProgramRankDTO> getRecentViewedPrograms(Long memberId, int limit) {
        if (memberId == null) {
            return List.of();
        }
        List<ExperienceProgramRankDTO> programs = experienceProgramRankDAO.findRecentViewedByMemberId(memberId, limit);
        attachProgramFiles(programs);
        return programs;
    }

    private void attachProgramFiles(List<ExperienceProgramRankDTO> programs) {
        programs.forEach(program -> {
            List<ExperienceProgramFileDTO> files =
                    experienceProgramFileDAO.findAllByExperienceProgramId(program.getExperienceProgramId());
            program.setExperienceProgramFiles(files);
        });
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

package com.app.trycatch.service.experience;

import com.app.trycatch.common.pagination.Criteria;
import com.app.trycatch.common.exception.ExperienceProgramNotFoundException;
import com.app.trycatch.dto.experience.ExperienceProgramDTO;
import com.app.trycatch.dto.experience.ExperienceProgramFileDTO;
import com.app.trycatch.dto.experience.ExperienceProgramListWithPagingDTO;
import com.app.trycatch.repository.experience.ApplyDAO;
import com.app.trycatch.repository.experience.ExperienceProgramDAO;
import com.app.trycatch.repository.experience.ExperienceProgramFileDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExperienceProgramService {
    private final ExperienceProgramDAO experienceProgramDAO;
    private final ExperienceProgramFileDAO experienceProgramFileDAO;
    private final ApplyDAO applyDAO;

    public ExperienceProgramListWithPagingDTO getList(int page, String status, String keyword, String job, String sort) {
        int total = experienceProgramDAO.countPublic(status, keyword, job);
        Criteria criteria = new Criteria(page, total);
        List<ExperienceProgramDTO> programs = experienceProgramDAO.findPublic(criteria, status, keyword, job, sort);

        criteria.setHasMore(programs.size() > criteria.getRowCount());
        if (criteria.isHasMore()) {
            programs.remove(programs.size() - 1);
        }

        programs.forEach(program -> {
            List<ExperienceProgramFileDTO> files = experienceProgramFileDAO.findAllByExperienceProgramId(program.getId());
            program.setExperienceProgramFiles(files);
        });

        ExperienceProgramListWithPagingDTO dto = new ExperienceProgramListWithPagingDTO();
        dto.setPrograms(programs);
        dto.setCriteria(criteria);
        dto.setTotal(total);
        return dto;
    }

    public ExperienceProgramDTO getDetail(Long id) {
        experienceProgramDAO.increaseViewCount(id);

        ExperienceProgramDTO program = experienceProgramDAO.findById(id)
                .orElseThrow(() -> new ExperienceProgramNotFoundException("체험 프로그램을 찾을 수 없습니다. id=" + id));

        List<ExperienceProgramFileDTO> files = experienceProgramFileDAO.findAllByExperienceProgramId(id);
        program.setExperienceProgramFiles(files);
        return program;
    }

    public boolean hasApplied(Long programId, Long memberId) {
        return applyDAO.existsByProgramIdAndMemberId(programId, memberId);
    }

    public List<String> getDistinctJobs() {
        return experienceProgramDAO.findDistinctJobs();
    }
}

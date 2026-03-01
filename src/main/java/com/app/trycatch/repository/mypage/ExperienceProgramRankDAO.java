package com.app.trycatch.repository.mypage;

import com.app.trycatch.dto.mypage.ExperienceProgramRankDTO;
import com.app.trycatch.mapper.mypage.ExperienceProgramRankMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExperienceProgramRankDAO {
    private final ExperienceProgramRankMapper experienceProgramRankMapper;

    public List<ExperienceProgramRankDTO> findTopByViewCount(int limit) {
        return experienceProgramRankMapper.selectTopByViewCount(limit);
    }

    public List<ExperienceProgramRankDTO> findTopPublicByViewCount(int limit) {
        return experienceProgramRankMapper.selectTopPublicByViewCount(limit);
    }

    public List<ExperienceProgramRankDTO> findTopPublicByLatest(int limit) {
        return experienceProgramRankMapper.selectTopPublicByLatest(limit);
    }

    public List<ExperienceProgramRankDTO> findTopByUpdatedDatetime(int limit) {
        return experienceProgramRankMapper.selectTopByUpdatedDatetime(limit);
    }

    public List<ExperienceProgramRankDTO> findTopByDeadlineSoon(int limit) {
        return experienceProgramRankMapper.selectTopByDeadlineSoon(limit);
    }

    public List<ExperienceProgramRankDTO> findTopByRecruitmentCount(int limit) {
        return experienceProgramRankMapper.selectTopByRecruitmentCount(limit);
    }

    public List<ExperienceProgramRankDTO> findTopByApplyRateAsc(int limit) {
        return experienceProgramRankMapper.selectTopByApplyRateAsc(limit);
    }

    public List<ExperienceProgramRankDTO> findTopByCreatedDatetime(int limit) {
        return experienceProgramRankMapper.selectTopByCreatedDatetime(limit);
    }

    public List<ExperienceProgramRankDTO> findRecentViewedByMemberId(Long memberId, int limit) {
        return experienceProgramRankMapper.selectRecentViewedByMemberId(memberId, limit);
    }
}

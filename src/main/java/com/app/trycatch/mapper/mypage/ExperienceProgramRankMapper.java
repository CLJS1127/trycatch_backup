package com.app.trycatch.mapper.mypage;

import com.app.trycatch.dto.mypage.ExperienceProgramRankDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExperienceProgramRankMapper {
    // 조회수 상위 N개 (원픽 공고, TOP100 공통)
    List<ExperienceProgramRankDTO> selectTopByViewCount(@Param("limit") int limit);
    // 공기업 타입 필터 + 조회수 상위 N개
    List<ExperienceProgramRankDTO> selectTopPublicByViewCount(@Param("limit") int limit);
    // 공기업 타입 필터 + 최신순 N개
    List<ExperienceProgramRankDTO> selectTopPublicByLatest(@Param("limit") int limit);
    // 메인 노출용 최신 업데이트순 N개
    List<ExperienceProgramRankDTO> selectTopByUpdatedDatetime(@Param("limit") int limit);
    // 마감 임박순 N개
    List<ExperienceProgramRankDTO> selectTopByDeadlineSoon(@Param("limit") int limit);
    // 모집인원 많은순 N개
    List<ExperienceProgramRankDTO> selectTopByRecruitmentCount(@Param("limit") int limit);
    // 지원자수/모집인원 낮은순 N개
    List<ExperienceProgramRankDTO> selectTopByApplyRateAsc(@Param("limit") int limit);
    // 최신 등록순 N개
    List<ExperienceProgramRankDTO> selectTopByCreatedDatetime(@Param("limit") int limit);
    // 최근 본 공고 N개
    List<ExperienceProgramRankDTO> selectRecentViewedByMemberId(@Param("memberId") Long memberId, @Param("limit") int limit);
}

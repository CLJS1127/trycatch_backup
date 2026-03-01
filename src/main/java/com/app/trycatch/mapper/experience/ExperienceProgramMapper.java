package com.app.trycatch.mapper.experience;

import com.app.trycatch.common.pagination.Criteria;
import com.app.trycatch.common.search.Search;
import com.app.trycatch.domain.experience.ExperienceProgramVO;
import com.app.trycatch.dto.experience.ExperienceProgramDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface ExperienceProgramMapper {
//    프로그램 등록
    public void insert(ExperienceProgramDTO experienceProgramDTO);

//    프로그램 수정
    public void update(ExperienceProgramVO experienceProgramVO);

//    skill-log 최근 공고
//    목록
    public List<ExperienceProgramDTO> selectAllByMemberIdOfChallenger(@Param("criteria") Criteria criteria,
                                                             @Param("search") Search search, @Param("id") Long id);
//    개수
    public int selectTotalByMemberIdOfChallenger(@Param("search") Search search, @Param("id") Long id);

//    조회
    public Optional<ExperienceProgramDTO> selectById(Long id);

    void increaseViewCount(@Param("id") Long id);

//    기업 대시보드: 상태별 프로그램 수
    Map<String, Long> countByStatus(@Param("corpId") Long corpId);

//    기업 대시보드: 최신 프로그램 N개
    List<ExperienceProgramDTO> selectLatestByCorpId(@Param("corpId") Long corpId, @Param("limit") int limit);

//    기업 프로그램 관리: 전체 개수 (필터)
    int countByCorpId(@Param("corpId") Long corpId, @Param("status") String status, @Param("keyword") String keyword);

//    기업 프로그램 관리: 목록 (페이징 + 필터)
    List<ExperienceProgramDTO> selectByCorpId(@Param("corpId") Long corpId, @Param("criteria") Criteria criteria,
                                              @Param("status") String status, @Param("keyword") String keyword,
                                              @Param("sort") String sort);

//    체험 프로그램 목록(공개): 전체 개수 (필터)
    int countPublic(@Param("status") String status, @Param("keyword") String keyword, @Param("job") String job);

//    체험 프로그램 목록(공개): 목록 (페이징 + 필터)
    List<ExperienceProgramDTO> selectPublic(@Param("criteria") Criteria criteria, @Param("status") String status,
                                            @Param("keyword") String keyword, @Param("job") String job,
                                            @Param("sort") String sort);

//    체험 프로그램 목록(공개): 직무 필터 옵션
    List<String> selectDistinctJobs();

//    마감일 조회 (스케줄러용)
    List<ExperienceProgramDTO> selectByDeadline(@Param("deadline") String deadline);

//    마감일 지난 모집중 공고 일괄 마감 처리
    int closeExpiredPrograms(@Param("today") String today);

//    체험 시작일 도달: 모집마감 → 진행중
    int startPrograms(@Param("today") String today);

//    체험 종료일 다음날: 진행중 → 진행종료
    int finishPrograms(@Param("today") String today);
}

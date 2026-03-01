package com.app.trycatch.dto.mypage;

import com.app.trycatch.dto.experience.ExperienceProgramFileDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ExperienceProgramRankDTO {
    private Long experienceProgramId;
    private String experienceProgramTitle;
    private String experienceProgramJob;
    private String experienceProgramDeadline;
    private int experienceProgramViewCount;
    private int experienceProgramRecruitmentCount;
    private int applicantCount;
    private Long corpId;
    private String corpCompanyName;
    private String corpLogoFilePath;
    private String corpLogoFileName;
    private List<ExperienceProgramFileDTO> experienceProgramFiles;
}

package com.example.jujuassembly.domain.report.dto;

import com.example.jujuassembly.domain.report.entity.StatusEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportStatusRequestDto {


    private StatusEnum status;


}

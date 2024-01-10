package com.example.jujuassembly.domain.report.dto;

import com.example.jujuassembly.domain.report.entity.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReportStatusRequestDto {


    private StatusEnum status;


}

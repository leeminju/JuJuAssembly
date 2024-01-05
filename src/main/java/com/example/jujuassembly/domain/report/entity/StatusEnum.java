package com.example.jujuassembly.domain.report.entity;

public enum StatusEnum {
    //진행중, 채택, 비채택
    PROCEEDING("Proceeding"),
    ADOPTED("Adopted"),
    UN_ADOPTED("UnAdopted");

    private final String status;

    StatusEnum(String status) {
        this.status = status;
    }

}
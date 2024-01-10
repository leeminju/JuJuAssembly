package com.example.jujuassembly.domain.report.entity;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.domain.report.dto.ReportRequestDto;
import com.example.jujuassembly.global.entity.Timestamped;
import com.example.jujuassembly.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "reports")
public class Report extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String image;

    @Column
    @Enumerated
    private StatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public Report(ReportRequestDto requestDto) {
        this.name = requestDto.getName();
        this.status = StatusEnum.PROCEEDING;
    }

    public void updateUser(User user) {
        this.user = user;
    }

    public void updateCategory(Category category) {
        this.category = category;
    }

    public void updateImage(String image) {
        this.image = image;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateStatus(StatusEnum status) {
        this.status = status;
    }
}


package com.example.jujuassembly.domain.user.entity;

import com.example.jujuassembly.domain.category.entity.Category;
import com.example.jujuassembly.global.entity.Timestamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id")
    private String loginId;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    private String email;

    @Column(name = "is_archived")
    private Boolean isArchived;

    @Column(nullable = false, name = "role")
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;

    @OneToOne
    @JoinColumn(name = "first_preferred_category_id")
    private Category firstPreferredCategory;

    @OneToOne
    @JoinColumn(name = "second_preferred_category_id")
    private Category secondPreferredCategory;


}

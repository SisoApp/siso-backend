package com.siso.user.domain.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DrinkingCapacity drinking_capacity;

    @Enumerated(EnumType.STRING)
    private Religion religion;

    @Column(name = "is_smoke")
    private boolean smoke; // true흡연함, false = 흡연 안함

    @Column(nullable = false)
    private int age;

    @Column(length = 50, nullable = false)
    private String nickname;

    @Column(length = 255, nullable = true)
    private String introduce;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact")
    private PreferenceContact preferenceContact;

    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id")
    private List<UserProfileImage> profileImages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Location Location;

    @Enumerated(EnumType.STRING)
    private Sex Sex;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}

package com.siso.user.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "drinking_capacity")
    private DrinkingCapacity drinkingCapacity;

    @Enumerated(EnumType.STRING)
    private Religion religion;

    @Column(name = "is_smoke")
    private boolean smoke;

    @Column(nullable = false)
    private int age;

    @Column(length = 50, nullable = false)
    private String nickname;

    @Column(length = 255)
    private String introduce;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact")
    private PreferenceContact preferenceContact;

    @Enumerated(EnumType.STRING)
    private Location location;

    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Enumerated(EnumType.STRING)
    private PreferenceSex preferenceSex;


    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void updateProfile(DrinkingCapacity drinkingCapacity, Religion religion, boolean smoke, String nickname, String introduce, PreferenceContact preferenceContact, Location location) {
        this.drinkingCapacity = drinkingCapacity;
        this.religion = religion;
        this.smoke = smoke;
        this.nickname = nickname;
        this.introduce = introduce;
        this.preferenceContact = preferenceContact;
        this.location = location;
    }
}

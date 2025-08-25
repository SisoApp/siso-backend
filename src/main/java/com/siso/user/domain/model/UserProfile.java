package com.siso.user.domain.model;

import com.siso.image.domain.model.Image;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "drinking_capacity")
    private DrinkingCapacity drinkingCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest")
    private Religion religion;

    @Column(name = "is_smoke")
    private boolean smoke;

    @Column(name = "age", nullable = false)
    private int age;

    @Column(name = "nickname", length = 50, nullable = false)
    private String nickname;

    @Column(name = "introduce", length = 255)
    private String introduce;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact")
    private PreferenceContact preferenceContact;

    @Enumerated(EnumType.STRING)
    @Column(name = "location")
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex")
    private Sex sex;
  
    @Enumerated(EnumType.STRING)
    private PreferenceSex preferenceSex;

    @OneToOne
    @JoinColumn(name = "profile_image_id")
    private Image profileImage;

    // 양방향 연관 관계 설정
    public void linkUser(User user) {
        this.user = user;
        user.linkProfile(this);
    }

    @Builder
    public UserProfile(User user, DrinkingCapacity drinkingCapacity, Religion religion, boolean smoke, String nickname, int age, String introduce, PreferenceContact preferenceContact, Location location, Sex sex, Image profileImage) {
        this.user = user;
        user.linkProfile(this);
        this.drinkingCapacity = drinkingCapacity;
        this.religion = religion;
        this.smoke = smoke;
        this.age = age;
        this.nickname = nickname;
        this.introduce = introduce;
        this.preferenceContact = preferenceContact;
        this.location = location;
        this.sex = sex;
        this.profileImage = profileImage;
    }

    public void updateProfile(DrinkingCapacity drinkingCapacity, Religion religion, boolean smoke, String nickname, String introduce, PreferenceContact preferenceContact, Location location) {
        this.drinkingCapacity = drinkingCapacity;
        this.religion = religion;
        this.smoke = smoke;
        this.nickname = nickname;
        this.introduce = introduce;
        this.preferenceContact = preferenceContact;
        this.location = location;
    }

    public void setProfileImage(Image profileImage) {
        this.profileImage = profileImage;
    }
}

package com.siso.user.domain.model;

import com.siso.image.domain.model.Image;
import com.siso.user.dto.request.UserProfileRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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
    @Column(name = "drinking_capacity", nullable = true)
    private DrinkingCapacity drinkingCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "religion", nullable = true)
    private Religion religion;

    @Column(name = "is_smoke", nullable = true)
    private boolean smoke;

    @Column(name = "age", nullable = false)
    private int age;

    @Column(name = "nickname", length = 50, nullable = false)
    private String nickname;

    @Column(name = "introduce", length = 255)
    private String introduce;

    @Column(name = "location", nullable = true)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex", nullable = true)
    private Sex sex;

    @Enumerated(EnumType.STRING)
    @Column(name = "preference_sex", nullable = true)
    private PreferenceSex preferenceSex;

    @Enumerated(EnumType.STRING)
    @Column(name = "mbti", nullable = true)
    private Mbti mbti;

    //이런 인연을 만나고 싶어요 파트
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "profile_meetings", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "meeting")
    private List<Meeting> meetings;

    @Builder
    public UserProfile(User user, DrinkingCapacity drinkingCapacity, Religion religion, boolean smoke, String nickname, int age, String introduce,
                       String location, Sex sex, Mbti mbti, PreferenceSex preferenceSex, List<Meeting> meetings) {
        this.user = user;
        // 양방향 연관 관계 설정
        user.linkProfile(this);
        this.drinkingCapacity = drinkingCapacity;
        this.religion = religion;
        this.smoke = smoke;
        this.age = age;
        this.nickname = nickname;
        this.introduce = introduce;
        this.location = location;
        this.sex = sex;
        this.mbti = mbti;
        this.preferenceSex = preferenceSex;
        this.meetings = meetings;
    }

    public void updateProfile(UserProfileRequestDto dto) {
        this.drinkingCapacity = dto.getDrinkingCapacity();
        this.religion = dto.getReligion();
        this.smoke = dto.isSmoke();
        this.age = dto.getAge();
        this.nickname = dto.getNickname();
        this.introduce = dto.getIntroduce();
        this.location = dto.getLocation();
        this.mbti = dto.getMbti();
        this.preferenceSex = dto.getPreferenceSex();
        this.meetings = dto.getMeetings();
    }
}
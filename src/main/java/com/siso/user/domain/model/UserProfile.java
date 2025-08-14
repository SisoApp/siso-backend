package com.siso.user.domain.model;

import com.siso.image.domain.model.Image;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
    private DrinkingCapacity drinking_capacity;

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

    @Builder.Default
    @OneToMany(cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id")
    private List<Image> profileImages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Location location;

    @Enumerated(EnumType.STRING)
    private Sex sex;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void addImage(Image image) {
        if (image != null) this.profileImages.add(image);
    }

    public void replaceImages(List<Image> images) {
        this.profileImages.clear();
        if (images != null) this.profileImages.addAll(images);
    }
}

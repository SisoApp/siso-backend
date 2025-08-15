package com.siso.user.domain.model;

import com.siso.common.domain.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_online", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isOnline = false;

    @Column(name = "is_block", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isBlock = false;

    @Column(name = "is_deleted", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isDeleted = false;
}

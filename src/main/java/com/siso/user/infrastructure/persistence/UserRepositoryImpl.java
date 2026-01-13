package com.siso.user.infrastructure.persistence;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.siso.user.domain.model.PresenceStatus;
import com.siso.user.domain.model.Sex;
import com.siso.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.siso.user.domain.model.QUser.user;
import static com.siso.user.domain.model.QUserInterest.userInterest;
import static com.siso.user.domain.model.QUserProfile.userProfile;
import static com.siso.image.domain.model.QImage.image;
import static com.siso.voicesample.domain.model.QVoiceSample.voiceSample;

/**
 * User Custom Repository 구현체
 * - QueryDSL을 사용하여 N+1 쿼리 문제 해결
 * - Fetch Join으로 연관 엔티티 한 번에 조회
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<User> findByIdsWithAllRelations(List<Long> ids) {
        return queryFactory
                .selectFrom(user).distinct()
                .leftJoin(user.images, image).fetchJoin()
                .leftJoin(user.userProfile, userProfile).fetchJoin()
                .leftJoin(user.voiceSample, voiceSample).fetchJoin()
                .where(
                        user.id.in(ids),
                        user.isDeleted.eq(false),
                        user.isBlock.eq(false)
                )
                .fetch();
    }

    @Override
    public List<User> findUsersWithDynamicFilters(String gender, Integer minAge, Integer maxAge,
                                                   PresenceStatus presenceStatus) {
        return queryFactory
                .selectFrom(user).distinct()
                .leftJoin(user.images, image).fetchJoin()
                .leftJoin(user.userProfile, userProfile).fetchJoin()
                .where(
                        eqGender(gender),
                        betweenAge(minAge, maxAge),
                        eqPresenceStatus(presenceStatus),
                        user.isDeleted.eq(false),
                        user.isBlock.eq(false)
                )
                .fetch();
    }

    @Override
    public List<User> findMatchingCandidatesWithRelations(Long excludeUserId, List<String> preferredGenders,
                                                           Integer minAge, Integer maxAge) {
        return queryFactory
                .selectFrom(user).distinct()
                .leftJoin(user.images, image).fetchJoin()
                .leftJoin(user.userProfile, userProfile).fetchJoin()
                .leftJoin(user.userInterests, userInterest).fetchJoin()
                .where(
                        user.id.ne(excludeUserId),
                        inGenders(preferredGenders),
                        betweenAge(minAge, maxAge),
                        eqPresenceStatus(PresenceStatus.ONLINE),
                        user.isDeleted.eq(false),
                        user.isBlock.eq(false)
                )
                .fetch();
    }

    // ============ 동적 쿼리 헬퍼 메서드 ============

    private BooleanExpression eqGender(String gender) {
        return gender != null ? userProfile.sex.eq(Sex.valueOf(gender)) : null;
    }

    private BooleanExpression betweenAge(Integer minAge, Integer maxAge) {
        if (minAge != null && maxAge != null) {
            return userProfile.age.between(minAge, maxAge);
        } else if (minAge != null) {
            return userProfile.age.goe(minAge);
        } else if (maxAge != null) {
            return userProfile.age.loe(maxAge);
        }
        return null;
    }

    private BooleanExpression eqPresenceStatus(PresenceStatus presenceStatus) {
        return presenceStatus != null ? user.presenceStatus.eq(presenceStatus) : null;
    }

    private BooleanExpression inGenders(List<String> genders) {
        if (genders == null || genders.isEmpty()) {
            return null;
        }

        List<Sex> sexEnums = genders.stream()
                .map(Sex::valueOf)
                .toList();

        return userProfile.sex.in(sexEnums);
    }
}

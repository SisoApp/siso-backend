package com.siso.user.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserProfile is a Querydsl query type for UserProfile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserProfile extends EntityPathBase<UserProfile> {

    private static final long serialVersionUID = -879222813L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserProfile userProfile = new QUserProfile("userProfile");

    public final NumberPath<Integer> age = createNumber("age", Integer.class);

    public final EnumPath<DrinkingCapacity> drinkingCapacity = createEnum("drinkingCapacity", DrinkingCapacity.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath introduce = createString("introduce");

    public final StringPath location = createString("location");

    public final EnumPath<Mbti> mbti = createEnum("mbti", Mbti.class);

    public final ListPath<Meeting, EnumPath<Meeting>> meetings = this.<Meeting, EnumPath<Meeting>>createList("meetings", Meeting.class, EnumPath.class, PathInits.DIRECT2);

    public final StringPath nickname = createString("nickname");

    public final EnumPath<PreferenceSex> preferenceSex = createEnum("preferenceSex", PreferenceSex.class);

    public final EnumPath<Religion> religion = createEnum("religion", Religion.class);

    public final EnumPath<Sex> sex = createEnum("sex", Sex.class);

    public final BooleanPath smoke = createBoolean("smoke");

    public final QUser user;

    public QUserProfile(String variable) {
        this(UserProfile.class, forVariable(variable), INITS);
    }

    public QUserProfile(Path<? extends UserProfile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserProfile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserProfile(PathMetadata metadata, PathInits inits) {
        this(UserProfile.class, metadata, inits);
    }

    public QUserProfile(Class<? extends UserProfile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}


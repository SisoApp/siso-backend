package com.siso.common.firebase.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFcmToken is a Querydsl query type for FcmToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFcmToken extends EntityPathBase<FcmToken> {

    private static final long serialVersionUID = 469416133L;

    public static final QFcmToken fcmToken = new QFcmToken("fcmToken");

    public final com.siso.common.domain.QBaseTime _super = new com.siso.common.domain.QBaseTime(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath token = createString("token");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QFcmToken(String variable) {
        super(FcmToken.class, forVariable(variable));
    }

    public QFcmToken(Path<? extends FcmToken> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFcmToken(PathMetadata metadata) {
        super(FcmToken.class, metadata);
    }

}


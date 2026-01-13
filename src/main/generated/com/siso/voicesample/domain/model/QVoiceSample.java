package com.siso.voicesample.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QVoiceSample is a Querydsl query type for VoiceSample
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVoiceSample extends EntityPathBase<VoiceSample> {

    private static final long serialVersionUID = 783198414L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QVoiceSample voiceSample = new QVoiceSample("voiceSample");

    public final com.siso.common.domain.QBaseTime _super = new com.siso.common.domain.QBaseTime(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> duration = createNumber("duration", Integer.class);

    public final NumberPath<Integer> fileSize = createNumber("fileSize", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath presignedUrl = createString("presignedUrl");

    public final DateTimePath<java.time.LocalDateTime> presignedUrlExpiresAt = createDateTime("presignedUrlExpiresAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath url = createString("url");

    public final com.siso.user.domain.model.QUser user;

    public QVoiceSample(String variable) {
        this(VoiceSample.class, forVariable(variable), INITS);
    }

    public QVoiceSample(Path<? extends VoiceSample> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QVoiceSample(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QVoiceSample(PathMetadata metadata, PathInits inits) {
        this(VoiceSample.class, metadata, inits);
    }

    public QVoiceSample(Class<? extends VoiceSample> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.siso.user.domain.model.QUser(forProperty("user"), inits.get("user")) : null;
    }

}


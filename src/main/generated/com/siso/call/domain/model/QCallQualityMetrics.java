package com.siso.call.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCallQualityMetrics is a Querydsl query type for CallQualityMetrics
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCallQualityMetrics extends EntityPathBase<CallQualityMetrics> {

    private static final long serialVersionUID = -666914000L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCallQualityMetrics callQualityMetrics = new QCallQualityMetrics("callQualityMetrics");

    public final com.siso.common.domain.QBaseTime _super = new com.siso.common.domain.QBaseTime(this);

    public final NumberPath<Integer> audioBitrate = createNumber("audioBitrate", Integer.class);

    public final StringPath audioCodec = createString("audioCodec");

    public final QCall call;

    public final StringPath clientType = createString("clientType");

    public final EnumPath<CallQualityMetrics.ConnectionQuality> connectionQuality = createEnum("connectionQuality", CallQualityMetrics.ConnectionQuality.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> jitter = createNumber("jitter", Integer.class);

    public final StringPath networkType = createString("networkType");

    public final NumberPath<Integer> packetLossRate = createNumber("packetLossRate", Integer.class);

    public final NumberPath<Integer> roundTripTime = createNumber("roundTripTime", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> videoBitrate = createNumber("videoBitrate", Integer.class);

    public final StringPath videoCodec = createString("videoCodec");

    public QCallQualityMetrics(String variable) {
        this(CallQualityMetrics.class, forVariable(variable), INITS);
    }

    public QCallQualityMetrics(Path<? extends CallQualityMetrics> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCallQualityMetrics(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCallQualityMetrics(PathMetadata metadata, PathInits inits) {
        this(CallQualityMetrics.class, metadata, inits);
    }

    public QCallQualityMetrics(Class<? extends CallQualityMetrics> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.call = inits.isInitialized("call") ? new QCall(forProperty("call"), inits.get("call")) : null;
    }

}


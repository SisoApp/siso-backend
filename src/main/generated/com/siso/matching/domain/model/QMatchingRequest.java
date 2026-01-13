package com.siso.matching.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMatchingRequest is a Querydsl query type for MatchingRequest
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMatchingRequest extends EntityPathBase<MatchingRequest> {

    private static final long serialVersionUID = 16545381L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMatchingRequest matchingRequest = new QMatchingRequest("matchingRequest");

    public final com.siso.common.domain.QBaseTime _super = new com.siso.common.domain.QBaseTime(this);

    public final NumberPath<Integer> candidatesCount = createNumber("candidatesCount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> matchedCount = createNumber("matchedCount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> processedAt = createDateTime("processedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> processingTimeMs = createNumber("processingTimeMs", Integer.class);

    public final StringPath requestId = createString("requestId");

    public final EnumPath<MatchingStatus> status = createEnum("status", MatchingStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.siso.user.domain.model.QUser user;

    public QMatchingRequest(String variable) {
        this(MatchingRequest.class, forVariable(variable), INITS);
    }

    public QMatchingRequest(Path<? extends MatchingRequest> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMatchingRequest(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMatchingRequest(PathMetadata metadata, PathInits inits) {
        this(MatchingRequest.class, metadata, inits);
    }

    public QMatchingRequest(Class<? extends MatchingRequest> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.siso.user.domain.model.QUser(forProperty("user"), inits.get("user")) : null;
    }

}


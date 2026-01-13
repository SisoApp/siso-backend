package com.siso.call.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCall is a Querydsl query type for Call
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCall extends EntityPathBase<Call> {

    private static final long serialVersionUID = -633155252L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCall call = new QCall("call");

    public final StringPath agoraChannelName = createString("agoraChannelName");

    public final StringPath agoraToken = createString("agoraToken");

    public final com.siso.user.domain.model.QUser caller;

    public final ListPath<com.siso.callreview.domain.model.CallReview, com.siso.callreview.domain.model.QCallReview> callReviews = this.<com.siso.callreview.domain.model.CallReview, com.siso.callreview.domain.model.QCallReview>createList("callReviews", com.siso.callreview.domain.model.CallReview.class, com.siso.callreview.domain.model.QCallReview.class, PathInits.DIRECT2);

    public final EnumPath<CallStatus> callStatus = createEnum("callStatus", CallStatus.class);

    public final NumberPath<Long> duration = createNumber("duration", Long.class);

    public final DateTimePath<java.time.LocalDateTime> endTime = createDateTime("endTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.siso.user.domain.model.QUser receiver;

    public final DateTimePath<java.time.LocalDateTime> startTime = createDateTime("startTime", java.time.LocalDateTime.class);

    public QCall(String variable) {
        this(Call.class, forVariable(variable), INITS);
    }

    public QCall(Path<? extends Call> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCall(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCall(PathMetadata metadata, PathInits inits) {
        this(Call.class, metadata, inits);
    }

    public QCall(Class<? extends Call> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.caller = inits.isInitialized("caller") ? new com.siso.user.domain.model.QUser(forProperty("caller"), inits.get("caller")) : null;
        this.receiver = inits.isInitialized("receiver") ? new com.siso.user.domain.model.QUser(forProperty("receiver"), inits.get("receiver")) : null;
    }

}


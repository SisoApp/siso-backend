package com.siso.callreview.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCallReview is a Querydsl query type for CallReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCallReview extends EntityPathBase<CallReview> {

    private static final long serialVersionUID = 1701066076L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCallReview callReview = new QCallReview("callReview");

    public final com.siso.common.domain.QBaseTime _super = new com.siso.common.domain.QBaseTime(this);

    public final com.siso.call.domain.model.QCall call;

    public final StringPath comment = createString("comment");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCallReview(String variable) {
        this(CallReview.class, forVariable(variable), INITS);
    }

    public QCallReview(Path<? extends CallReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCallReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCallReview(PathMetadata metadata, PathInits inits) {
        this(CallReview.class, metadata, inits);
    }

    public QCallReview(Class<? extends CallReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.call = inits.isInitialized("call") ? new com.siso.call.domain.model.QCall(forProperty("call"), inits.get("call")) : null;
    }

}


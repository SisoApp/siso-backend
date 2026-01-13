package com.siso.chat.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatRoomLimit is a Querydsl query type for ChatRoomLimit
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatRoomLimit extends EntityPathBase<ChatRoomLimit> {

    private static final long serialVersionUID = 2030938816L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatRoomLimit chatRoomLimit = new QChatRoomLimit("chatRoomLimit");

    public final QChatRoomMember chatRoomMember;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> sentCount = createNumber("sentCount", Integer.class);

    public final com.siso.user.domain.model.QUser user;

    public QChatRoomLimit(String variable) {
        this(ChatRoomLimit.class, forVariable(variable), INITS);
    }

    public QChatRoomLimit(Path<? extends ChatRoomLimit> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatRoomLimit(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatRoomLimit(PathMetadata metadata, PathInits inits) {
        this(ChatRoomLimit.class, metadata, inits);
    }

    public QChatRoomLimit(Class<? extends ChatRoomLimit> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatRoomMember = inits.isInitialized("chatRoomMember") ? new QChatRoomMember(forProperty("chatRoomMember"), inits.get("chatRoomMember")) : null;
        this.user = inits.isInitialized("user") ? new com.siso.user.domain.model.QUser(forProperty("user"), inits.get("user")) : null;
    }

}


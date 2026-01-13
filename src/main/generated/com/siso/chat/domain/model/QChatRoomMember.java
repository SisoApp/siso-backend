package com.siso.chat.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatRoomMember is a Querydsl query type for ChatRoomMember
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatRoomMember extends EntityPathBase<ChatRoomMember> {

    private static final long serialVersionUID = -1440478155L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatRoomMember chatRoomMember = new QChatRoomMember("chatRoomMember");

    public final com.siso.common.domain.QBaseTime _super = new com.siso.common.domain.QBaseTime(this);

    public final QChatRoom chatRoom;

    public final QChatRoomLimit chatRoomLimit;

    public final EnumPath<ChatRoomMemberStatus> chatRoomMemberStatus = createEnum("chatRoomMemberStatus", ChatRoomMemberStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastReadAt = createDateTime("lastReadAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> lastReadMessageId = createNumber("lastReadMessageId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.siso.user.domain.model.QUser user;

    public QChatRoomMember(String variable) {
        this(ChatRoomMember.class, forVariable(variable), INITS);
    }

    public QChatRoomMember(Path<? extends ChatRoomMember> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatRoomMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatRoomMember(PathMetadata metadata, PathInits inits) {
        this(ChatRoomMember.class, metadata, inits);
    }

    public QChatRoomMember(Class<? extends ChatRoomMember> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatRoom = inits.isInitialized("chatRoom") ? new QChatRoom(forProperty("chatRoom"), inits.get("chatRoom")) : null;
        this.chatRoomLimit = inits.isInitialized("chatRoomLimit") ? new QChatRoomLimit(forProperty("chatRoomLimit"), inits.get("chatRoomLimit")) : null;
        this.user = inits.isInitialized("user") ? new com.siso.user.domain.model.QUser(forProperty("user"), inits.get("user")) : null;
    }

}


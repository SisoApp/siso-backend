package com.siso.user.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -1358621978L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final com.siso.common.domain.QBaseTime _super = new com.siso.common.domain.QBaseTime(this);

    public final ListPath<com.siso.call.domain.model.Call, com.siso.call.domain.model.QCall> caller = this.<com.siso.call.domain.model.Call, com.siso.call.domain.model.QCall>createList("caller", com.siso.call.domain.model.Call.class, com.siso.call.domain.model.QCall.class, PathInits.DIRECT2);

    public final ListPath<com.siso.chat.domain.model.ChatMessage, com.siso.chat.domain.model.QChatMessage> chatMessages = this.<com.siso.chat.domain.model.ChatMessage, com.siso.chat.domain.model.QChatMessage>createList("chatMessages", com.siso.chat.domain.model.ChatMessage.class, com.siso.chat.domain.model.QChatMessage.class, PathInits.DIRECT2);

    public final ListPath<com.siso.chat.domain.model.ChatRoomMember, com.siso.chat.domain.model.QChatRoomMember> chatRoomMembers = this.<com.siso.chat.domain.model.ChatRoomMember, com.siso.chat.domain.model.QChatRoomMember>createList("chatRoomMembers", com.siso.chat.domain.model.ChatRoomMember.class, com.siso.chat.domain.model.QChatRoomMember.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<com.siso.image.domain.model.Image, com.siso.image.domain.model.QImage> images = this.<com.siso.image.domain.model.Image, com.siso.image.domain.model.QImage>createList("images", com.siso.image.domain.model.Image.class, com.siso.image.domain.model.QImage.class, PathInits.DIRECT2);

    public final BooleanPath isBlock = createBoolean("isBlock");

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final DateTimePath<java.time.LocalDateTime> lastActiveAt = createDateTime("lastActiveAt", java.time.LocalDateTime.class);

    public final BooleanPath notificationSubscribed = createBoolean("notificationSubscribed");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final EnumPath<PresenceStatus> presenceStatus = createEnum("presenceStatus", PresenceStatus.class);

    public final EnumPath<Provider> provider = createEnum("provider", Provider.class);

    public final ListPath<com.siso.call.domain.model.Call, com.siso.call.domain.model.QCall> receiver = this.<com.siso.call.domain.model.Call, com.siso.call.domain.model.QCall>createList("receiver", com.siso.call.domain.model.Call.class, com.siso.call.domain.model.QCall.class, PathInits.DIRECT2);

    public final StringPath refreshToken = createString("refreshToken");

    public final EnumPath<RegistrationStatus> registrationStatus = createEnum("registrationStatus", RegistrationStatus.class);

    public final ListPath<com.siso.report.domain.model.Report, com.siso.report.domain.model.QReport> reported = this.<com.siso.report.domain.model.Report, com.siso.report.domain.model.QReport>createList("reported", com.siso.report.domain.model.Report.class, com.siso.report.domain.model.QReport.class, PathInits.DIRECT2);

    public final ListPath<com.siso.report.domain.model.Report, com.siso.report.domain.model.QReport> reporter = this.<com.siso.report.domain.model.Report, com.siso.report.domain.model.QReport>createList("reporter", com.siso.report.domain.model.Report.class, com.siso.report.domain.model.QReport.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final SetPath<UserInterest, QUserInterest> userInterests = this.<UserInterest, QUserInterest>createSet("userInterests", UserInterest.class, QUserInterest.class, PathInits.DIRECT2);

    public final QUserProfile userProfile;

    public final com.siso.voicesample.domain.model.QVoiceSample voiceSample;

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.userProfile = inits.isInitialized("userProfile") ? new QUserProfile(forProperty("userProfile"), inits.get("userProfile")) : null;
        this.voiceSample = inits.isInitialized("voiceSample") ? new com.siso.voicesample.domain.model.QVoiceSample(forProperty("voiceSample"), inits.get("voiceSample")) : null;
    }

}


package com.applehip.activemq.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "CHATROOM_MESSAGE")
class ChatMessage(
    @EmbeddedId
    var chatMessageId : ChatMessageId? = null,
    @Column
    var userNo : Long?,
    @Column
    var message : String = "",
    @Column
    var messageType : String = "N",
    @Column
    @CreatedDate
    var writeDate : LocalDateTime? = null
)

@Embeddable
class ChatMessageId(
        @Column
        private var chatroomId : Long?,
        @Column
        private var seq : Long?
) : Serializable
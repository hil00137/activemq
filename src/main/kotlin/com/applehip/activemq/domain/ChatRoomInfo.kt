package com.service.applehip.domain.chat

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "CHATROOM_INFO")
class ChatRoomInfo(
    @Id
    var id : Long? = null,
    @Column
    var maxSeq : Long = 0, //마지막 글 순번
    @Column
    var userList : String = "", // 참여한 유저 리스트
    @Column
    @LastModifiedDate
    var lastDate : LocalDateTime? = null, //마지막 글 작성시간
    @Column
    var delYn : String = "N",
    @Column
    var delDate : LocalDateTime? = null,
    @Column
    var regUserNo : Long? = null,
    @Column
    @CreatedDate
    var regDate : LocalDateTime? = null
)

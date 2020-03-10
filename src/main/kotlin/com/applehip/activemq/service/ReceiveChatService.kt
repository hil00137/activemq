package com.applehip.activemq.service

import com.applehip.activemq.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.Exception
import javax.jms.MapMessage

@Service
class ReceiveChatService(
        private var chatRoomInfoRepository: ChatRoomInfoRepository,
        private var chatMessageRepository: ChatMessageRepository
) {
    /**
     * Roominfo 가져오기
     */
    fun getRoomInfo(roomId : Long): ChatRoomInfo? {
        val option = this.chatRoomInfoRepository.findById(roomId)
        return if(option.isPresent) {
            option.get()
        } else {
            null
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun saveChatMessage(message: MapMessage, roomInfo : ChatRoomInfo) : Boolean {
        val seq = ++roomInfo.maxSeq
        val userNo = message.getLong("requestId")
        chatRoomInfoRepository.save(roomInfo)
        val chatMessage = ChatMessage(
                chatMessageId = ChatMessageId(chatroomId = roomInfo.id, seq = seq),
                userNo = userNo,
                message = message.getString("msg")
        )
        chatMessageRepository.save(chatMessage)
        return true
    }
}
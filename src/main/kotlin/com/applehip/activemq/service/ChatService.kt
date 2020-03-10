package com.applehip.activemq.service

import com.applehip.activemq.domain.ChatRoomInfo
import com.applehip.activemq.domain.ChatRoomInfoRepository
import org.springframework.stereotype.Service

@Service
class ChatService(
        private var chatRoomInfoRepository: ChatRoomInfoRepository
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
}
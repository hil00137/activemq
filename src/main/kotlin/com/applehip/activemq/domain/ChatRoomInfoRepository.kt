package com.service.applehip.domain.chat

import org.springframework.data.jpa.repository.JpaRepository

interface ChatRoomInfoRepository : JpaRepository<ChatRoomInfo, Long> {
}
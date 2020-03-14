package com.applehip.activemq.domain

import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessage, ChatMessageId> {
}
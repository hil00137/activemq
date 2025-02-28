package com.applehip.activemq.service

import com.applehip.activemq.agent.ChatAgent
import com.applehip.activemq.domain.ChatRoomInfoRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.core.JmsTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import javax.jms.QueueConnectionFactory

@Service
class AgentHealthService(
        private var jmsTemplate: JmsTemplate,
        private var chatService : ChatService
) {

    @Value(value = "\${chat.queue.size}")
    private lateinit var queueSize : String
    @Value(value = "\${chat.queue.prefix}")
    private lateinit var queuePrefix : String

    fun getQueueSize() = queueSize.toInt()

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
        val list = ConcurrentHashMap<String, ChatAgent>()
        var count = 0
    }

    /**
     * 1초마다 agent 헬스 체크 및 agent 재기동
     */
    @Scheduled(cron = "0/1 * * * * *")
    fun checkAgent() {
        val size = this.getQueueSize()

        if(ChatAgent.connection == null) {
            val factory = jmsTemplate.connectionFactory as QueueConnectionFactory
            ChatAgent.connection = factory.createQueueConnection().also { it.start() }
        }

        for ( i in 0 until size) {
            val a = list["$i"]
            if(a == null || !a.alive) {
                val agent = ChatAgent(
                        queueName = "${queuePrefix}_$i",
                        chatService = chatService)
                list["$i"] = agent.also { it.start() }
                logger.info("${queuePrefix}_$i Agent Create")
                continue
            }
        }

        if(count == 0) {
            logger.info("Agent Health Service Active")
            count = 10
        }
        count--
    }

    fun aliveCheck(index : Int) : String {
        val agent = list[index.toString()] ?: return "$index does not registered"
        return if(agent.isAlive) {
            "$index is Alive"
        } else {
            "$index is Down. Waiting Please"
        }
    }
}
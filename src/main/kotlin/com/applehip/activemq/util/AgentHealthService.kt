package com.applehip.activemq.util

import com.applehip.activemq.agent.ChatAgent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class AgentHealthService {

    @Value(value = "\${chat.queue.size}")
    private lateinit var queueSize : String
    @Value(value = "\${chat.queue.prefix}")
    private lateinit var queuePrefix : String

    fun getQueueSize() = queueSize.toInt()

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
        val list = ConcurrentHashMap<String, ChatAgent>()
    }

    /**
     * 1초마다 agent 헬스 체크 및 agent 재기동
     */
    @Scheduled(cron = "0/1 * * * * *")
    fun checkAgent() {
        val size = this.getQueueSize()
        for ( i in 0 until size) {
            val a = list["$i"]
            if(a == null || !a.alive) {
                list["$i"] = ChatAgent("${queuePrefix}_$i").also { it.start() }
                logger.info("$i 생성")
                continue
            }
        }
        logger.info("==========================")
        logger.info("size : ${list.size}")
    }
}
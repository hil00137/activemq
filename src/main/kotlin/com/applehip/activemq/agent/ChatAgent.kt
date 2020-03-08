package com.applehip.activemq.agent

import org.slf4j.LoggerFactory
import kotlin.random.Random

class ChatAgent(val queueName : String) : Thread() {

    val logger = LoggerFactory.getLogger(this::class.java)
    var alive = false

    /**
     * 샘플 Agent
     */
    override fun run() {
        try {
            var count = 0
            val max = Random.nextInt(10, 20)
            logger.info("max : $max")
            while(true) {
                alive = true
                sleep(1000)
                count++
                logger.debug("$queueName is alive")
                if(count == max) {
                    throw Exception()
                }
            }
        } catch (exception : Exception) {
            alive = false
            logger.error("$queueName : "+exception.message)
        }
    }
}
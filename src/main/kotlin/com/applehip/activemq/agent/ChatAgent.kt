package com.applehip.activemq.agent

import org.slf4j.LoggerFactory
import javax.jms.*

class ChatAgent(val queueName : String) : Thread() {

    val logger = LoggerFactory.getLogger(this::class.java)
    var alive = false
    var session : QueueSession? = null
    var receiver : QueueReceiver? = null

    companion object {
        var connection : QueueConnection? = null
    }

    /**
     * 실제 ActiveMq Agent
     */
    override fun run() {
        try {
            session = connection?.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE)
            val queue = session?.createQueue(queueName)
            receiver = session?.createReceiver(queue)

            while(true) {
                if(receiver == null) {
                    break
                }
                alive = true
                val message = receiver!!.receiveNoWait()
                if(message == null) {
                    sleep(100)
                    continue
                }

                val mapMessage = message as MapMessage
                logger.info("${mapMessage.getString("requestId")} in ${mapMessage.getString("roomId")}")
                message.acknowledge()
                logger.debug("$queueName is alive")
            }
        } catch (exception : Exception) {
            receiver?.close()
            receiver = null
            session?.close()
            session = null
            alive = false
            logger.error("$queueName : "+exception.message)
        }
    }
}
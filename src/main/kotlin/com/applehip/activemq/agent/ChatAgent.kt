package com.applehip.activemq.agent

import com.applehip.activemq.service.ChatService
import org.slf4j.LoggerFactory
import javax.jms.*

class ChatAgent(
        val queueName : String,
        private val chatService: ChatService
) : Thread() {

    var alive = false
    var jobFlag = false
    var session : QueueSession? = null
    var receiver : QueueReceiver? = null
    var totalCount : Int = 0
    var failCount : Int = 0
    var successCount : Int = 0

    companion object {
        var connection : QueueConnection? = null
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    /**
     * 실제 ActiveMq Agent
     */
    override fun run() {
        currentThread().name = queueName
        try {
            // Connection Check
            if(connection == null) {
                throw Exception("Connection Fail")
            }

            // Session Check
            session = connection?.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE)?:throw Exception("Session Create Fail")
            logger.info("Session Create Success")

            // Queue Check
            val queue = session?.createQueue(queueName) ?: throw Exception("Queue Create Fail")
            logger.info("Queue Create Success")

            // Receiver check
            receiver = session?.createReceiver(queue)?:throw Exception("Receiver Create Fail")
            logger.info("Receiver Create Success")

            while(true) {
                this.alive = true
                val message = this.receiver!!.receiveNoWait()
                if(message == null) {
                    // 작업이 일어났을 경우에만 합 출력
                    if(this.jobFlag) {
                        logger.info("Read : $totalCount , Fail : $failCount, Input : $successCount ")
                    }
                    this.resetCount()
                    sleep(100)
                    continue
                }
                this.jobFlag = true
                this.totalCount++

                // 1. message 가 MapMessage 형식이 아니면 Fail
                if(message !is MapMessage) {
                    this.failCount++
                    message.acknowledge()
                    logger.debug("Message Is not MapMessage")
                    continue
                }

                // 2. message의 roomId가 parsing 할수 없으면 Fail
                if(!this.isValidRoomId(message)) {
                    this.failCount++
                    message.acknowledge()
                    continue
                }

                // 3. message의 requestId가 parsing 할수 없으면 Fail
                if(!isValidRequestId(message)) {
                    this.failCount++
                    message.acknowledge()
                    continue
                }

                // 4. message에 msg가 없을 때 Fail
                val msg : String? = message.getString("msg")
                if(msg == null || msg.trim() == "") {
                    this.failCount++
                    logger.debug("[No msg]\n $message")
                    message.acknowledge()
                    continue
                }

                val roomInfo = this.checkRoom(roomId = message.getLong("roomId"))

                // 5. 해당 방번호로 RoomInfo 가  없을때 Fail
                if(roomInfo == null) {
                    this.failCount++
                    logger.debug("[No exist RoomId]\n $message")
                    message.acknowledge()
                    continue
                }

                // 6. 해당 방번호의 인원이 전부 나갔을때 Fail
                if(roomInfo.delYn == "Y") {
                    this.failCount++
                    logger.debug("[Deleted Room]\n $message")
                    message.acknowledge()
                    continue
                }

                // 7. 해당 방에 해당 유저가 없으면 Fail
                if(!isUserInRoom(roomInfo.userList, message.getString("requestId"))) {
                    this.failCount++
                    logger.debug("[No User in Room]\n $message")
                    message.acknowledge()
                    continue
                }

                successCount++
                // 8. 실질적으로 insert 하는 부분
                logger.info("${message.getString("requestId")} in ${message.getString("roomId")}")
                message.acknowledge()
            }
        } catch (exception : Exception) {
            logger.error(exception.message)
        } finally {
            receiver?.close()
            receiver = null
            session?.close()
            session = null
            alive = false
        }
    }

    /**
     * 유효한 RoomId 인지 확인
     */
    private fun isValidRoomId(message : MapMessage): Boolean {
        val roomId = message.getString("roomId")
        if(roomId == null) {
            logger.debug("[No RoomId]\n $message")
            return false
        }

        try {
            roomId.toLong()
        } catch (notLong : NumberFormatException) {
            logger.debug("[RoomId parsing error] - $roomId \n $message")
            return false
        }

        return true
    }

    /**
     * 유효한 RequestId 인지 확인
     */
    private fun isValidRequestId(message : MapMessage): Boolean {
        val requestId = message.getString("requestId")
        if(requestId == null) {
            logger.debug("[No RequestId]\n $message")
            return false
        }

        try {
            requestId.toLong()
        } catch (notLong : NumberFormatException) {
            logger.debug("[RequestId parsing error] - $requestId \n $message")
            return false
        }

        return true
    }

    /**
     * 방번호를 이용하여 ChatRoomInfo를 가져옴.
     */
    private fun checkRoom(roomId : Long) = this.chatService.getRoomInfo(roomId)

    /**
     * 해당 방에 참여되있는 유저인지 확인
     */
    private fun isUserInRoom(userList : String, requestUser : String) = userList.split("|").contains(requestUser)

    /**
     * Count 초기화 및 jobFlag 를 false 로 변경
     */
    private fun resetCount() {
        this.totalCount = 0
        this.failCount = 0
        this.successCount = 0
        this.jobFlag = false
    }
}
package com.github.alien11689.scheduledmessaging

import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.ScheduledMessage
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import javax.jms.Connection
import javax.jms.ConnectionFactory
import javax.jms.Destination
import javax.jms.Message
import javax.jms.MessageConsumer
import javax.jms.MessageListener
import javax.jms.MessageProducer
import javax.jms.Queue
import javax.jms.Session
import javax.jms.TextMessage

class ScheduledMessagesTest extends Specification {

    String consumerText

    @Unroll
    def 'should send message with 8 second delay to queue #currentUuid'() {
        given:
            String queue = "scheduledMessageQueue-$currentUuid"
            Connection connection = activeMQConnectionFactory.createConnection()
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE)
            Destination destination = session.createQueue(queue)
            prepareConsumer(connection, session, destination)
            MessageProducer producer = prepareProducer(session, destination)
            Message message = session.createTextMessage("Message: $currentUuid")
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 8000)
        when:
            producer.send(message)
            println("Now is ${new Date()}")
        then:
            Thread.sleep(5000)
        and:
            consumerText == null
        and:
            new PollingConditions().within(5) {
                consumerText != null
            }
        where:
            currentUuid << [UUID.randomUUID()]
    }

    private MessageProducer prepareProducer(Session session, Queue destination) {
        session.createProducer(destination)
    }

    private void prepareConsumer(Connection connection, Session session, Queue destination) {
        MessageConsumer consumer = session.createConsumer(destination)
        consumer.messageListener = listener
        connection.start()
    }

    MessageListener listener = new MessageListener() {
        @Override
        void onMessage(Message message) {
            println("Message received at ${new Date()}")
            consumerText = (message as TextMessage).text
        }
    }

    ConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory('admin', 'admin', 'tcp://localhost:61616')
}

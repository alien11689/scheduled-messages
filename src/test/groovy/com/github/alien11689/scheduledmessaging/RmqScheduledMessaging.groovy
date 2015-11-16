package com.github.alien11689.scheduledmessaging

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

class RmqScheduledMessaging extends Specification {
    String exchange = 'my-exchange'
    String consumerText

    @Unroll
    def 'should send message with 8 second delay to queue #currentUuid'() {
        given:
            String queue = "scheduledMessageQueue-$currentUuid"
            createQueue(queue)
            createConsumer(queue)
            println("Now is ${new Date()}")
        when:
            channel.basicPublish(exchange,
                    queue,
                    new AMQP.BasicProperties.Builder().headers('x-delay': 8000).build(),
                    "Message: $currentUuid".bytes)
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

    private void createQueue(String queue) {
        channel.exchangeDeclare(exchange, 'x-delayed-message', true, false, ['x-delayed-type': 'direct']);
        channel.queueDeclare(queue, true, false, false, null);
        channel.queueBind(queue, exchange, queue);
    }

    private void createConsumer(String queue) {
        consumerChannel.queueDeclare(queue, true, false, false, null);
        Consumer consumer = new DefaultConsumer(consumerChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                println("Message received at ${new Date()}")
                consumerText = new String(body, "UTF-8")
            }
        };
        consumerChannel.basicConsume(queue, true, consumer);
    }

    @AutoCleanup(quiet = true)
    ConnectionFactory connectionFactory = new ConnectionFactory(
            host: 'localhost',
            username: 'admin',
            password: 'admin')

    @AutoCleanup(quiet = true)
    Connection connection = connectionFactory.newConnection()

    @AutoCleanup(quiet = true)
    Channel channel = connection.createChannel();

    @AutoCleanup(quiet = true)
    Channel consumerChannel = connection.createChannel();

}

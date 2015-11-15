FROM rabbitmq

RUN curl http://www.rabbitmq.com/community-plugins/v3.5.x/rabbitmq_delayed_message_exchange-0.0.1-rmq3.5.x-9bf265e4.ez > rabbitmq_delayed_message_exchange-0.0.1-rmq3.5.x-9bf265e4.ez && \
    mv rabbitmq_delayed_message_exchange-0.0.1-rmq3.5.x-9bf265e4.ez /usr/lib/rabbitmq/lib/rabbitmq_server-3.5.6/plugins/ && \
    rabbitmq-plugins enable --offline rabbitmq_delayed_message_exchange && \
    rabbitmq-plugins enable --offline rabbitmq_management


ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 5672 4369 25672 15672
CMD ["rabbitmq-server"]

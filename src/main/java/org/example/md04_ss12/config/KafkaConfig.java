package org.example.md04_ss12.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Bai 2, 5, 6: tao topic va cau hinh retry cho consumer.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic orderEventsTopic(@Value("${app.kafka.topic.order-events}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic shippingEventsTopic(@Value("${app.kafka.topic.shipping-events}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic flashSaleOrdersTopic(@Value("${app.kafka.topic.flash-sale-orders}") String name) {
        return TopicBuilder.name(name).partitions(6).replicas(1).build();
    }

    /** Loi khi xu ly message: thu lai 3 lan, moi lan cach 1s, sau do log va bo qua. */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(1000L, 3));
    }
}

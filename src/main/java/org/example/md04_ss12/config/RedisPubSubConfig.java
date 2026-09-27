package org.example.md04_ss12.config;

import org.example.md04_ss12.product.PromotionUpdateSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Bai 4: Product-Service dang ky lang nghe channel "promotion-updates".
 */
@Configuration
public class RedisPubSubConfig {

    @Bean
    public ChannelTopic promotionUpdatesTopic(@Value("${app.redis.channel.promotion-updates}") String channel) {
        return new ChannelTopic(channel);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                       PromotionUpdateSubscriber subscriber,
                                                                       ChannelTopic promotionUpdatesTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(subscriber, promotionUpdatesTopic);
        return container;
    }
}

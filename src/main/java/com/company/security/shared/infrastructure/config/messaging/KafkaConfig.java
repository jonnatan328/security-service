package com.company.security.shared.infrastructure.config.messaging;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ReactiveKafkaProducerTemplate<String, String> reactiveKafkaProducerTemplate(
            KafkaProperties kafkaProperties) {

        Map<String, Object> producerProps = kafkaProperties.buildProducerProperties(null);
        SenderOptions<String, String> senderOptions = SenderOptions.create(producerProps);

        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }
}

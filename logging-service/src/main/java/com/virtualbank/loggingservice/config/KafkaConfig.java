package com.virtualbank.loggingservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration class
 * Sets up Kafka consumer and producer configurations
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.consumer.group-id}")
    private String groupId;

    @Value("${kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    /**
     * Consumer configuration properties
     * @return Map of consumer configuration properties
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // Disable auto-commit for manual acknowledgment
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        // Set maximum poll records
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        
        // Set session timeout
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        
        // Set heartbeat interval
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        
        return props;
    }

    /**
     * Producer configuration properties
     * @return Map of producer configuration properties
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        // Enable idempotence to prevent duplicate messages
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // Set acks to all for maximum durability
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        
        // Set retries
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        
        // Set batch size and linger for better performance
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        
        // Set buffer memory
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        return props;
    }

    /**
     * Consumer factory for creating Kafka consumers
     * @return ConsumerFactory instance
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /**
     * Producer factory for creating Kafka producers
     * @return ProducerFactory instance
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * Kafka template for sending messages
     * @return KafkaTemplate instance
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Kafka listener container factory for consuming messages
     * @return ConcurrentKafkaListenerContainerFactory instance
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Set concurrency (number of consumer threads)
        factory.setConcurrency(3);
        
        // Enable batch processing
        factory.setBatchListener(true);
        
        // Set poll timeout
        factory.getContainerProperties().setPollTimeout(3000);
        
        // Set ack mode to manual
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        return factory;
    }
} 
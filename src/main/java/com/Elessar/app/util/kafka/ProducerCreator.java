package com.Elessar.app.util.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import java.util.Properties;

/**
 * Created by Hans on 3/17/19.
 */
public class ProducerCreator {
    public static <K, V> Producer<K, V> create(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "localhost:9092");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);  // exactly once delivery => require in-flight requests < 5, retries > 0 and ack = all
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);  // to maintain order with retries
        props.put(ProducerConfig.LINGER_MS_CONFIG, 100);    // batching request coming within 100ms
        return new KafkaProducer(props, keySerializer, valueSerializer);
    }
}

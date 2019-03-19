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
        return new KafkaProducer(props, keySerializer, valueSerializer);
    }
}

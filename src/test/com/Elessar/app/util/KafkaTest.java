package com.Elessar.app.util;

import com.Elessar.app.util.kafka.ConsumerCreator;
import com.Elessar.app.util.kafka.KafkaConstants;
import com.Elessar.app.util.kafka.ProducerCreator;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Hans on 3/17/19.
 */
public class KafkaTest {
    private static final String testTopic = "Test";

    @Before
    public void setUp() throws Exception {
        final AdminClient adminClient = AdminClient.create(getAdminProps());
        NewTopic newTopic = new NewTopic(testTopic, 1, (short) 1);
        List<NewTopic> newTopics = new ArrayList<>();
        newTopics.add(newTopic);

        adminClient.createTopics(newTopics);
        adminClient.close();
    }

    @Test
    public void sendMsgTest() {
        final int msgCountToSend = 10;
        final Producer<Long, String> producer = ProducerCreator.create();
        final Consumer<Long, String> consumer = ConsumerCreator.create(testTopic);

        // send messages
        for (int i = 0; i < msgCountToSend; i++) {
            final ProducerRecord<Long, String> record = new ProducerRecord<>(testTopic, "Message " + i);

            try {
                final RecordMetadata metadata = producer.send(record).get();
                System.out.println("Message " + i + " sent to partition " + metadata.partition() + " with offset " + metadata.offset());

            } catch (Exception e) {
                System.out.printf("Caught exception during sending message to Kafka broker: %s\n", e.getMessage());
            }
        }

        // retrieve messages
        int msgCountReceived = 0;
        int noMsgFound = 0;

        while (msgCountReceived < msgCountToSend && noMsgFound < KafkaConstants.MAX_NO_MESSAGE_FOUND_COUNT) {
            final ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofSeconds((long) 1));
            if (consumerRecords.count() == 0) {
                noMsgFound += 1;
            }

            for (ConsumerRecord<Long, String> record : consumerRecords) {
                System.out.println(record.value() + " received from partition " + record.partition() + " with offset " + record.offset());
                assertEquals("Message " + msgCountReceived, record.value());
            }

            msgCountReceived += 1;
            consumer.commitSync();
        }

        consumer.close();
    }

    @After
    public void tearDown() throws Exception {
        final AdminClient adminClient = AdminClient.create(getAdminProps());
        List<String> Topics = new ArrayList<>();
        Topics.add(testTopic);

        adminClient.deleteTopics(Topics);
        adminClient.close();
    }

    private Properties getAdminProps() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstants.KAFKA_BROKERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        return props;
    }

}

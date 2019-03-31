package com.Elessar.app.client;

import com.Elessar.app.util.kafka.ConsumerCreator;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hans on 3/18/19.
 */
public class KafkaMsgQueue implements MsgQueue {
    private final Consumer<Long, String> consumer;

    public KafkaMsgQueue(String topic) {
        topic = topic.replaceAll("\\s", "");
        consumer = ConsumerCreator.create(topic, new LongDeserializer(), new StringDeserializer());
    }

    @Override
    public List<String> poll(Duration timeout) throws Exception {
        System.out.println("This is pull mode in client");
        final ConsumerRecords<Long, String> consumerRecords = consumer.poll(timeout);
        final List<String> messageList = new ArrayList<>();
        for (ConsumerRecord<Long, String> record : consumerRecords) {
            messageList.add(record.value());
        }

        consumer.commitSync();
        return messageList;
    }

    @Override
    public void close() {
        consumer.close();
    }

}

package com.Elessar.app.util;

import com.Elessar.app.server.KafkaMsgSender;
import com.Elessar.app.server.Message;
import com.Elessar.app.server.MsgSender;
import com.Elessar.app.util.kafka.ConsumerCreator;
import com.Elessar.app.util.kafka.KafkaConstants;
import com.Elessar.proto.P2Pmsg;
import com.Elessar.proto.P2Pmsg.P2PMsgRequest;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

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
        final MsgSender msgSender = new KafkaMsgSender();
        final Consumer<Long, String> consumer = ConsumerCreator.create(testTopic, "Feiyi Wang", new LongDeserializer(), new StringDeserializer());

        // send messages
        final List<Message> messages = new ArrayList<>();
        final P2PMsgRequest.Builder p2pMsgRequest = P2PMsgRequest.newBuilder();

        for (int i = 0; i < msgCountToSend; i++) {
            final Message message = new Message("Shuai Ni", "Feiyi Wang", getRandomStr(10), (long) 0, false);

            messages.add(message);
            p2pMsgRequest.setFromUser(message.getFromUser())
                         .setToUser(message.getToUser())
                         .addMessage(P2Pmsg.Message.newBuilder()
                                 .setText(message.getText())
                                 .setTimestamp(message.getTimestamp()));
        }

        try {
            msgSender.send(messages, testTopic);
        } catch (Exception e) {
            System.out.printf("Caught exception during sending message to Kafka broker: %s\n", e.getMessage());
            assert(false);
        }

        // retrieve messages
        final ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofSeconds((long) 1));

        assertEquals(1, consumerRecords.count());

        for (ConsumerRecord<Long, String> record : consumerRecords) {
            assertEquals(p2pMsgRequest.build().toString(), record.value());
        }

        consumer.commitSync();
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

    private static String getRandomStr(int length) {
        final StringBuilder sb = new StringBuilder();
        final Random r = new Random();

        for (int i = 0; i < length; i++) {
            final int num = r.nextInt(26);
            sb.append((char) ('a' + num));
        }

        return sb.toString();
    }

}

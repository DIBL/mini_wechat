package com.Elessar.app.server;


import com.Elessar.proto.P2Pmsg;
import com.Elessar.proto.P2Pmsg.P2PMsgRequest;
import com.Elessar.proto.P2Pmsg.P2PMsgResponse;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.Properties;

/**
 * Created by Hans on 3/17/19.
 */
public class KafkaMsgSender implements MsgSender {
    private static final Logger logger = LogManager.getLogger(KafkaMsgSender.class);
    private final Producer<Long, String> producer;

    public KafkaMsgSender() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "client1");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(props);
    }

    @Override
    public P2PMsgResponse send(List<Message> messages, String topic) throws Exception {
        final P2PMsgRequest.Builder p2pMsgRequest = P2PMsgRequest.newBuilder();
        final String fromUser = messages.get(0).getFromUser();
        final String toUser = messages.get(0).getToUser();

        for (Message message : messages) {
            p2pMsgRequest.setFromUser(fromUser)
                         .setToUser(toUser)
                         .addMessage(P2Pmsg.Message.newBuilder()
                                .setText(message.getText())
                                .setTimestamp(message.getTimestamp()));
        }

        final ProducerRecord<Long, String> record = new ProducerRecord<>(topic, p2pMsgRequest.toString());
        final RecordMetadata metadata = producer.send(record).get();

        logger.info("Message sent to Kafka server at topic " + topic + " partition " + metadata.partition() + " with offset " + metadata.offset());

        final P2PMsgResponse.Builder p2pMsgResponse = P2PMsgResponse.newBuilder();
        p2pMsgResponse.setSuccess(true).setIsDelivered(false);

        return p2pMsgResponse.build();
    }
}

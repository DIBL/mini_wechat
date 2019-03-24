package com.Elessar.app.server;

import com.Elessar.app.util.Metric;
import com.Elessar.app.util.MetricManager;
import com.Elessar.app.util.kafka.ProducerCreator;
import com.Elessar.proto.P2Pmsg;
import com.Elessar.proto.P2Pmsg.P2PMsgRequest;
import com.Elessar.proto.P2Pmsg.P2PMsgResponse;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;

/**
 * Created by Hans on 3/17/19.
 */
public class KafkaMsgSender implements MsgSender {
    private final String KAFKA = "kafka";
    private static final Logger logger = LogManager.getLogger(KafkaMsgSender.class);
    private final MetricManager metricManager;
    private final Producer<Long, String> producer;

    public KafkaMsgSender(MetricManager metricManager) {
        this.metricManager = metricManager;
        final Serializer<Long> keySerializer = new LongSerializer();
        final Serializer<String> valueSerializer = new StringSerializer();
        producer = ProducerCreator.create(keySerializer, valueSerializer);
    }

    @Override
    public P2PMsgResponse send(List<Message> messages, String topic) throws Exception {
        final Metric metric = metricManager.newMetric(new StringBuilder().append(MsgSender.MSG_SENDER).append(".")
                                                                         .append(MsgSender.SEND).toString());

        final P2PMsgRequest.Builder p2pMsgRequest = P2PMsgRequest.newBuilder();
        final String fromUser = messages.get(0).getFromUser();
        final String toUser = messages.get(0).getToUser();

        p2pMsgRequest.setFromUser(fromUser).setToUser(toUser);

        for (Message message : messages) {
            p2pMsgRequest.addMessage(P2Pmsg.Message.newBuilder()
                                .setText(message.getText())
                                .setTimestamp(message.getTimestamp()));
        }

        topic = topic.replaceAll("\\s", "");
        final ProducerRecord<Long, String> record = new ProducerRecord<>(topic, p2pMsgRequest.toString());
        final P2PMsgResponse.Builder p2pMsgResponse = P2PMsgResponse.newBuilder();
        final RecordMetadata metadata = producer.send(record).get();    // Synchronous send and wait for ack before proceed

        logger.trace("Message sent to Kafka server at topic " + metadata.topic() + " partition " + metadata.partition() + " with offset " + metadata.offset());
        p2pMsgResponse.setSuccess(true).setIsDelivered(true);

        metric.timerStop();

        return p2pMsgResponse.build();
    }

    @Override
    public void close() {
        producer.close();
    }
}
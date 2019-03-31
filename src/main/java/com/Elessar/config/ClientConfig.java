package com.Elessar.config;

import com.Elessar.app.client.BlockingMsgQueue;
import com.Elessar.app.client.KafkaMsgQueue;
import com.Elessar.app.client.MsgQueue;
import com.Elessar.app.util.MetricManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;

@Configuration
@ComponentScan("com.Elessar.app.client")
@PropertySource("file:/Users/hans/Self-Learning/Project/mini-wechat/target/client.properties")
public class ClientConfig {
    @Value("${client.mode}")
    private String mode;

    @Value("${client.port}")
    private Integer port;

    @Value ("${client.server_url}")
    private String serverURL;

    @Bean
    public Integer port() {
        return port;
    }

    @Bean
    public String serverURL() {
        return serverURL;
    }

    @Bean
    public MetricManager metricManager() {
        return new MetricManager("ClientMetric", 100);
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MsgQueue msgQueue(String currUser) {
        if (!"push".equals(mode) && !"pull".equals(mode)) {
            throw new RuntimeException(mode + " mode is not supported");
        }

        if ("pull".equals(mode)) {
            return new KafkaMsgQueue(currUser);
        }

        return new BlockingMsgQueue(port(), metricManager());
    }
}

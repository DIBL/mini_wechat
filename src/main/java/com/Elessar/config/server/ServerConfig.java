package com.Elessar.config.server;
import com.Elessar.app.server.DirectMsgSender;
import com.Elessar.app.server.KafkaMsgSender;
import com.Elessar.app.server.MsgSender;
import com.Elessar.app.server.User;
import com.Elessar.app.util.MetricManager;
import com.Elessar.database.MongoDB;
import com.Elessar.database.MyDatabase;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@ComponentScan("com.Elessar.app.server")
@PropertySource("classpath:server.properties")
public class ServerConfig {
    @Value ("${mode}")
    private String mode;

    @Value ("${port}")
    private Integer port;

    @Value ("${cache_timeToExpire}")
    private Long ttl;

    @Bean
    public String mode() {
        return mode;
    }

    @Bean
    public InetSocketAddress inetSocketAddress() {
        return new InetSocketAddress("localhost", port);
    }

    @Bean
    public MetricManager metricManager() {
        return new MetricManager("ServerMetric", 100);
    }

    @Bean
    public MyDatabase db() {
        return new MongoDB(MongoClients.create("mongodb://localhost:27017").getDatabase("myDB"), metricManager());
    }

    @Bean
    public LoadingCache<String, User> cache() {
        return CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(ttl, TimeUnit.SECONDS)
                .build(
                        new CacheLoader<String, User>() {
                            @Override
                            public User load(String userName) {
                                final List<User> users = db().find(new User(userName, null, null, null, null, null));
                                // Cannot find current user, return an empty user
                                if (users.isEmpty()) {
                                    return new User(null, null, null, null, null, null);
                                }

                                return users.get(0);
                            }
                        }
                );
    }

    @Bean
    public MsgSender msgSender() {
        if (!"push".equals(mode) && !"pull".equals(mode)) {
            throw new RuntimeException(mode + " mode is not supported");
        }

        if ("pull".equals(mode)) {
            return new KafkaMsgSender(inetSocketAddress().toString(), metricManager());
        }

        return new DirectMsgSender(metricManager());
    }
}

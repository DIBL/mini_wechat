package com.Elessar.config;
import com.Elessar.app.server.DirectMsgSender;
import com.Elessar.app.server.KafkaMsgSender;
import com.Elessar.app.server.MsgSender;
import com.Elessar.app.server.User;
import com.Elessar.app.util.HttpClient;
import com.Elessar.app.util.MetricManager;
import com.Elessar.database.MongoDB;
import com.Elessar.database.MyDatabase;
import com.google.api.client.http.javanet.NetHttpTransport;
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
@PropertySource("file:${server.root}/server.properties")
public class ServerConfig {
    @Value ("${server.mode}")
    private String mode;

    @Value ("${server.port}")
    private Integer port;

    @Value ("${server.cache_time_to_expire}")
    private Long ttl;

    @Value ("${server.metric_manager_buffer_size}")
    private Integer bufferSize;

    @Value ("${server.mongodb.host}")
    private String mongoHost;

    @Value ("${server.mongodb.port}")
    private Integer mongoPort;

    @Value ("${server.mongodb.db_name}")
    private String dbName;

    @Value ("${server.cache.max_size}")
    private Long cacheSize;

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
        return new MetricManager("ServerMetric", bufferSize);
    }

    @Bean
    public MyDatabase db() {
        return new MongoDB(MongoClients.create("mongodb://" + mongoHost + ":" + mongoPort).getDatabase(dbName), metricManager());
    }

    @Bean
    public LoadingCache<String, User> cache() {
        return CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
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

        return new DirectMsgSender(metricManager(), httpClient());
    }

    @Bean
    public HttpClient httpClient() {
        return new HttpClient(new NetHttpTransport().createRequestFactory());
    }
}

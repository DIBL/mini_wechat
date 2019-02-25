package com.Elessar.app.client;

import com.Elessar.app.server.Message;
import com.Elessar.app.server.MyServer;
import com.Elessar.app.server.User;
import com.Elessar.app.util.MetricManager;
import com.Elessar.database.MongoDB;
import com.Elessar.database.MyDatabase;
import com.Elessar.proto.Logoff.LogoffResponse;
import com.Elessar.proto.Logon.LogonResponse;
import com.Elessar.proto.P2Pmsg.P2PMsgResponse;
import com.Elessar.proto.Registration.RegistrationResponse;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.junit.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;
import static org.junit.Assert.assertEquals;

/**
 * Created by Hans on 2/24/19.
 */
public class MyClientIntegTest {
    private static MongoDatabase mongoDB;
    private static MyClient clientA, clientB;
    private static int clientA_Port, clientB_Port;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        final String serverAddress = "127.0.0.1";
        final int serverPort = 9000;
        final String serverURL = new StringBuilder().append("http://")
                                                    .append(serverAddress).append(":")
                                                    .append(serverPort).toString();
        clientA_Port = 4000;
        clientB_Port = 5000;
        final MetricManager serverMetricManager = new MetricManager("ServerMetric", 1000000);
        final MetricManager clientMetricManager = new MetricManager("ClientMetric", 1000000);

        // Setup server
        mongoDB = MongoClients.create("mongodb://localhost:27017").getDatabase("test");
        mongoDB.getCollection(MyDatabase.USERS).createIndex(Indexes.text(User.NAME), new IndexOptions().unique(true));

        final MyDatabase db = new MongoDB(mongoDB, serverMetricManager);
        final MyServer server = new MyServer("localhost", serverPort, db, serverMetricManager);

        // Setup client A
        final BlockingQueue<String> msgQueueA = new LinkedBlockingQueue<>();
        clientA = new MyClient(serverURL, clientMetricManager);
        final MyClientServer clientA_Server = new MyClientServer("localhost", clientA_Port, msgQueueA, clientMetricManager);

        // Setup client B
        final BlockingQueue<String> msgQueueB = new LinkedBlockingQueue<>();
        clientB = new MyClient(serverURL, clientMetricManager);
        final MyClientServer clientB_Server = new MyClientServer("localhost", clientB_Port, msgQueueB, clientMetricManager);

        server.run();
        clientA_Server.run();
        clientB_Server.run();
    }


    @Test
    public void test() {
        registerTest();

        logOnTest();

        logOffTest();

        p2pMsgTest();
    }

    private void registerTest() {
        try {
            try (MongoCursor<Document> cursor = mongoDB.getCollection(MyDatabase.USERS).find(or(eq(User.NAME, "Shuai Hu"),
                    eq(User.NAME, "Zhi Xu"))).iterator()) {
                assertEquals(false, cursor.hasNext());
            }

            RegistrationResponse registerResp1 = clientA.register("Shuai Hu", "4238902", "shuaih@yahoo.com", "62357492");
            assertEquals(true, registerResp1.getSuccess());

            RegistrationResponse registerResp2 = clientA.register("Shuai Hu", "4238902", "shuaih@yahoo.com", "62357492");
            assertEquals(false, registerResp2.getSuccess());

            RegistrationResponse registerResp3 = clientA.register("Zhi Xu", "213252", "zhix@126.com", "62258237");
            assertEquals(true, registerResp3.getSuccess());

            try (MongoCursor<Document> cursor = mongoDB.getCollection(MyDatabase.USERS).find(eq(User.NAME, "Shuai Hu")).iterator()) {
                assertEquals(true, cursor.hasNext());
            }

            try (MongoCursor<Document> cursor = mongoDB.getCollection(MyDatabase.USERS).find(eq(User.NAME, "Zhi Xu")).iterator()) {
                assertEquals(true, cursor.hasNext());
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            assert(false);
        }
    }


    private void logOnTest() {
        try {
            try (MongoCursor<Document> cursor = mongoDB.getCollection(MyDatabase.USERS).find(eq(User.NAME, "Shuai Hu")).iterator()) {
                if (cursor.hasNext()) {
                    assertEquals(false, cursor.next().getBoolean(User.ONLINE));
                }
            }

            LogonResponse logonResp1 = clientA.logOn("Ertai Cai", "2222123", clientA_Port);
            Assert.assertEquals(false, logonResp1.getSuccess());

            LogonResponse logonResp2 = clientA.logOn("Shuai Hu", "4238903", clientA_Port);
            Assert.assertEquals(false, logonResp2.getSuccess());

            LogonResponse logonResp3 = clientA.logOn("Shuai Hu", "4238902", clientA_Port);
            Assert.assertEquals(true, logonResp3.getSuccess());

            LogonResponse logonResp4 = clientA.logOn("Shuai Hu", "4238902", clientA_Port);
            Assert.assertEquals(true, logonResp4.getSuccess());

            try (MongoCursor<Document> cursor = mongoDB.getCollection(MyDatabase.USERS).find(eq(User.NAME, "Shuai Hu")).iterator()) {
                if (cursor.hasNext()) {
                    assertEquals(true, cursor.next().getBoolean(User.ONLINE));
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            assert(false);
        }
    }


    private void logOffTest() {
        try {
            try (MongoCursor<Document> cursor = mongoDB.getCollection(MyDatabase.USERS).find(eq(User.NAME, "Shuai Hu")).iterator()) {
                if (cursor.hasNext()) {
                    assertEquals(true, cursor.next().getBoolean(User.ONLINE));
                }
            }

            LogoffResponse logoffResp1 = clientA.logOff("Ertai Cai");
            Assert.assertEquals(false, logoffResp1.getSuccess());

            LogoffResponse logoffResp2 = clientA.logOff("Zhi Xu");
            Assert.assertEquals(true, logoffResp2.getSuccess());

            LogoffResponse logoffResp3 = clientA.logOff("Shuai Hu");
            Assert.assertEquals(true, logoffResp3.getSuccess());

            LogoffResponse logoffResp4 = clientA.logOff("Shuai Hu");
            Assert.assertEquals(true, logoffResp4.getSuccess());

            try (MongoCursor<Document> cursor = mongoDB.getCollection(MyDatabase.USERS).find(eq(User.NAME, "Shuai Hu")).iterator()) {
                if (cursor.hasNext()) {
                    assertEquals(false, cursor.next().getBoolean(User.ONLINE));
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            assert(false);
        }
    }

    private void p2pMsgTest() {
        try {
            try (MongoCursor<Document> cursor = mongoDB.getCollection(MyDatabase.MESSAGES).find(or(eq(Message.FROM_USER, "Shuai Hu"),
                                                                                                   eq(Message.FROM_USER, "Zhi Xu"))).iterator()) {
                assertEquals(false, cursor.hasNext());
            }

            LogonResponse logonResp1 = clientA.logOn("Shuai Hu", "4238902", clientA_Port);;
            assertEquals(true, logonResp1.getSuccess());

            P2PMsgResponse p2PMsgResp1 = clientA.sendMessage("Shuai Hu", "Zhi Xu", "SB");
            assertEquals(true, p2PMsgResp1.getSuccess());
            assertEquals(false, p2PMsgResp1.getIsDelivered());

            try (MongoCursor<Document> cursor = mongoDB.getCollection(MyDatabase.MESSAGES).find(eq(Message.FROM_USER, "Shuai Hu")).iterator()) {
                if (cursor.hasNext()) {
                    Document doc = cursor.next();
                    assertEquals("Zhi Xu", doc.getString(Message.TO_USER));
                    assertEquals(false, doc.getBoolean(Message.ISDELIVERED));
                }
            }

            LogonResponse logonResp2 = clientB.logOn("Zhi Xu", "213252", clientB_Port);
            assertEquals(true, logonResp1.getSuccess());


            try (MongoCursor<Document> cursor = mongoDB.getCollection(MyDatabase.MESSAGES).find(eq(Message.FROM_USER, "Shuai Hu")).iterator()) {
                if (cursor.hasNext()) {
                    Document doc = cursor.next();
                    assertEquals("Zhi Xu", doc.getString(Message.TO_USER));
                    assertEquals(true, doc.getBoolean(Message.ISDELIVERED));
                }
            }

            P2PMsgResponse p2PMsgResp2 = clientB.sendMessage("Zhi Xu", "Shuai Hu", "Ni Da Ye!");
            assertEquals(true, p2PMsgResp2.getSuccess());
            assertEquals(true, p2PMsgResp2.getIsDelivered());

            try (MongoCursor<Document> cursor = mongoDB.getCollection(MyDatabase.MESSAGES).find(eq(Message.FROM_USER, "Zhi Xu")).iterator()) {
                if (cursor.hasNext()) {
                    Document doc = cursor.next();
                    assertEquals("Shuai Hu", doc.getString(Message.TO_USER));
                    assertEquals(true, doc.getBoolean(Message.ISDELIVERED));
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            assert(false);
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        mongoDB.drop();
    }
}

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

import java.util.ArrayList;
import java.util.List;
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
        mongoDB = MongoClients.create("mongodb://localhost:27017").getDatabase("MyClientIntegTest");
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
    public void registerTest() {
        try {
            registerTestSetup();

            List<Document> list1 = getList(mongoDB.getCollection(MyDatabase.USERS).find(or(eq(User.NAME, "Shuai Hu"),
                                                                                           eq(User.NAME, "Zhi Xu"))).iterator());
            assertEquals(true, list1.isEmpty());

            RegistrationResponse registerResp1 = clientA.register("Shuai Hu", "4238902", "shuaih@yahoo.com", "62357492");
            assertEquals(true, registerResp1.getSuccess());

            // Repeated registration with same user name and password
            RegistrationResponse registerResp2 = clientA.register("Shuai Hu", "4238902", "shuaih@yahoo.com", "62357492");
            assertEquals(false, registerResp2.getSuccess());

            // Repeated registration with same user name but different password
            RegistrationResponse registerResp3 = clientA.register("Shuai Hu", "1239412", "shuaih@yahoo.com", "62357492");
            assertEquals(false, registerResp3.getSuccess());

            RegistrationResponse registerResp4 = clientA.register("Zhi Xu", "213252", "zhix@126.com", "62258237");
            assertEquals(true, registerResp4.getSuccess());

            List<Document> list2 = getList(mongoDB.getCollection(MyDatabase.USERS).find(eq(User.NAME, "Shuai Hu")).iterator());
            assertEquals(1, list2.size());
            assertEquals(MyClient.hash("4238902"), list2.get(0).getString(User.PASSWORD));
            assertEquals("shuaih@yahoo.com", list2.get(0).getString(User.EMAIL));
            assertEquals("62357492", list2.get(0).getString(User.PHONE));


            List<Document> list3 = getList(mongoDB.getCollection(MyDatabase.USERS).find(eq(User.NAME, "Zhi Xu")).iterator());
            assertEquals(1, list3.size());
            assertEquals(MyClient.hash("213252"), list3.get(0).getString(User.PASSWORD));
            assertEquals("zhix@126.com", list3.get(0).getString(User.EMAIL));
            assertEquals("62258237", list3.get(0).getString(User.PHONE));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            assert(false);
        }
    }

    @Test
    public void logOnTest() {
        try {
            logOnTestSetup();

            List<Document> list1 = getList(mongoDB.getCollection(MyDatabase.USERS).find(eq(User.NAME, "Shuai Hu")).iterator());
            assertEquals(1, list1.size());
            assertEquals(false, list1.get(0).getBoolean(User.ONLINE));

            // Unregistered user
            LogonResponse logonResp1 = clientA.logOn("Ertai Cai", "2222123", clientA_Port);
            Assert.assertEquals(false, logonResp1.getSuccess());

            // Registered and offline user with wrong password
            LogonResponse logonResp2 = clientA.logOn("Shuai Hu", "4238903", clientA_Port);
            Assert.assertEquals(false, logonResp2.getSuccess());

            // Registered and offline user with correct password
            LogonResponse logonResp3 = clientA.logOn("Shuai Hu", "4238902", clientA_Port);
            Assert.assertEquals(true, logonResp3.getSuccess());

            // Registered and online user with correct password
            LogonResponse logonResp4 = clientA.logOn("Shuai Hu", "4238902", clientA_Port);
            Assert.assertEquals(true, logonResp4.getSuccess());

            List<Document> list2 = getList(mongoDB.getCollection(MyDatabase.USERS).find(eq(User.NAME, "Shuai Hu")).iterator());
            assertEquals(1, list2.size());
            assertEquals(true, list2.get(0).getBoolean(User.ONLINE));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            assert(false);
        }
    }

    @Test
    public void logOffTest() {
        try {
            logOffTestSetup();

            List<Document> list1 = getList(mongoDB.getCollection(MyDatabase.USERS).find(eq(User.NAME, "Shuai Hu")).iterator());
            assertEquals(1, list1.size());
            assertEquals(true, list1.get(0).getBoolean(User.ONLINE));

            List<Document> list2 = getList(mongoDB.getCollection(MyDatabase.USERS).find(eq(User.NAME, "Zhi Xu")).iterator());
            assertEquals(1, list2.size());
            assertEquals(false, list2.get(0).getBoolean(User.ONLINE));

            // Unregistered user
            LogoffResponse logoffResp1 = clientA.logOff("Ertai Cai");
            Assert.assertEquals(false, logoffResp1.getSuccess());

            // Registered but offline user
            LogoffResponse logoffResp2 = clientA.logOff("Zhi Xu");
            Assert.assertEquals(true, logoffResp2.getSuccess());

            // Registered and online user
            LogoffResponse logoffResp3 = clientA.logOff("Shuai Hu");
            Assert.assertEquals(true, logoffResp3.getSuccess());

            // Registered and offline user
            LogoffResponse logoffResp4 = clientA.logOff("Shuai Hu");
            Assert.assertEquals(true, logoffResp4.getSuccess());

            List<Document> list3 = getList(mongoDB.getCollection(MyDatabase.USERS).find(eq(User.NAME, "Shuai Hu")).iterator());
            assertEquals(1, list3.size());
            assertEquals(false, list3.get(0).getBoolean(User.ONLINE));

            List<Document> list4 = getList(mongoDB.getCollection(MyDatabase.USERS).find(eq(User.NAME, "Zhi Xu")).iterator());
            assertEquals(1, list4.size());
            assertEquals(false, list4.get(0).getBoolean(User.ONLINE));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            assert(false);
        }
    }

    @Test
    public void p2pMsgTest() {
        try {
            p2pMsgTestSetup();

            List<Document> list1 = getList(mongoDB.getCollection(MyDatabase.MESSAGES).find(or(eq(Message.FROM_USER, "Shuai Hu"),
                                                                                              eq(Message.FROM_USER, "Zhi Xu"))).iterator());
            assertEquals(true, list1.isEmpty());

            LogonResponse logonResp1 = clientA.logOn("Shuai Hu", "4238902", clientA_Port);;
            assertEquals(true, logonResp1.getSuccess());

            // Send message from online user A to offline user B
            P2PMsgResponse p2PMsgResp1 = clientA.sendMessage("Shuai Hu", "Zhi Xu", "SB");
            assertEquals(true, p2PMsgResp1.getSuccess());
            assertEquals(false, p2PMsgResp1.getIsDelivered());

            List<Document> list2 = getList(mongoDB.getCollection(MyDatabase.MESSAGES).find(eq(Message.FROM_USER, "Shuai Hu")).iterator());
            assertEquals(1, list2.size());
            assertEquals("Zhi Xu", list2.get(0).getString(Message.TO_USER));
            assertEquals("SB", list2.get(0).getString(Message.TEXT));
            assertEquals(false, list2.get(0).getBoolean(Message.ISDELIVERED));

            // Offline user B log on to retrieve unread messages
            LogonResponse logonResp2 = clientB.logOn("Zhi Xu", "213252", clientB_Port);
            assertEquals(true, logonResp1.getSuccess());

            // Sleep 2 sec, wait for messages delivered to receipt
            Thread.sleep(2000L);

            List<Document> list3 = getList(mongoDB.getCollection(MyDatabase.MESSAGES).find(eq(Message.FROM_USER, "Shuai Hu")).iterator());
            assertEquals(1, list3.size());
            assertEquals("Zhi Xu", list3.get(0).getString(Message.TO_USER));
            assertEquals(true, list3.get(0).getBoolean(Message.ISDELIVERED));

            // Send message from online user B to online user A
            P2PMsgResponse p2PMsgResp2 = clientB.sendMessage("Zhi Xu", "Shuai Hu", "Ni Da Ye!");
            assertEquals(true, p2PMsgResp2.getSuccess());
            assertEquals(true, p2PMsgResp2.getIsDelivered());

            List<Document> list4 = getList(mongoDB.getCollection(MyDatabase.MESSAGES).find(eq(Message.FROM_USER, "Zhi Xu")).iterator());
            assertEquals(1, list4.size());
            assertEquals("Shuai Hu", list4.get(0).getString(Message.TO_USER));
            assertEquals(true, list4.get(0).getBoolean(Message.ISDELIVERED));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            assert(false);
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        mongoDB.drop();
    }


    private void registerTestSetup() throws Exception {
        mongoDB.getCollection(MyDatabase.USERS).deleteMany(new Document());

        List<Document> list1 = getList(mongoDB.getCollection(MyDatabase.USERS).find(new Document()).iterator());
        assertEquals(true, list1.isEmpty());
    }

    private void logOnTestSetup() throws Exception {
        registerTestSetup();

        RegistrationResponse registerResp1 = clientA.register("Shuai Hu", "4238902", "shuaih@yahoo.com", "62357492");
        assertEquals(true, registerResp1.getSuccess());

        RegistrationResponse registerResp2 = clientA.register("Zhi Xu", "213252", "zhix@126.com", "62258237");
        assertEquals(true, registerResp2.getSuccess());
    }

    private void logOffTestSetup() throws Exception {
        logOnTestSetup();

        LogonResponse logonResp3 = clientA.logOn("Shuai Hu", "4238902", clientA_Port);
        Assert.assertEquals(true, logonResp3.getSuccess());
    }

    private void p2pMsgTestSetup() throws Exception {
        logOffTestSetup();

        LogoffResponse logoffResp1 = clientA.logOff("Zhi Xu");
        Assert.assertEquals(true, logoffResp1.getSuccess());

        LogoffResponse logoffResp2 = clientA.logOff("Shuai Hu");
        Assert.assertEquals(true, logoffResp2.getSuccess());
    }

    private List<Document> getList(MongoCursor<Document> cursor) {
        List<Document> list = new ArrayList<>();
        while (cursor.hasNext()) {
            list.add(cursor.next());
        }

        return list;
    }
}

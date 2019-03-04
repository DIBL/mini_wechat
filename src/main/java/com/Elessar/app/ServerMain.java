package com.Elessar.app;

import com.Elessar.app.server.MyServer;
import com.Elessar.app.util.MetricManager;
import com.Elessar.database.MongoDB;
import com.Elessar.database.MyDatabase;
import com.example.tutorial.Addressbook.Person;
import com.mongodb.client.MongoClients;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;



/**
 * Created by Hans on 1/7/19.
 */

public class ServerMain {
    public static void main(String[] args){
        Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
        mongoLogger.setLevel(Level.SEVERE); // e.g. or Log.WARNING, etc.
        final MetricManager metricManager = new MetricManager("ServerMetric", 100);
        final MyDatabase db = new MongoDB(MongoClients.create("mongodb://localhost:27017").getDatabase("myDB"), metricManager);
        final MyServer server = new MyServer("localhost", 9000, db, metricManager);
        server.run();
        //testProtoBuf();
    }

    private static void testProtoBuf() {
        final Person.Builder sendPersonData = Person.newBuilder().setId(123).setName("DIBL").setEmail("abc@gmail.com");
        final Person.PhoneNumber.Builder phoneNumber = Person.PhoneNumber.newBuilder().setNumber("321-7684231");
        sendPersonData.addPhones(phoneNumber);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            sendPersonData.build().writeTo(os);
            try (InputStream is = new ByteArrayInputStream(os.toByteArray())) {
                final Person receivePersonData = Person.parseFrom(is);
                System.out.printf("receive and send data is equal: %b\n", sendPersonData.equals(receivePersonData));
                System.out.println(receivePersonData.toString());
                System.out.println(sendPersonData.toString());
            }
        } catch (IOException e) {
            System.out.println("Server failed because: " + e.getMessage());
        }

    }
}


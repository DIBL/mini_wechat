package com.Elessar.app;

import com.Elessar.app.server.MyServer;
import com.Elessar.config.server.ServerConfig;
import com.example.tutorial.Addressbook.Person;

import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


/**
 * Created by Hans on 1/7/19.
 */

public class ServerMain {
    /**
     *
     * @param args [0] push or pull mode
     */
    public static void main(String[] args){
        Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
        mongoLogger.setLevel(Level.SEVERE); // e.g. or Log.WARNING, etc.

        final ApplicationContext context = new AnnotationConfigApplicationContext(ServerConfig.class);
        final MyServer server = context.getBean(MyServer.class);

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


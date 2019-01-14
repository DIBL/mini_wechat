package com.Elessar.app;

import com.Elessar.app.server.MyServer;

import com.example.tutorial.Addressbook.Person;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by Hans on 1/7/19.
 */

public class Main {
    public static void main(String[] args){
        final MyServer server = new MyServer("localhost", 9000);
        server.run();
        testProtoBuf();
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
            System.out.println("Cannot write to stream");
        }

    }
}


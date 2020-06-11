package com.belano.auctionsniper;

import org.hamcrest.CoreMatchers;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.junit.Assert;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SingleMessageListener implements MessageListener {

    private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(1);

    @Override
    public void processMessage(Chat chat, Message message) {
        messages.add(message);
    }


    public void receivesAMessage() throws InterruptedException {
        Assert.assertThat("Message", messages.poll(5, TimeUnit.SECONDS),
                CoreMatchers.is(CoreMatchers.notNullValue()));
    }
}

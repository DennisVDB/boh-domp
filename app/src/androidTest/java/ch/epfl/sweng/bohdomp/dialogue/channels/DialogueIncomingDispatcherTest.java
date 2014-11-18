package ch.epfl.sweng.bohdomp.dialogue.channels;

import android.test.AndroidTestCase;

import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.UnknownContact;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.NullArgumentException;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueTextMessage;

import static ch.epfl.sweng.bohdomp.dialogue.channels.DialogueIncomingDispatcher.receiveMessage;

/**
 * Tests the DialogueIncomingDispatcher class.
 */
public class DialogueIncomingDispatcherTest extends AndroidTestCase {
    public void testReceiveMessageNullArguments() {
        Contact contact = new UnknownContact("123");
        String body = "Hello World!";

        DialogueMessage message = new DialogueTextMessage(contact, body, DialogueMessage.MessageStatus.INCOMING);

        try {
            receiveMessage(null, message);
            fail();
        } catch (NullArgumentException e) {
            // OK
        }

        try {
            receiveMessage(getContext(), null);
            fail();
        } catch (NullArgumentException e) {
            // OK
        }
    }
}
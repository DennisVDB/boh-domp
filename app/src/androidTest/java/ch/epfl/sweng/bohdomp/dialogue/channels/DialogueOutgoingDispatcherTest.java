package ch.epfl.sweng.bohdomp.dialogue.channels;

import android.test.AndroidTestCase;

import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.UnknownContact;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.NullArgumentException;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueTextMessage;

import static ch.epfl.sweng.bohdomp.dialogue.channels.DialogueOutgoingDispatcher.sendMessage;

/**
 * Tests the DialogueOutgoingDispatcher class.
 */
public class DialogueOutgoingDispatcherTest extends AndroidTestCase {
    public void testSendMessageNullArguments() {
        Contact contact = new UnknownContact("123");
        String body = "Hello World!";

        DialogueMessage message = new DialogueTextMessage(contact, body, DialogueMessage.MessageStatus.OUTGOING);

        try {
            sendMessage(null, message);
            fail();
        } catch (NullArgumentException e) {
            // OK
        }

        try {
            sendMessage(getContext(), null);
            fail();
        } catch (NullArgumentException e) {
            // OK
        }
    }
}
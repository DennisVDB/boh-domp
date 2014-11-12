package ch.epfl.sweng.bohdomp.dialogue.channels;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.ContactFactory;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.NullArgumentException;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueTextMessage;

public class DialogueOutgoingDispatcherTest extends AndroidTestCase {
    public void testNullArgsSendMessage() throws Exception {
        ContactFactory contactFactory = new ContactFactory(getContext());
        Contact contact = contactFactory.contactFromNumber("123");

        DialogueMessage message = new DialogueTextMessage(contact, "Hello", DialogueMessage.MessageStatus.OUTGOING);

        try {
            DialogueOutgoingDispatcher.sendMessage(null, message);
            Assert.fail("Should fail with null context parameter.");
        } catch (NullArgumentException e) {
            // Success
        }

        try {
            DialogueIncomingDispatcher.receiveMessage(getContext(), null);
            Assert.fail("Should fail with null message parameter.");
        } catch (NullArgumentException e) {
            // Success
        }
    }
}
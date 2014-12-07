package ch.epfl.sweng.bohdomp.dialogue.channels;


import android.content.Intent;
import android.test.ServiceTestCase;

import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.conversation.Conversation;
import ch.epfl.sweng.bohdomp.dialogue.data.DefaultDialogData;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.ContactFactory;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.NullArgumentException;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueTextMessage;


/**
 * Class creating a Tester for a Dialogue Outgoing Dispatcher
 */
public final class DialogueOutgoingDispatcherTest extends ServiceTestCase<DialogueOutgoingDispatcher> {

    private final static String ACTION_SEND_MESSAGE = "ACTION_SEND_MESSAGE";

    private DialogueMessage mMessage;
    private DialogueMessage mMessageIncoming;
    private Conversation mConversation;
    private Intent mIntent;
    private Intent mBadIntent;

    public DialogueOutgoingDispatcherTest() {
        super(DialogueOutgoingDispatcher.class);

    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ContactFactory contactFactory = new ContactFactory(getSystemContext());

        Contact contact = contactFactory.contactFromNumber("0762677108");
        mConversation = DefaultDialogData.getInstance().createOrGetConversation(contact);

        Contact.PhoneNumber number = contact.getPhoneNumbers().iterator().next();
        Contact.ChannelType channel = Contact.ChannelType.SMS;

        String body = "Hello";
        mMessage = new DialogueTextMessage(contact, channel, number, body, DialogueMessage.MessageDirection.OUTGOING);
        mMessageIncoming = new DialogueTextMessage(contact, null, null,
                body, DialogueMessage.MessageDirection.INCOMING);

        mIntent = new Intent();
        mIntent.setAction(ACTION_SEND_MESSAGE);
        mIntent.putExtra(DialogueMessage.MESSAGE, mMessage);

        mBadIntent = new Intent();
        mBadIntent.setAction("");
        mBadIntent.putExtra(DialogueMessage.MESSAGE, mMessage);
    }

    public void testReceiveNullContext() {
        try {
            DialogueOutgoingDispatcher.sendMessage(null, mMessage);
            fail("Exception should have been thrown");
        } catch (NullArgumentException e) {
            //ok
        }
    }

    public void testReceiveNullMessage() {
        try {
            DialogueOutgoingDispatcher.sendMessage(mContext, null);
            fail("Exception should have been thrown");
        } catch (NullArgumentException e) {
            //ok
        }
    }

    public void testReceiveOutgoingMessage() {
        try {
            DialogueOutgoingDispatcher.sendMessage(mContext, mMessageIncoming);
            fail("Exception should have been thrown");
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    public void testServiceStartedCorrectlyViaIntent() throws Exception {
        assertNull(getService());

        startService(mIntent);

        assertNotNull(getService());
    }

    public void testOnHandleGoodIntent() throws Exception {
        setupService();

        assertNotNull(getService());

        getService().onHandleIntent(mIntent);

        List<DialogueMessage> messages = DefaultDialogData.getInstance().
                getConversation(mConversation.getId()).getMessages();

        assertTrue(messages.size() > 0);

        DialogueMessage message1 = messages.get(messages.size() - 1);

        assertNotNull(message1);

        assertEquals(mMessage.getId(), message1.getId());
        assertEquals(mMessage.getContact(), message1.getContact());
        assertEquals(mMessage.getBody(), message1.getBody());
        assertEquals(mMessage.getIsDataMessage(), message1.getIsDataMessage());
        assertEquals(mMessage.getIsReadStatus(), message1.getIsReadStatus());
        assertEquals(mMessage.getStatus(), message1.getStatus());
        assertEquals(mMessage.getTimeStamp(), message1.getTimeStamp());
    }


    public void testOnHandleBadIntent() throws Exception {
        setupService();

        assertNotNull(getService());

        int initialNbOfMessages =  DefaultDialogData.getInstance().
                getConversation(mConversation.getId()).getMessages().size();

        getService().onHandleIntent(mBadIntent);

        int afterNbOfMessages =  DefaultDialogData.getInstance().
                getConversation(mConversation.getId()).getMessages().size();


        assertEquals(initialNbOfMessages, afterNbOfMessages);
    }
}

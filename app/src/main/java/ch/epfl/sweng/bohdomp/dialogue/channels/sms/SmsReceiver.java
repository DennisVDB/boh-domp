package ch.epfl.sweng.bohdomp.dialogue.channels.sms;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;


import ch.epfl.sweng.bohdomp.dialogue.BuildConfig;
import ch.epfl.sweng.bohdomp.dialogue.channels.DialogueIncomingDispatcher;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.ContactFactory;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.NullArgumentException;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueTextMessage;
/**
 * Defines an Sms Broadcast Receiver
 */
public final class SmsReceiver extends BroadcastReceiver {
    private  ContactFactory mContactFactory = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            throw new NullArgumentException("context");
        }
        if (intent == null) {
            throw new NullArgumentException("intent");
        }

        if (mContactFactory == null) {
            mContactFactory = new ContactFactory(context);
        }

        SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        for (SmsMessage smsMessage : smsMessages) {
            if (BuildConfig.DEBUG && (smsMessage == null)) {
                throw new AssertionError("smsMessage == null");
            }
            //Starting the DialogueIncomingDispatcher for each received message
            DialogueMessage dialogueMessage = convertFromSmsMessage(smsMessage);
            DialogueIncomingDispatcher.receiveMessage(context, dialogueMessage);
        }
    }
    private DialogueTextMessage convertFromSmsMessage(SmsMessage smsMessage) {
        Contact contact = mContactFactory.contactFromNumber(smsMessage.getDisplayOriginatingAddress());
        String stringBody = smsMessage.getMessageBody();

        return new DialogueTextMessage(contact, stringBody, DialogueMessage.MessageStatus.INCOMING);
    }


}
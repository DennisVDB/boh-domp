package ch.epfl.sweng.bohdomp.dialogue.channels.sms;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;


import ch.epfl.sweng.bohdomp.dialogue.BuildConfig;
import ch.epfl.sweng.bohdomp.dialogue.channels.DialogueIncomingDispatcher;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.ContactFactory;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.InvalidNumberException;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.NullArgumentException;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueTextMessage;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * Defines an Sms Broadcast Receiver
 */
public final class SmsReceiver extends BroadcastReceiver {
    private  ContactFactory mContactFactory;

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

        String messageBody = "";

        for (SmsMessage smsMessage : smsMessages) {
            if (BuildConfig.DEBUG && (smsMessage == null)) {
                throw new AssertionError("smsMessage == null");
            }

            messageBody += smsMessage.getMessageBody();
        }

        sendToIncomingDispatcher(context, messageBody, smsMessages[0].getOriginatingAddress());
    }

    private void sendToIncomingDispatcher(Context context, String messageBody, String phoneNumber) {
        Contract.throwIfArgNull(context, "context");
        Contract.throwIfArgNull(messageBody, "messageBody");
        Contract.throwIfArgNull(phoneNumber, "phoneNumber");

        try {
            DialogueMessage dialogueMessage = convertFromSmsMessage(messageBody, phoneNumber);
            DialogueIncomingDispatcher.receiveMessage(context, dialogueMessage);

        } catch (InvalidNumberException e) {
            Toast.makeText(context, "Incoming message from strange address: "
                    + phoneNumber, Toast.LENGTH_LONG).show();
        }
    }

    private DialogueTextMessage convertFromSmsMessage(String messageBody, String phoneNumber)
        throws InvalidNumberException {
        Contract.throwIfArgNull(messageBody, "messageBody");
        Contract.throwIfArgNull(phoneNumber, "phoneNumber");

        Contact contact = mContactFactory.contactFromNumber(phoneNumber);

        return new DialogueTextMessage(contact, messageBody, DialogueMessage.MessageStatus.INCOMING);
    }
}
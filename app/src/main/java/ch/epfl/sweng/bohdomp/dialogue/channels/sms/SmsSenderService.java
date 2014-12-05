package ch.epfl.sweng.bohdomp.dialogue.channels.sms;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import java.util.ArrayList;

import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.PhoneNumber;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * The sms sender service sends the message passed to it via the intent and handles the different errors
 * that can occur when sending a sms.
 */
public class SmsSenderService extends IntentService {
    public static final String ACTION_SEND_SMS = "SEND_SMS";

    private BroadcastReceiver mSentBroadcastReceiver = new SmsSentBroadcastReceiver();
    private BroadcastReceiver mDeliveryBroadcastReceiver = new SmsDeliveryBroadcastReceiver();

    private SmsManager mSmsManager = SmsManager.getDefault();

    public SmsSenderService() {
        super("SmsSenderService");
    }

    /**
     * Sends the message passed through the intent.
     *
     * @param intent containing the message to be sent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Contract.assertNotNull(intent, "intent");

        if (intent.getAction().equals(ACTION_SEND_SMS)) {
            sendMessage(DialogueMessage.extractMessage(intent));
        }
    }

    private void sendMessage(DialogueMessage message) {
        Contract.assertNotNull(message, "message");

        if (!needsPartitioning(message)) {
            sendMonoPartMessage(message);
        } else {
            ArrayList<String> messages = mSmsManager.divideMessage(message.getBody().getMessageBody());

            PhoneNumber number = message.getPhoneNumber();
            sendMultiPartMessage(message, messages, number);
        }
    }

    private void sendMultiPartMessage(DialogueMessage message, ArrayList<String> messages, PhoneNumber number) {
        Contract.assertNotNull(message, "message");
        Contract.assertNotNull(messages, "messages");

        Contract.assertNotNull(number, "number");

        mSentBroadcastReceiver = new SmsSentBroadcastReceiver(messages.size());
        mDeliveryBroadcastReceiver = new SmsDeliveryBroadcastReceiver(messages.size());

        registerReceiver(mDeliveryBroadcastReceiver,
                new IntentFilter(SmsDeliveryBroadcastReceiver.ACTION_SMS_DELIVERED));
        registerReceiver(mSentBroadcastReceiver,
                new IntentFilter(SmsSentBroadcastReceiver.ACTION_SMS_SENT));

        mSmsManager.sendMultipartTextMessage(number.getNumber(), null, messages,
                getSentPendingIntentList(message),
                getDeliveredPendingIntentList(message));
    }

    private void sendMonoPartMessage(DialogueMessage message) {
        Contract.assertNotNull(message, "message");

        String phoneNumber = message.getPhoneNumber().getNumber();
        String messageBody = message.getBody().getMessageBody();

        mSentBroadcastReceiver = new SmsDeliveryBroadcastReceiver();
        mDeliveryBroadcastReceiver = new SmsDeliveryBroadcastReceiver();

        registerReceiver(mDeliveryBroadcastReceiver,
                new IntentFilter(SmsDeliveryBroadcastReceiver.ACTION_SMS_DELIVERED));
        registerReceiver(mSentBroadcastReceiver,
                new IntentFilter(SmsSentBroadcastReceiver.ACTION_SMS_SENT));

        mSmsManager.sendTextMessage(phoneNumber, null, messageBody,
                getSentPendingIntent(message), getDeliveryPendingIntent(message));
    }

    private boolean needsPartitioning(DialogueMessage message) {
        Contract.assertNotNull(message, "message");

        return message.getBody().getMessageBody().getBytes().length > SmsMessage.MAX_USER_DATA_BYTES;
    }

    @Override
    public void onDestroy() {
        Contract.assertNotNull(mSentBroadcastReceiver, "mSentBroadcastReceiver");
        Contract.assertNotNull(mDeliveryBroadcastReceiver, "mDeliveryBroadcastReceiver");

        try {
            unregisterReceiver(mSentBroadcastReceiver);
            unregisterReceiver(mDeliveryBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            //It can happen only during testing
        }

        super.onDestroy();
    }


    private PendingIntent getSentPendingIntent(DialogueMessage message) {
        Contract.assertNotNull(message, "message");

        Bundle bundle = new Bundle();

        Intent intent = new Intent(getApplicationContext(), SmsSentBroadcastReceiver.class);
        intent.setAction(SmsSentBroadcastReceiver.ACTION_SMS_SENT);
        intent.putExtra(DialogueMessage.MESSAGE, message);

        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return sentPendingIntent;
    }

    private PendingIntent getDeliveryPendingIntent(DialogueMessage message) {
        Contract.assertNotNull(message, "message");

        Intent intent = new Intent(getApplicationContext(), SmsDeliveryBroadcastReceiver.class);
        intent.setAction(SmsDeliveryBroadcastReceiver.ACTION_SMS_DELIVERED);
        intent.putExtra(DialogueMessage.MESSAGE, message);

        PendingIntent deliveryPendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return deliveryPendingIntent;
    }

    /*
    Needs only one because the API is weird.
     */
    private ArrayList<PendingIntent> getSentPendingIntentList(DialogueMessage message) {
        ArrayList<PendingIntent> list = new ArrayList<PendingIntent>();

        list.add(getSentPendingIntent(message));

        return list;
    }

    /*
    Needs only one because the API is weird.
     */
    private ArrayList<PendingIntent> getDeliveredPendingIntentList(DialogueMessage message) {
        Contract.throwIfArgNull(message, "message");

        ArrayList<PendingIntent> list = new ArrayList<PendingIntent>();

        list.add(getDeliveryPendingIntent(message));

        return list;
    }
}

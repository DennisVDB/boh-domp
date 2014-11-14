package ch.epfl.sweng.bohdomp.dialogue.channels.sms;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;


import ch.epfl.sweng.bohdomp.dialogue.BuildConfig;
import ch.epfl.sweng.bohdomp.dialogue.R;
import ch.epfl.sweng.bohdomp.dialogue.channels.DialogueIncomingDispatcher;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.ContactFactory;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.NullArgumentException;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueTextMessage;
import ch.epfl.sweng.bohdomp.dialogue.ui.contactList.ConversationListActivity;

/**
 * Defines an Sms Broadcast Receiver
 */
public final class SmsReceiver extends BroadcastReceiver {
    private ContactFactory mContactFactory = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            throw new NullArgumentException("context");
        }
        if (intent == null) {
            throw new NullArgumentException("intent");
        }

        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setSmallIcon(R.drawable.red_circle_unread);
        notificationBuilder.setContentTitle("New message bitchheessss");
        notificationBuilder.setContentText("YEPPAAAA");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, ConversationListActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ConversationListActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        notificationBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, notificationBuilder.build());

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
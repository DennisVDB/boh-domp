package ch.epfl.sweng.bohdomp.dialogue.channels.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsManager;

import ch.epfl.sweng.bohdomp.dialogue.data.DefaultDialogData;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * The "sms sent" broadcast receiver handles the result code (the message was sent or not) returned
 * by the sms manager when sending a sms message.
 */
public final class SmsSentBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsSentBroadcastReceiver";

    public static final String ACTION_SMS_SENT = "SMS_SENT";

    private int mNParts;
    private int partsReceived = 0;
    private boolean hasSucceeded = true;

    /**
     * Constructor for a message that can be
     * sent in one part.
     */
    public SmsSentBroadcastReceiver() {
        super();

        this.mNParts = 1;
    }

    /**
     * Constructor for a message that needs
     * to be sent un multiple parts.
     *
     * @param nParts to be acknowledged.
     */
    public SmsSentBroadcastReceiver(int nParts) {
        super();

        Contract.throwIfArg(nParts <= 0, "Need a least 1 part");

        this.mNParts = nParts;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Contract.throwIfArgNull(context, "context");
        Contract.throwIfArgNull(intent, "intent");

        if (intent.getAction().equals(ACTION_SMS_SENT)) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                case SmsManager.RESULT_ERROR_NULL_PDU:
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    hasSucceeded = false;
                    break;
                default:
                    break;
            }

            partsReceived += 1;

            if ((partsReceived == mNParts) && hasSucceeded) {
                DialogueMessage message = DialogueMessage.extractMessage(intent);
                DefaultDialogData.getInstance().setMessageStatus(message, DialogueMessage.MessageStatus.SENT);

                writeToSmsProvider(context, message);
            }
        }
    }

    private void writeToSmsProvider(Context context, DialogueMessage message) {
        ContentValues values = new ContentValues();

        values.put("address", message.getPhoneNumber().getNumber());
        values.put("body", message.getPlainTextBody().getMessageBody());

        context.getContentResolver().insert(Telephony.Sms.Sent.CONTENT_URI, values);
    }
}

package ch.epfl.sweng.bohdomp.dialogue.channels.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ch.epfl.sweng.bohdomp.dialogue.data.DefaultDialogData;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * The "sms delivery" broadcast receiver handles the result code (the message was delivered or not)
 * return by the sms manager when sending a sms message.
 */
public final class SmsDeliveryBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_SMS_DELIVERED = "SMS_DELIVERED";

    private int mNParts;
    private int partsReceived = 0;

    private boolean hasSucceeded = true;

    /**
     * Constructor for a message that can be
     * sent in one part.
     */
    public SmsDeliveryBroadcastReceiver() {
        super();

        this.mNParts = 1;
    }

    /**
     * Constructor for a message that needs
     * to be sent un multiple parts.
     *
     * @param nParts to be acknowledged.
     */
    public SmsDeliveryBroadcastReceiver(int nParts) {
        super();

        Contract.throwIfArg(nParts <= 0, "Need a least 1 part");

        this.mNParts = nParts;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Contract.throwIfArgNull(context, "context");
        Contract.throwIfArgNull(intent, "intent");

        if (intent.getAction().equals(ACTION_SMS_DELIVERED)) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    break;
                case Activity.RESULT_CANCELED:
                    hasSucceeded = false;
                    break;
                default:
                    break;
            }

            partsReceived += 1;

            if (partsReceived == mNParts) {
                if (hasSucceeded) {


                    DialogueMessage message = DialogueMessage.extractMessage(intent);
                    DefaultDialogData.getInstance().setMessageStatus(message, DialogueMessage.MessageStatus.DELIVERED);
                }
            }
        }
    }
}

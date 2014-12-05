package ch.epfl.sweng.bohdomp.dialogue.channels;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import ch.epfl.sweng.bohdomp.dialogue.channels.sms.SmsSenderService;
import ch.epfl.sweng.bohdomp.dialogue.data.DefaultDialogData;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.EncryptedDialogueTextMessage;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * Dispatches the outgoing message to the channels.
 */
public final class DialogueOutgoingDispatcher extends IntentService {
    private static final String ACTION_SEND_MESSAGE = "ACTION_SEND_MESSAGE";
    private static final String EXTRA_ENCRYPT = "ENCRYPT";

    public DialogueOutgoingDispatcher() {
        super("DialogueOutgoingDispatcher");
    }

    /**
     * Handles the sending of messages.
     *
     * @param context of the application.
     * @param message to be sent.
     */
    public static void sendMessage(Context context, DialogueMessage message, boolean encrypt) {
        Contract.throwIfArgNull(context, "context");
        Contract.throwIfArgNull(message, "message");
        Contract.throwIfArg(message.getDirection() == DialogueMessage.MessageDirection.INCOMING,
                "Wrong message direction");

        /* Create intent and send to myself */
        Intent intent = new Intent(context, DialogueOutgoingDispatcher.class);
        intent.setAction(ACTION_SEND_MESSAGE);
        intent.putExtra(DialogueMessage.MESSAGE, message);
        intent.putExtra(DialogueOutgoingDispatcher.EXTRA_ENCRYPT, encrypt);
        context.startService(intent);
    }

    private DialogueMessage makeEncrypted(DialogueMessage original, boolean shouldEncrypt) {
        if (shouldEncrypt) {
            return new EncryptedDialogueTextMessage(
                    getApplicationContext(), original.getContact(), original.getChannel(),
                    original.getPhoneNumber(), original.getBody().getMessageBody(), original.getDirection());
        } else {
            return original;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Contract.throwIfArgNull(intent, "intent");

        if (intent.getAction().equals(ACTION_SEND_MESSAGE)) {
            boolean shouldEncrypt = intent.getExtras().getBoolean(EXTRA_ENCRYPT);

            DialogueMessage original = DialogueMessage.extractMessage(intent);
            DialogueMessage outgoing = makeEncrypted(original, shouldEncrypt);

            DefaultDialogData.getInstance().addMessageToConversation(original);
            dispatchMessage(outgoing);
        }
    }

    private void dispatchMessage(DialogueMessage message) {
        switch (message.getChannel()) {
            case SMS:
                dispatchSms(message);
                break;
            default:
                throw new IllegalStateException("not valid channel");
        }
    }

    private void dispatchSms(final DialogueMessage message) {
        Contract.assertNotNull(message, "message");

        /* Create intent and send to SmsSenderService */
        Intent intent = new Intent(getApplicationContext(), SmsSenderService.class);
        intent.setAction(SmsSenderService.ACTION_SEND_SMS);
        intent.putExtra(DialogueMessage.MESSAGE, message);
        getApplicationContext().startService(intent);
    }

    private void sendMms(DialogueMessage message) {
        Contract.assertNotNull(message, "message");

        /* Create intent */
        /* Send intent to service */
    }
}



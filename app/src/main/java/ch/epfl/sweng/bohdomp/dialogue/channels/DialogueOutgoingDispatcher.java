package ch.epfl.sweng.bohdomp.dialogue.channels;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import ch.epfl.sweng.bohdomp.dialogue.channels.sms.SmsSenderService;
import ch.epfl.sweng.bohdomp.dialogue.crypto.CryptoService;
import ch.epfl.sweng.bohdomp.dialogue.crypto.KeyManager;
import ch.epfl.sweng.bohdomp.dialogue.data.DefaultDialogData;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueTextMessage;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * Dispatches the outgoing message to the channels.
 */
public final class DialogueOutgoingDispatcher extends IntentService {
    private static final String ACTION_SEND_MESSAGE = "ACTION_SEND_MESSAGE";
    private static final String MUST_BE_ENCRYPTED = "ENCRYPT";

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
        intent.putExtra(MUST_BE_ENCRYPTED, encrypt);
        intent.putExtra(DialogueMessage.MESSAGE, message);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Contract.throwIfArgNull(intent, "intent");

        if (intent.getAction().equals(ACTION_SEND_MESSAGE)) {
            DialogueMessage message = DialogueMessage.extractMessage(intent);

            DefaultDialogData.getInstance().addMessageToConversation(message);
            Boolean encrypt = intent.getBooleanExtra(MUST_BE_ENCRYPTED, false);
            switch (message.getChannel()) {
                case SMS:
                    sendSms(message, encrypt);
                    break;
                default:
                    throw new IllegalStateException("not valid channel");
            }
        }

    }

    private void sendSms(final DialogueMessage message, boolean crypt) {
        Contract.assertNotNull(message, "message");


        Log.i("DialogueOutgoingDispatcher", "3");

        if (crypt) {

            CryptoService.startActionEncrypt(getApplicationContext(), KeyManager.FINGERPRINT,
                    message.getBody().getMessageBody(),

                    new ResultReceiver(null) {
                        @Override
                        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                            if (resultCode == CryptoService.RESULT_SUCCESS) {
                                String encryptedText = resultData.getString(CryptoService.EXTRA_ENCRYPTED_TEXT);
                                DialogueMessage encryptedMessage = new DialogueTextMessage(message.getContact(),
                                        message.getChannel(), message.getPhoneNumber(),
                                        encryptedText, DialogueMessage.MessageDirection.OUTGOING);

                                sendMessage(encryptedMessage);
                            }
                        }
                    });
        } else {
            sendMessage(message);
        }
    }

    private void sendMessage(DialogueMessage message) {
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



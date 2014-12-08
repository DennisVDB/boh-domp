package ch.epfl.sweng.bohdomp.dialogue.channels;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import ch.epfl.sweng.bohdomp.dialogue.crypto.CryptoService;
import ch.epfl.sweng.bohdomp.dialogue.data.DefaultDialogData;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueTextMessage;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * Handles the incoming messages.
 */
public final class DialogueIncomingDispatcher extends IntentService {
    public static final String ACTION_RECEIVE_MESSAGE = "ACTION_RECEIVE_MESSAGE";

    private static boolean sIsRunning;

    public DialogueIncomingDispatcher() {
        super("DialogueIncomingDispatcher");
    }

    /**
     * Handles the incoming messages.
     * @param context of the application.
     * @param message to be received.
     */
    public static void receiveMessage(final Context context, final DialogueMessage message) {
        Contract.throwIfArgNull(context, "context");
        Contract.throwIfArgNull(message, "message");
        Contract.throwIfArg(message.getDirection() == DialogueMessage.MessageDirection.OUTGOING,
                "An outgoing message should not arrive to the incoming dispatcher");

        CryptoService.startActionDecrypt(context, message.getBody().getMessageBody(), new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == CryptoService.RESULT_SUCCESS) {
                    String decryptedText = resultData.getString(CryptoService.EXTRA_CLEAR_TEXT);
                    DialogueMessage decryptedMessage = new DialogueTextMessage(message.getContact(),
                            message.getChannel(), message.getPhoneNumber(),
                            decryptedText, DialogueMessage.MessageDirection.INCOMING);

                    /* Create intent and send to myself */
                    Intent intent = new Intent(context, DialogueIncomingDispatcher.class);
                    intent.setAction(ACTION_RECEIVE_MESSAGE);
                    intent.putExtra(DialogueMessage.MESSAGE, decryptedMessage);
                    context.startService(intent);
                }
            }
        });
    }

    public static boolean isRunning() {
        return sIsRunning;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Contract.throwIfArgNull(intent, "intent");

        if (intent.getAction().equals(ACTION_RECEIVE_MESSAGE)) {

            DialogueMessage message = DialogueMessage.extractMessage(intent);

            Notificator notificator = new Notificator(getApplicationContext());
            notificator.update(message);

            DefaultDialogData.getInstance().addMessageToConversation(message);

            sIsRunning = true;
        }
        //ignore when receiving other commands
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sIsRunning = false;

    }
}
package ch.epfl.sweng.bohdomp.dialogue.channels;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import org.bouncycastle.openpgp.PGPException;

import java.io.IOException;

import ch.epfl.sweng.bohdomp.dialogue.crypto.Crypto;
import ch.epfl.sweng.bohdomp.dialogue.crypto.openpgp.IncorrectPassphraseException;
import ch.epfl.sweng.bohdomp.dialogue.data.DefaultDialogData;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueTextMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.TextMessageBody;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * Handles the incoming messages.
 */
public final class DialogueIncomingDispatcher extends IntentService{
    public static final String ACTION_RECEIVE_MESSAGE = "ACTION_RECEIVE_MESSAGE";

    private static boolean sIsRunning;


    private Notificator mNotificator;

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

        /* Create intent and send to myself */
        Intent intent = new Intent(context, DialogueIncomingDispatcher.class);
        intent.setAction(ACTION_RECEIVE_MESSAGE);
        intent.putExtra(DialogueMessage.MESSAGE, message);
        context.startService(intent);
    }

    public static boolean isRunning() {
        return sIsRunning;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Contract.throwIfArgNull(intent, "intent");

        if (intent.getAction().equals(ACTION_RECEIVE_MESSAGE)) {

            DialogueMessage message = DialogueMessage.extractMessage(intent);

            try {
                TextMessageBody decryptedBody = Crypto.decrypt(getApplicationContext(),
                        message.getBody().getMessageBody());

                DialogueMessage decryptedMessage = new DialogueTextMessage(message.getContact(),
                        message.getChannel(), message.getPhoneNumber(), decryptedBody.getMessageBody(),
                        message.getDirection());

                mNotificator = new Notificator(getApplicationContext());
                mNotificator.update(message);

                DefaultDialogData.getInstance().addMessageToConversation(decryptedMessage);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (PGPException e) {
                e.printStackTrace();
            } catch (IncorrectPassphraseException e) {
                e.printStackTrace();
            }

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
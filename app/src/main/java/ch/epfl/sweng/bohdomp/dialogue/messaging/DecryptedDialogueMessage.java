package ch.epfl.sweng.bohdomp.dialogue.messaging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.crypto.CryptoService;

/**
 * Created by dennis on 05/12/14.
 */
public class DecryptedDialogueMessage extends DialogueMessage {
    private MessageBody mDecryptedBody;

    public DecryptedDialogueMessage(final Context context, Contact contact, Contact.ChannelType channel, Contact.PhoneNumber phoneNumber,
                                    String messageBody, MessageDirection messageDirection, boolean isDataMessage) {

        super(contact, channel, phoneNumber, messageBody, messageDirection, isDataMessage);

        CryptoService.startActionDecrypt(context, messageBody, new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == CryptoService.RESULT_SUCCESS) {
                    mDecryptedBody = new TextMessageBody(resultData.getString(CryptoService.EXTRA_CLEAR_TEXT));
                }
            }
        });
    }

    @Override
    public MessageBody getPlainTextBody() {
        return mDecryptedBody;
    }

    @Override
    public MessageBody getBody() {
        return super.getBody();
    }

    @Override
    public MessageBody newBody(String body) {
        return new TextMessageBody(body);
    }
}

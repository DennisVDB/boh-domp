package ch.epfl.sweng.bohdomp.dialogue.messaging;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;

import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.crypto.CryptoService;
import ch.epfl.sweng.bohdomp.dialogue.crypto.KeyManager;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * Created by dennis on 05/12/14.
 */
public class EncryptedDialogueTextMessage extends DialogueMessage {
    private TextMessageBody mEncryptedBody;

    public EncryptedDialogueTextMessage(final Context context, Contact contact, Contact.ChannelType channel, Contact.PhoneNumber phoneNumber,
                                        String messageBody, MessageDirection messageDirection) {

        super(contact, channel, phoneNumber, messageBody, messageDirection, false);

        Contract.throwIfArgNull(context, "context");

//        CryptoService.startActionEncrypt(context, KeyManager.FINGERPRINT, messageBody,
//                new ResultReceiver(null) {
//                    @Override
//                    protected void onReceiveResult(final int resultCode, final Bundle resultData) {
//                        if (resultCode == CryptoService.RESULT_SUCCESS) {
//                            mEncryptedBody = new TextMessageBody(resultData.getString(CryptoService.EXTRA_ENCRYPTED_TEXT));
//                        }
//                    }
//                });

        this.mEncryptedBody = new TextMessageBody("Fuck");
    }

    @Override
    public MessageBody getBody() {
        Contract.assertNotNull(mEncryptedBody, "body2");
        return mEncryptedBody;
    }

    @Override
    public MessageBody getPlainTextBody() {
        return super.getPlainTextBody();
    }

    @Override
    public MessageBody newBody(String body) {
        return new TextMessageBody(body);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(mEncryptedBody, flags);
    }

    EncryptedDialogueTextMessage(Parcel in) {
        super(in);

        this.mEncryptedBody = in.readParcelable(TextMessageBody.class.getClassLoader());
    }

    public static final Parcelable.Creator<EncryptedDialogueTextMessage> CREATOR =
            new Parcelable.Creator<EncryptedDialogueTextMessage>() {
                public EncryptedDialogueTextMessage createFromParcel(Parcel source) {
                    return new EncryptedDialogueTextMessage(source);
                }

                public EncryptedDialogueTextMessage[] newArray(int size) {
                    return new EncryptedDialogueTextMessage[size];
                }
            };
}

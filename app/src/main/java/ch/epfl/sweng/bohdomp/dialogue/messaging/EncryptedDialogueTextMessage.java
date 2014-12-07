package ch.epfl.sweng.bohdomp.dialogue.messaging;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.crypto.Crypto;
import ch.epfl.sweng.bohdomp.dialogue.crypto.KeyManager;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * Encrypted version of a DialogueTextMessage.
 * The message is encrypted only when it is first needed.
 */
public class EncryptedDialogueTextMessage extends DialogueMessage {
    public static final Parcelable.Creator<EncryptedDialogueTextMessage> CREATOR =
            new Parcelable.Creator<EncryptedDialogueTextMessage>() {
                public EncryptedDialogueTextMessage createFromParcel(Parcel source) {
                    return new EncryptedDialogueTextMessage(source);
                }

                public EncryptedDialogueTextMessage[] newArray(int size) {
                    return new EncryptedDialogueTextMessage[size];
                }
            };
    private Context mContext;
    private String mMessageBody;
    private TextMessageBody mEncryptedBody;
    private boolean mHasBeenEncrypted;

    public EncryptedDialogueTextMessage(Context context, Contact contact, Contact.ChannelType channel,
                                        Contact.PhoneNumber phoneNumber, String messageBody,
                                        MessageDirection messageDirection) {

        super(contact, channel, phoneNumber, messageBody, messageDirection, false);

        Contract.throwIfArgNull(context, "context");

        this.mContext = context;
        this.mMessageBody = messageBody;
        this.mHasBeenEncrypted = false;
    }

    private EncryptedDialogueTextMessage(Parcel in) {
        super(in);

        this.mContext = null; // don't need anymore after encryption
        this.mMessageBody = null; // don't need anymore after encryption
        this.mEncryptedBody = in.readParcelable(TextMessageBody.class.getClassLoader());
        this.mHasBeenEncrypted = in.readByte() != 0;
    }

    @Override
    public MessageBody getBody() {
        /* Encrypt the 1st time only */
        if (!mHasBeenEncrypted) {
            encryptBody();
        }

        return mEncryptedBody;
    }

    private void encryptBody() {
        mEncryptedBody = Crypto.encrypt(mContext, mMessageBody, KeyManager.FINGERPRINT);
        mHasBeenEncrypted = true;
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
        /*
        We can't put the context needed to encrypt the body
        to the parcel therefore we encrypt the body when we
        need to parcel it and then pass a null mContext and null
        mMessageBody because after the first encryption we don't need
        them anymore.
         */
        if (!mHasBeenEncrypted) {
            encryptBody();
        }

        super.writeToParcel(dest, flags);

        // Null mContext
        // Null mMessageBody
        dest.writeParcelable(mEncryptedBody, flags);
        dest.writeByte(mHasBeenEncrypted ? (byte) 1 : (byte) 0);
    }
}

package ch.epfl.sweng.bohdomp.dialogue.messaging;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

import ch.epfl.sweng.bohdomp.dialogue.conversation.ChannelType;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.PhoneNumber;

/**
 * Class representing a dialogue text message
 */
public final class DialogueTextMessage extends DialogueMessage {
    public DialogueTextMessage(Contact contact, ChannelType channel, PhoneNumber number,
                               String textBody, MessageDirection messageDirection) {
        super(contact, channel, number, textBody, messageDirection, false);
    }

    public DialogueTextMessage(Contact contact, ChannelType channel, PhoneNumber number,
                               String textBody, MessageDirection messageDirection,
                               MessageStatus status, DateTime time) {
        super(contact, channel, number, textBody, messageDirection, status, false, time);
    }

    @Override
    public MessageBody newBody(String body) {
        return new TextMessageBody(body);
    }

    public static final Parcelable.Creator<DialogueTextMessage> CREATOR =
        new Parcelable.Creator<DialogueTextMessage>() {
            public DialogueTextMessage createFromParcel(Parcel source) {
                return new DialogueTextMessage(source);
            }

            public DialogueTextMessage[] newArray(int size) {
                return new DialogueTextMessage[size];
            }
        };

    private DialogueTextMessage(Parcel source) {
        super(source);
    }
}

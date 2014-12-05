package ch.epfl.sweng.bohdomp.dialogue.messaging;


import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.util.Locale;

import ch.epfl.sweng.bohdomp.dialogue.R;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.ids.DialogueMessageId;
import ch.epfl.sweng.bohdomp.dialogue.ids.IdManager;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * Abstract class representing an message. This class is mutable.
 */
public abstract class DialogueMessage implements Parcelable {

    /**
     * Enumeration representing the state of a message.
     */
    public static enum MessageStatus {
        SENT, DELIVERED, IN_TRANSIT, FAILED
    }

    /**
     * Enumeration representing the direction of a message.
     */
    public static enum MessageDirection {
        INCOMING, OUTGOING
    }

    public static final String MESSAGE = "MESSAGE";

    private final Contact mContact;
    private final Contact.ChannelType mChannel;
    private final Contact.PhoneNumber mPhoneNumber;
    private MessageBody mBody;
    private final DateTime mTimestamp;
    private final DialogueMessageId mId;
    private MessageStatus mStatus;
    private MessageDirection mDirection;

    private boolean mIsReadStatus;
    private boolean mIsDataMessage;

    /**
     * Extracts a DialogueMessage from an intent.
     * @param intent containing the message.
     * @return the message contained in the intent.
     */
    public static DialogueMessage extractMessage(Intent intent) {
        Contract.throwIfArgNull(intent, "intent");

        return (DialogueMessage) intent.getExtras().getParcelable(MESSAGE);
    }

    DialogueMessage(Contact contact, Contact.ChannelType channel, Contact.PhoneNumber phoneNumber,
                    String messageBody, MessageDirection messageDirection, boolean isDataMessage) {

        Contract.throwIfArgNull(contact, "contact");
        Contract.throwIfArgNull(messageBody, "message body");
        Contract.throwIfArgNull(messageDirection, "message status");

        this.mContact = contact;
        this.mChannel = channel;
        this.mPhoneNumber = phoneNumber;
        this.mBody = newBody(messageBody);
        this.mTimestamp = new DateTime();
        this.mId = IdManager.getInstance().newDialogueMessageId();
        this.mIsReadStatus = false;
        this.mDirection = messageDirection;
        this.mStatus = MessageStatus.IN_TRANSIT;
        this.mIsDataMessage = isDataMessage;
    }

    /**
     * Getter for the mContact message
     * @return the mContact whose message is
     */
    public Contact getContact() {
        //FIXME:Contact should be immutable
        return mContact;
    }

    public Contact.ChannelType getChannel() {
        return mChannel;
    }

    public Contact.PhoneNumber getPhoneNumber() {
        return mPhoneNumber;
    }

    /**
     * Getter for the body message
     * @return the body message
     */
    public MessageBody getBody() {
        return mBody;
    }

    /**
     * Getter for is read status of message
     * @return true if message has been read , false otherwise
     */
    public boolean getIsReadStatus() {
        return mIsReadStatus;
    }

    /**
     * Getter for the message time stamp
     * @return the time stamp of the message
     */
    public DateTime getTimeStamp() {
        return mTimestamp;
    }

    public MessageBody getPlainTextBody() {
        return getBody();
    }

    /**
     * Give a nice presentation to display the TimeStamp of the message
     * @return String holding a nice presentation of the TimeStamp accordingly to the current time.
     */
    public String prettyTimeStamp(Context context) {
        Contract.throwIfArgNull(context, "context");

        DateTime currentTime = new DateTime();
        DateTime thisMorning = currentTime.withTimeAtStartOfDay();
        DateTime yesterdayMorning = thisMorning.minusDays(1);
        DateTime morning2DaysAgo = thisMorning.minusDays(2);

        Duration elapsedTime = new Duration(mTimestamp, currentTime);

        if (elapsedTime.getStandardMinutes() <= 1) {
            return context.getString(R.string.now);
        }

        if (mTimestamp.withTimeAtStartOfDay().equals(thisMorning)) {
            return mTimestamp.toString("HH:mm", Locale.ENGLISH);
        }

        if (new Interval(yesterdayMorning, thisMorning).contains(mTimestamp)) {
            return context.getString(R.string.yesterday) + ": " + mTimestamp.toString("HH:mm", Locale.ENGLISH);
        }

        if (new Interval(morning2DaysAgo, yesterdayMorning).contains(mTimestamp)) {
            return context.getString(R.string.two_days_ago) + ": " + mTimestamp.toString("HH:mm", Locale.ENGLISH);
        }

        if (!currentTime.toString("yyyy").equals(mTimestamp.toString("yyyy"))) {
            return mTimestamp.toString("dd/MM/yy: HH:mm", Locale.ENGLISH);
        }

        return mTimestamp.toString("dd.MM: HH:mm", Locale.ENGLISH);
    }

    /**
     * Getter for the message status
     * @return the message status.
     */
    public MessageStatus getStatus() {
        return mStatus;
    }

    /**
     * Getter for the message direction.
     * @return the message direction.
     */
    public MessageDirection getDirection() {
        return mDirection;
    }

    /**
     * Getter for the message id
     * @return the message id
     */
    public DialogueMessageId getId() {
        return mId;
    }

    /**
     * Sets a message as read
     */
    public void setAsRead() {
        //FIXME: Decide if we need a listener somewhere or when enter in converstation all messages are read
        mIsReadStatus = true;
    }

    /**
     * Method that returns the allowed channels where we can send the message
     * @return the list of allowed channels
     */
    public boolean getIsDataMessage() {
        return mIsDataMessage;
    }

    /**
     * Sets the message status.
     * @param status to be set to.
     */
    public void setStatus(MessageStatus status) {
        Contract.throwIfArgNull(status, "status");

        this.mStatus = status;
    }

    public void setBody(String body) {
        this.mBody = newBody(body);
    }

    /**
     * Factory method for constructing the corespondent type of message body
     * @param body the string we will put in the body
     * @return the created MessageBody
     */
    public abstract MessageBody newBody(String body);

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Contract.throwIfArgNull(dest, "dest");

        dest.writeParcelable(this.mContact, 0);
        dest.writeParcelable(this.mChannel, 0);
        dest.writeParcelable(this.mPhoneNumber, 0);
        dest.writeParcelable(this.mBody, 0);
        dest.writeLong(this.mTimestamp.getMillis());
        dest.writeParcelable(this.mId, 0);
        dest.writeInt(this.mStatus == null ? -1 : this.mStatus.ordinal());
        dest.writeInt(this.mDirection == null ? -1 : this.mDirection.ordinal());
        dest.writeByte(mIsReadStatus ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsDataMessage ? (byte) 1 : (byte) 0);
    }

    DialogueMessage(Parcel in) {
        Contract.throwIfArgNull(in, "in");

        this.mContact = in.readParcelable(Contact.class.getClassLoader());
        this.mChannel = in.readParcelable(Contact.ChannelType.class.getClassLoader());
        this.mPhoneNumber = in.readParcelable(Contact.PhoneNumber.class.getClassLoader());
        this.mBody = in.readParcelable(MessageBody.class.getClassLoader());
        this.mTimestamp = new DateTime(in.readLong());
        this.mId = in.readParcelable(DialogueMessageId.class.getClassLoader());
        int tmpMessageStatus = in.readInt();
        this.mStatus = tmpMessageStatus == -1 ? null : MessageStatus.values()[tmpMessageStatus];
        int tmpMessageDirection = in.readInt();
        this.mDirection = tmpMessageDirection == -1 ? null : MessageDirection.values()[tmpMessageDirection];
        this.mIsReadStatus = in.readByte() != 0;
        this.mIsDataMessage = in.readByte() != 0;
    }
}

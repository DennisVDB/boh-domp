package ch.epfl.sweng.bohdomp.dialogue.conversation;

import android.content.Context;
import android.os.Parcel;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ch.epfl.sweng.bohdomp.dialogue.R;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.ids.ConversationId;
import ch.epfl.sweng.bohdomp.dialogue.ids.IdManager;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * Class representing a Dialogue conversation. This class is mutable
 */
public final class DialogueConversation implements Conversation {
    public static final String CONVERSATION_ID = "conversationID";

    private final ConversationId mId;
    private final List<Contact> mContact;

    private final List<DialogueMessage> mMessages;
    private List<ConversationListener> mListeners;

    private DateTime mLastActivityTime;

    private Contact.ChannelType mChannel;
    private Contact.PhoneNumber mPhoneNumber;
    private boolean mEncrypt;

    private int mMessageCount;
    private boolean mHasUnread;

    /**
     * Constructor of the class
     * @param contacts - set of contacts we add to conversation
     */
    public DialogueConversation(List<Contact> contacts) {
        Contract.throwIfArgNull(contacts, "contacts");
        Contract.throwIfArg(contacts.size() == 0, "Must have at least one contact");
        Contract.throwIfArg(contacts.contains(null), "There is a null contact");

        this.mId = IdManager.getInstance().newConversationId();
        this.mContact = new ArrayList<Contact>(contacts);
        this.mChannel = null;
        this.mEncrypt = false;
        this.mPhoneNumber = null;
        this.mMessages = new ArrayList<DialogueMessage>();
        this.mListeners = new ArrayList<ConversationListener>();
        this.mMessageCount = 0;
        this.mLastActivityTime = new DateTime();
        this.mHasUnread = false;
    }

    @Override
    public ConversationId getId() {
        return mId;
    }

    @Override
    public String getName() {
        return mContact.get(0).getDisplayName();
    }


    @Override
    public List<Contact> getContacts() {
        return new ArrayList<Contact>(mContact);
    }

    @Override
    public void setChannel(Contact.ChannelType channel) {
        Contract.throwIfArgNull(channel, "channel");

        this.mChannel = channel;
        notifyListeners();
    }

    @Override
    public Contact.ChannelType getChannel() {
        return mChannel;
    }

    @Override
    public void setPhoneNumber(Contact.PhoneNumber phone) {
        Contract.throwIfArgNull(phone, "phone number");

        this.mPhoneNumber = phone;
        notifyListeners();
    }

    @Override
    public Contact.PhoneNumber getPhoneNumber() {
        return mPhoneNumber;
    }

    @Override
    public void setEncrypt(boolean encrypt) {
        this.mEncrypt = encrypt;
        notifyListeners();
    }

    @Override
    public Boolean getEncrypt() {
        return mEncrypt;
    }

    @Override
    public List<DialogueMessage> getMessages() {
        return new ArrayList<DialogueMessage>(mMessages);
    }

    @Override
    public DateTime getLastActivityTime() {
        return mLastActivityTime;
    }

    @Override
    public String getLastConversationActivityString(Context context) {
        Contract.throwIfArgNull(context, "context");

        DateTime currentTime = new DateTime();
        DateTime thisMorning = currentTime.withTimeAtStartOfDay();
        DateTime yesterdayMorning = thisMorning.minusDays(1);
        DateTime morning2DaysAgo = thisMorning.minusDays(2);

        if (mLastActivityTime.withTimeAtStartOfDay().equals(thisMorning)) {
            return mLastActivityTime.toString("HH:mm", Locale.ENGLISH);
        }

        if (new Interval(yesterdayMorning, thisMorning).contains(mLastActivityTime)) {
            return context.getString(R.string.yesterday);
        }

        if (new Interval(morning2DaysAgo, yesterdayMorning).contains(mLastActivityTime)) {
            return context.getString(R.string.two_days_ago);
        }

        if (!currentTime.toString("yyyy").equals(mLastActivityTime.toString("yyyy"))) {
            return mLastActivityTime.toString("MM/yy", Locale.ENGLISH);
        }


        if (!currentTime.toString("ww").equals(mLastActivityTime.toString("ww"))) {
            return mLastActivityTime.toString("dd.MM", Locale.ENGLISH);
        }

        int indexWeekDay = mLastActivityTime.getDayOfWeek() - 1;
        return context.getResources().getStringArray(R.array.days_of_week)[indexWeekDay];
    }

    @Override
    public int getMessageCount() {
        return mMessageCount;
    }

    @Override
    public void setMessageStatus(DialogueMessage message, DialogueMessage.MessageStatus status) {
        Contract.throwIfArgNull(message, "message");
        Contract.throwIfArgNull(status, "status");

        for (DialogueMessage m : mMessages) {
            if (m.getId().equals(message.getId()) && m.getStatus() != status) {
                m.setStatus(status);
                notifyListeners();
            }
        }
    }


    @Override
    public boolean hasUnread() {
        return mHasUnread;
    }


    @Override
    public void addContact(Contact contact) {
        Contract.throwIfArgNull(contact, "contact");

        mContact.add(contact);
        notifyListeners();
    }

    @Override
    public void removeContact(Contact contact) {
        Contract.throwIfArgNull(contact, "contact");

        if (mContact.contains(contact)) {
            mContact.remove(contact);
            notifyListeners();
        }
    }

    @Override
    public void addMessage(DialogueMessage message) {
        Contract.throwIfArgNull(message, "message");

        if (message.getDirection() == DialogueMessage.MessageDirection.INCOMING) {
            mHasUnread = true;
        }

        mMessages.add(message);
        mMessageCount += 1;

        mLastActivityTime = new DateTime();

        notifyListeners();
    }

    @Override
    public void addListener(ConversationListener listener) {
        Contract.throwIfArgNull(listener, "listener");

        if (mListeners == null) {
            mListeners = new ArrayList<ConversationListener>();
        }
        mListeners.add(listener);
    }

    @Override
    public void removeListener(ConversationListener listener) {
        Contract.throwIfArgNull(listener, "listener");

        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    @Override
    public List<ConversationListener> getListeners() {
        return new ArrayList<ConversationListener>(mListeners);
    }

    @Override
    public boolean getHasUnread() {
        return mHasUnread;
    }

    @Override
    public void setAllMessagesAsRead() {
        mHasUnread = false;
        notifyListeners();
    }

    //Method that notifies listeners when a change in conversation occurs
    private void notifyListeners() {
        if (mListeners != null) {
            for (ConversationListener listener : mListeners) {
                listener.onConversationChanged(mId);
            }
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Contract.throwIfArgNull(dest, "dest");

        dest.writeParcelable(this.mId, flags);
        dest.writeList(mContact);
        dest.writeParcelable(mChannel, flags);
        dest.writeParcelable(mPhoneNumber, flags);
        dest.writeByte((byte) (mEncrypt ? 1 : 0));
        dest.writeList(mMessages);
        dest.writeLong(this.mLastActivityTime.getMillis());
        dest.writeInt(this.mMessageCount);
        dest.writeByte(mHasUnread ? (byte) 1 : (byte) 0);

    }

    @SuppressWarnings("unchecked") // we cannot solve this unchecked problem!
    private DialogueConversation(Parcel in) {
        this.mId = in.readParcelable(ConversationId.class.getClassLoader());
        this.mContact = in.readArrayList(Contact.class.getClassLoader());
        this.mChannel = in.readParcelable(Contact.ChannelType.class.getClassLoader());
        this.mPhoneNumber = in.readParcelable(Contact.PhoneNumber.class.getClassLoader());
        this.mEncrypt = in.readByte() != 0;
        this.mMessages =  in.readArrayList(DialogueMessage.class.getClassLoader());
        this.mLastActivityTime = new DateTime(in.readLong());
        this.mMessageCount = in.readInt();
        this.mHasUnread = in.readByte() != 0;
    }

    public static final Creator<DialogueConversation> CREATOR = new Creator<DialogueConversation>() {
        public DialogueConversation createFromParcel(Parcel source) {
            Contract.throwIfArgNull(source, "source");

            return new DialogueConversation(source);
        }

        public DialogueConversation[] newArray(int size) {
            return new DialogueConversation[size];
        }
    };
}

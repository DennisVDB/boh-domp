package ch.epfl.sweng.bohdomp.dialogue.conversation.contact;

import android.content.Context;
import android.os.Parcel;
import android.telephony.PhoneNumberUtils;

import java.util.HashSet;
import java.util.Set;

import ch.epfl.sweng.bohdomp.dialogue.exceptions.InvalidNumberException;

/**
 * class representing a contact for which no entry was found in the contact database
 */
public class UnknownContact implements Contact {

    private final String mPhoneNumber;

    public UnknownContact(final String phoneNumber) {
        this.mPhoneNumber = phoneNumber;
    }

    @Override
    public String getDisplayName() {
        return "unknown: " + mPhoneNumber;
    }

    @Override
    public Set<String> getPhoneNumbers() {
        Set<String> result = new HashSet<String>();
        result.add(mPhoneNumber);
        return result;
    }

    @Override
    public Set<ChannelType> availableChannels() {
        Set<ChannelType> result = new HashSet<ChannelType>();
        result.add(ChannelType.SMS);
        return result;
    }

    @Override
    public Contact updateInfo(final Context context) throws InvalidNumberException {
        return new ContactFactory(context).contactFromNumber(mPhoneNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        /*
         * from the android api at:
         * http://developer.android.com/reference/android/telephony/PhoneNumberUtils.html
         *
         * public static boolean compare (String a, String b)
         * Compare phone numbers a and b, return true if they're identical enough for caller ID purposes.
         */
        return PhoneNumberUtils.compare(this.mPhoneNumber, ((UnknownContact) o).mPhoneNumber);
    }

    @Override
    public int hashCode() {
        return PhoneNumberUtils.toCallerIDMinMatch(this.mPhoneNumber).hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPhoneNumber);
    }

    private UnknownContact(Parcel in) {
        this.mPhoneNumber = in.readString();
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        public UnknownContact createFromParcel(Parcel source) {
            return new UnknownContact(source);
        }

        public UnknownContact[] newArray(int size) {
            return new UnknownContact[size];
        }
    };
}

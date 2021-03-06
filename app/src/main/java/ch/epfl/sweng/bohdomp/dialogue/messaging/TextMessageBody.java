package ch.epfl.sweng.bohdomp.dialogue.messaging;

import android.os.Parcel;
import android.os.Parcelable;

import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;


/**
 *  Class representing the mBody of a text message
 */
public final class TextMessageBody implements MessageBody{
    private final String mBody;

    public TextMessageBody(String body) {
        Contract.throwIfArgNull(body, "body");

        this.mBody = body;
    }

    @Override
    public String getMessageBody() {
        return mBody;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Contract.throwIfArgNull(dest, "dest");

        dest.writeString(this.mBody);
    }

    public static final Parcelable.Creator<TextMessageBody> CREATOR = new Parcelable.Creator<TextMessageBody>() {
        public TextMessageBody createFromParcel(Parcel source) {
            return new TextMessageBody(source);
        }

        public TextMessageBody[] newArray(int size) {
            return new TextMessageBody[size];
        }
    };

    private TextMessageBody(Parcel in) {
        Contract.throwIfArgNull(in, "in");

        this.mBody = in.readString();
    }
}

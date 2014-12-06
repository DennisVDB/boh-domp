package ch.epfl.sweng.bohdomp.dialogue.messaging;

import android.content.Intent;
import android.os.Parcel;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.ContactFactory;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.InvalidNumberException;

public class EncryptedDialogueTextMessageTest extends AndroidTestCase {
    public void testParcelRoundTrip() throws InvalidNumberException {
        Contact contact = new ContactFactory(getContext()).contactFromNumber("1234");

        List<Contact> contactList = new ArrayList<>();
        contactList.add(contact);

        Contact.PhoneNumber phoneNumber = (Contact.PhoneNumber) contact.getPhoneNumbers(Contact.ChannelType.SMS).toArray()[0];
        EncryptedDialogueTextMessage message = new EncryptedDialogueTextMessage(getContext(), contact, Contact.ChannelType.SMS, phoneNumber, "test",
                DialogueMessage.MessageDirection.OUTGOING);

        Parcel parcel = Parcel.obtain();
        message.writeToParcel(parcel, 0);

        parcel.setDataPosition(0); // reset parcel for reading

        EncryptedDialogueTextMessage messageFromParcel = EncryptedDialogueTextMessage.CREATOR.createFromParcel(parcel);

        parcel.recycle();

        assertEquals(message.getPlainTextBody().getMessageBody(), messageFromParcel.getPlainTextBody().getMessageBody());

        assertEquals(message.getBody().getMessageBody(), messageFromParcel.getBody().getMessageBody());

        Intent intent = new Intent();
        intent.putExtra(DialogueMessage.MESSAGE, message);
        DialogueMessage message1 = DialogueMessage.extractMessage(intent);

        assertEquals(message.getPlainTextBody().getMessageBody(), message1.getPlainTextBody().getMessageBody());
        assertEquals(message.getBody().getMessageBody(), message1.getBody().getMessageBody());
    }
}
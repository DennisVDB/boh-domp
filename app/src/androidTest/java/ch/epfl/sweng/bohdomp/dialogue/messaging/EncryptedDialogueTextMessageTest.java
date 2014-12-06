package ch.epfl.sweng.bohdomp.dialogue.messaging;

import android.content.Intent;
import android.os.Parcel;
import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.ContactFactory;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.InvalidNumberException;

import static ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact.PhoneNumber;

/**
 * Tests the EncryptedDialogueTextMessage class.
 */
public class EncryptedDialogueTextMessageTest extends AndroidTestCase {
    public void testParcelRoundTrip() throws InvalidNumberException {
        Contact contact = new ContactFactory(getContext()).contactFromNumber("1234");

        List<Contact> contactList = new ArrayList<>();
        contactList.add(contact);

        PhoneNumber phoneNumber = (PhoneNumber) contact.getPhoneNumbers(Contact.ChannelType.SMS).toArray()[0];
        EncryptedDialogueTextMessage message = new EncryptedDialogueTextMessage(getContext(),
                contact, Contact.ChannelType.SMS, phoneNumber, "test",
                DialogueMessage.MessageDirection.OUTGOING);

        Parcel parcel = Parcel.obtain();
        message.writeToParcel(parcel, 0);

        parcel.setDataPosition(0); // reset parcel for reading

        DialogueMessage messageFromParcel = EncryptedDialogueTextMessage.CREATOR.createFromParcel(parcel);

        parcel.recycle();

        assertEquals(message.getPlainTextBody().getMessageBody(),
                messageFromParcel.getPlainTextBody().getMessageBody());

        assertEquals(message.getBody().getMessageBody(), messageFromParcel.getBody().getMessageBody());
    }
}
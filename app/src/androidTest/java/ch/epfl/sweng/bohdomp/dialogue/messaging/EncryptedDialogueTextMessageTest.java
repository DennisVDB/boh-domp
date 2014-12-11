package ch.epfl.sweng.bohdomp.dialogue.messaging;

import android.os.Parcel;
import android.test.AndroidTestCase;

import ch.epfl.sweng.bohdomp.dialogue.conversation.ChannelType;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.ContactFactory;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.PhoneNumber;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.TestContactUtils;
import ch.epfl.sweng.bohdomp.dialogue.crypto.TestKeyData;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.InvalidNumberException;


/**
 * Tests the EncryptedDialogueTextMessage class.
 */
public class EncryptedDialogueTextMessageTest extends AndroidTestCase {

    private static final String PHONE_NUMBER = "1234";

    public void testParcelRoundTrip() throws Exception {
        TestContactUtils.addContact(getContext(), TestKeyData.REAL_NAME, PHONE_NUMBER);

        ContactFactory contactFactory = new ContactFactory(getContext());
        contactFactory.insertFingerprintForPhoneNumber(PHONE_NUMBER, TestKeyData.FINGERPRINT);
        Contact contact = contactFactory.contactFromNumber(PHONE_NUMBER);

        PhoneNumber phoneNumber = (PhoneNumber) contact.getPhoneNumbers(ChannelType.SMS).toArray()[0];

        EncryptedDialogueTextMessage message = new EncryptedDialogueTextMessage(getContext(),
                contact, ChannelType.SMS, phoneNumber, "test",
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

    public void testIsEncrypted() throws InvalidNumberException {
        Contact contact = new ContactFactory(getContext()).contactFromNumber("1234");

        PhoneNumber phoneNumber = (PhoneNumber) contact.getPhoneNumbers(ChannelType.SMS).toArray()[0];

        EncryptedDialogueTextMessage message = new EncryptedDialogueTextMessage(getContext(),
                contact, ChannelType.SMS, phoneNumber, "test",
                DialogueMessage.MessageDirection.OUTGOING);

        assertTrue(message.isEncrypted());
    }

}
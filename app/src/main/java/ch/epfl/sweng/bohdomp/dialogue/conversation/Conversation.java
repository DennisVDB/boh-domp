package ch.epfl.sweng.bohdomp.dialogue.conversation;

import android.content.Context;
import android.os.Parcelable;

import org.joda.time.DateTime;

import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.PhoneNumber;
import ch.epfl.sweng.bohdomp.dialogue.ids.ConversationId;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;

/**
 * Interface representing a conversation.
 */
public interface Conversation extends Parcelable {

    /**
     * Getter for conversation id
     * @return conversation id
     */
    ConversationId getId();

    /**
     * Returns the name of the conversation
     * @return the name of the conversation
     */
    String getName();

    /**
     * Getter for the set of contacts
     * @return the set of contacts of the conversation
     */

    List<Contact> getContacts();

    void setChannel(ChannelType channel);
    ChannelType getChannel();

    void setPhoneNumber(PhoneNumber phone);
    PhoneNumber getPhoneNumber();

    /**
     * Setter for the encrypt boolean
     * @param encrypt if we want to encrypt or not
     */
    void setEncrypt(boolean encrypt);

    /**
     * Getter for the encrypt boolean
     * @return true if conversation is encrypted
     */
    Boolean needEncryption();

    /**
     * Getter for the messages list of the conversation
     * @return list of messages
     */
    List<DialogueMessage> getMessages();

    /**
     * Getter for the timeStamp of the conversation
     * @return the current timestamp
     */
    DateTime getLastActivityTime();

    /**
     * Depending on the last activity gives you an info to display.
     * @param context Context in which function has to be evaluated.
     * @return pretty string to display last activity.
     */
    String getLastConversationActivityString(Context context);

    /**
     * Getter for the number of messages in the conversation
     * @return number of messages in the conversation
     */
    int getMessageCount();

    /**
     * Getter for the unread boolean
     * @return conversationHasUnread parameter
     */
    boolean hasUnread();

    /**
     * Getter for the list of listeners
     * @return the list of listeners of the conversation
     */
    List<ConversationListener> getListeners();

    /**
     * Getter for the hasUnread parameter of the conversation
     * @return he hasUnread parameter of the conversation
     */
    boolean getHasUnread();

    /**
     * Sets the message status.
     * @param message to be set.
     * @param status of the message.
     */
    void setMessageStatus(DialogueMessage message, DialogueMessage.MessageStatus status);

    /**
     * Adds a contact to the conversation
     * @param contact the contact we want to add
     */
    void addContact(Contact contact);

    /**
     * Removes a contact from the conversation
     * @param contact the contact we want to remove
     */
    void removeContact(Contact contact);


    /**
     * Adds a message to the conversation
     * @param message the message we want to add
     */
    void addMessage(DialogueMessage message);

    /**
     * Adds a listener to the conversation
     * @param listener the listener to add
     */
    void addListener(ConversationListener listener);

    /**
     * Removes a listener from the conversation
     * @param listener the listener to be removed
     */
    void removeListener(ConversationListener listener);


    /**
     * Sets all messages as read
     */
    void setAllMessagesAsRead();
}

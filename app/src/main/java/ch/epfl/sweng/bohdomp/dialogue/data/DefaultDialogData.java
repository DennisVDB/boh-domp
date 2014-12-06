package ch.epfl.sweng.bohdomp.dialogue.data;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sweng.bohdomp.dialogue.conversation.Conversation;
import ch.epfl.sweng.bohdomp.dialogue.conversation.ConversationListener;
import ch.epfl.sweng.bohdomp.dialogue.conversation.DialogueConversation;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.ids.ConversationId;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * Default implementation of DialogData
 */
public final class DefaultDialogData implements DialogueData {
    private static final Comparator<Conversation> TIME_STAMPS_COMPARATOR;

    static {
        TIME_STAMPS_COMPARATOR = new Comparator<Conversation>() {
            public int compare(Conversation c1, Conversation c2) {
                Contract.throwIfArgNull(c1, "c1");
                Contract.throwIfArgNull(c2, "c2");

                return c2.getLastActivityTime().
                        compareTo(c1.getLastActivityTime()); // lowest timestamp comes before
            }
        };
    }

    private static final DialogueData OBJECT = new DefaultDialogData();

    private static final String CONVERSATION_ID = "CONVERSATION_ID";
    private static final String CONVERSATION = "CONVERSATION";

    private final Map<ConversationId, Conversation> mConversations;

    private final List<DialogueDataListener> mListeners;

    private DefaultDialogData() {
        mConversations = new HashMap<ConversationId, Conversation>();
        mListeners = new ArrayList<DialogueDataListener>();
    }

    /**
     * Gets the singleton instance
     * @return gets the singleton instance
     */
    public static DialogueData getInstance() {
        return OBJECT;
    }

    /*
        See DialogData.getConversations
     */
    public List<Conversation> getConversations() {
        List<Conversation> dialogueConversations = new ArrayList<Conversation>(mConversations.values());
        Collections.sort(dialogueConversations, TIME_STAMPS_COMPARATOR);

        return dialogueConversations;
    }

    /*
    See DialogData.getConversation
    */
    public Conversation getConversation(ConversationId conversationId) {
        Contract.throwIfArgNull(conversationId, "conversationId");

        return mConversations.get(conversationId);
    }

    /*
    See DialogData.getConversation
    */
    public Conversation createOrGetConversation(Contact contact) {
        Collection<Conversation> conversations = mConversations.values();

        // Try finding it first.
        for (Conversation conversation : conversations) {
            if (conversation.getContacts().contains(contact)) {
                return conversation;
            }
        }

        // Default case, conversation not found.
        List<Contact> contacts = new ArrayList<Contact>();
        contacts.add(contact);
        Conversation conversation = new DialogueConversation(contacts);

        //Notify Dialogue Data listeners if a conversation changes
        conversation.addListener(new ConversationListener() {
            @Override
            public void onConversationChanged(ConversationId id) {
                notifyListeners();
            }
        });

        mConversations.put(conversation.getId(), conversation);
        notifyListeners();

        return conversation;
    }

    /*
    See DialogData.getConversation
    */
    public void removeConversation(ConversationId id) {
        if (mConversations.containsKey(id)) {
            mConversations.remove(id);
            notifyListeners();
        }
    }

    public void updateConversation(Conversation conversation) {
        Contract.throwIfArgNull(conversation, "conversation");

        if (mConversations.containsKey(conversation.getId())) {
            mConversations.put(conversation.getId(), conversation);
            notifyListeners();
        }
    }

    /*
    See DialogData.getConversation
    */
    public void removeAllConversations() {
        mConversations.clear();
        notifyListeners();
    }

    @Override
    public void addMessageToConversation(DialogueMessage message) {
        Contract.throwIfArgNull(message, "message");

        Conversation c = this.createOrGetConversation(message.getContact());
        c.addMessage(message);

        //Listeners are notified by the listener of the conversation
    }

    @Override
    public void setMessageStatus(DialogueMessage message, DialogueMessage.MessageStatus status) {
        Contract.throwIfArgNull(message, "message");
        Contract.throwIfArgNull(status, "status");

        Collection<Conversation> conversations = mConversations.values();

        Conversation conversation = null;

        for (Conversation c : conversations) {
            if (c.getContacts().contains(message.getContact())) {
                conversation = c;
            }
        }

        if (conversation != null) {
            conversation.setMessageStatus(message, status);
        }
    }

    @Override
    public void addListener(DialogueDataListener listener) {
        Contract.throwIfArgNull(listener, "listener");

        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }

    }

    @Override
    public void removeListener(DialogueDataListener listener) {
        Contract.throwIfArgNull(listener, "listener");

        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    @Override
    public void restoreFromBundle(Bundle savedData) {
        Contract.throwIfArgNull(savedData, "savedData");

        savedData.setClassLoader(getClass().getClassLoader());
        List<ConversationId> conversationIds = savedData.getParcelableArrayList(CONVERSATION_ID);
        List<Conversation> conversations = savedData.getParcelableArrayList(CONVERSATION);

        if (conversationIds!= null && conversations!= null) {
            if (conversationIds.size() == conversations.size()) {
                for (int i=0; i<conversationIds.size(); i++) {
                    mConversations.put(conversationIds.get(i), conversations.get(i));
                }
                notifyListeners();
            }
        }
    }

    @Override
    public Bundle createBundle() {
        Bundle b = new Bundle();

        b.putParcelableArrayList(CONVERSATION_ID, new ArrayList<ConversationId>(mConversations.keySet()));
        b.putParcelableArrayList(CONVERSATION, new ArrayList<Conversation>(mConversations.values()));

        return b;
    }

    //Method that notifies listeners when a change in conversation occurs
    private void notifyListeners() {
        if (mListeners.size() > 0) {
            for (DialogueDataListener listener : mListeners) {
                listener.onDialogueDataChanged();
            }
        }
    }
}

package ch.epfl.sweng.bohdomp.dialogue.conversation;

import android.os.Bundle;
import android.test.InstrumentationTestCase;

import java.util.ArrayList;

import ch.epfl.sweng.bohdomp.dialogue.exceptions.NullArgumentException;
import ch.epfl.sweng.bohdomp.dialogue.ids.ConversationId;

/**
 * Created by BohDomp! on 18.11.14.
 */
public class DialogueDataNotListenerTest extends InstrumentationTestCase {

    private static final String CONVERSATION_ID = "CONVERSATION_ID";
    private static final String CONVERSATION = "CONVERSATION";
    private static final int TWELVE = 12;

    private DialogueData mDialogueData;

    public void setUp() {
        mDialogueData = DefaultDialogData.getInstance();
    }

    public void testGetNullConversation() {
        try {
            mDialogueData.getConversation(null);
            fail("NullArgumentException expected");
        } catch (NullArgumentException e) {
            // all good :)
        }
    }

    public void testAddNullMessageToConversation() {
        try {
            mDialogueData.addMessageToConversation(null);
            fail("NullArgumentException expected");
        } catch (NullArgumentException e) {
            // all good :)
        }
    }

    public void testAddNullListener() {
        try {
            mDialogueData.addListener(null);
            fail("NullArgumentException expected");
        } catch (NullArgumentException e) {
            // all good :)
        }
    }

    public void testRemoveNullListener() {
        try {
            mDialogueData.removeListener(null);
            fail("NullArgumentException expected");
        } catch (NullArgumentException e) {
            // all good :)
        }
    }

    public void testRemoveNonAddedListener() {
        mDialogueData.removeListener(new DialogueDataListener() {
            @Override
            public void onDialogueDataChanged() {
                // we don't care :)
            }
        });

        // nothing should happen :)
    }

    public void testRestoreFromNullBundle() {
        try {
            mDialogueData.restoreFromBundle(null);
            fail("NullArgumentException expected");
        } catch (NullArgumentException e) {
            // all good :)
        }
    }

    public void testRestoreFromBundleNullConversationId() {
        Bundle bundle = new Bundle();

        bundle.putParcelableArrayList(CONVERSATION_ID, null);

        mDialogueData.restoreFromBundle(bundle);

        // nothing should happen
    }

    public void testRestoreFromBundleTooManyConversationId() {
        Bundle bundle = new Bundle();

        ArrayList<Conversation> conversations = new ArrayList<Conversation>(TWELVE);
        ArrayList<ConversationId> conversationIds = new ArrayList<ConversationId>(TWELVE + 1);

        bundle.putParcelableArrayList(CONVERSATION_ID, conversationIds);
        bundle.putParcelableArrayList(CONVERSATION, conversations);

        mDialogueData.restoreFromBundle(bundle);

        // nothing should happen
    }

    // TODO public void testRestoreFromBundle()
}
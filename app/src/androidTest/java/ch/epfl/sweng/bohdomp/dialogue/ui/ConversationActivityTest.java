package ch.epfl.sweng.bohdomp.dialogue.ui;

import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import ch.epfl.sweng.bohdomp.dialogue.R;
import ch.epfl.sweng.bohdomp.dialogue.conversation.Conversation;
import ch.epfl.sweng.bohdomp.dialogue.data.DefaultDialogData;
import ch.epfl.sweng.bohdomp.dialogue.conversation.DialogueConversation;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.ContactFactory;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueTextMessage;
import ch.epfl.sweng.bohdomp.dialogue.ui.conversation.ConversationActivity;

/**
 * @author swengTeam 2013 BohDomp
 * Test for the ConversationActivity class
 */
public class ConversationActivityTest extends ActivityInstrumentationTestCase2<ConversationActivity> {
    private static final String CONTACT_NUMBER = "12345667889";
    private static final String MSG_BODY = "HELLO";

    private ConversationActivity mActivity;
    private Instrumentation mInstrumentation;

    private ListView mMessageList;
    private EditText mMessageContent;
    private Button mSendButton;

    private Contact mContact;
    private DialogueMessage mMessage;
    private Conversation mConversation;

    private int mConversationCountAtStart;

    public ConversationActivityTest() {
        super(ConversationActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();


        mInstrumentation = getInstrumentation();


        mConversationCountAtStart = DefaultDialogData.getInstance().getConversations().size();

        mContact = new ContactFactory(mInstrumentation.getTargetContext()).contactFromNumber(CONTACT_NUMBER);
        mMessage = new DialogueTextMessage(mContact, MSG_BODY, DialogueMessage.MessageStatus.INCOMING);
        mConversation = DefaultDialogData.getInstance().createOrGetConversation(mContact);

        Intent intent = new Intent(getInstrumentation().getTargetContext(), ConversationActivity.class);
        intent.putExtra(DialogueConversation.CONVERSATION_ID, mConversation.getId());

        setActivityIntent(intent);
        mActivity = getActivity();

        mMessageList = (ListView) mActivity.findViewById(R.id.message_List);
        mMessageContent = (EditText) mActivity.findViewById(R.id.new_message_content);
        mSendButton = (Button) mActivity.findViewById(R.id.send_message_button);
    }

    //FIXME: Yoan please take a look at this test
    /*@Override
    protected void tearDown() throws Exception {
        DefaultDialogData.getInstance().removeConversation(mConversation.getId());
        assertEquals("Not reset",mConversationCountAtStart,DefaultDialogData.getInstance().getConversations().size());

        super.tearDown();
    }*/

    public void testSetup() {
        assertNotNull("Not setup correctly", DefaultDialogData.getInstance().getConversation(mConversation.getId()));

        assertNotNull(mMessageList);
        assertNotNull(mMessageContent);
        assertNotNull(mSendButton);
        assertEquals("Not setup correctly", mMessageList.getCount(), mConversation.getMessageCount());
    }

    public void testSendSms() {
        int count = mConversation.getMessageCount();

        assertEquals(mMessageList.getAdapter().getCount(), count);

        assertFalse(mSendButton.isEnabled());

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessageContent.setText(MSG_BODY);
                mSendButton.performClick();
            }
        });

        assertFalse(mSendButton.isEnabled());

        mInstrumentation.waitForIdleSync();
        //FIXME: Yoan please take a look at this test
       /* assertEquals(mMessageList.getCount(), count + 1);

        DialogueMessage msg = (DialogueMessage) mMessageList.getAdapter().getItem(0);

        assertEquals(msg.getBody().getMessageBody(), MSG_BODY);
        assertEquals(msg.getContact(), mContact);
*/
    }
}

package ch.epfl.sweng.bohdomp.dialogue.ui.conversation;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.R;
import ch.epfl.sweng.bohdomp.dialogue.channels.DialogueOutgoingDispatcher;
import ch.epfl.sweng.bohdomp.dialogue.conversation.Conversation;
import ch.epfl.sweng.bohdomp.dialogue.conversation.ConversationListener;
import ch.epfl.sweng.bohdomp.dialogue.conversation.DialogueConversation;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.data.DefaultDialogData;
import ch.epfl.sweng.bohdomp.dialogue.data.StorageManager;
import ch.epfl.sweng.bohdomp.dialogue.ids.ConversationId;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueTextMessage;
import ch.epfl.sweng.bohdomp.dialogue.messaging.EncryptedDialogueTextMessage;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * @author swengTeam 2014 BohDomp
 * Activity displaying a set of messages
 */
public class ConversationActivity extends Activity implements ConversationListener {
    private ListView mMessageList;
    private EditText mNewMessageText;
    private Button mSendButton;

    private MessagesAdapter mMessageItemListAdapter;

    private Conversation mConversation;
    private List<DialogueMessage> mMessages;

    private StorageManager mStorageManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        setupActionBar();

        Intent intent = getIntent();

        try {
            ConversationId conversationID;
            conversationID = intent.getParcelableExtra(DialogueConversation.CONVERSATION_ID);

            initData(conversationID);
            setViewElement();
            setupListener();

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }

    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /*
     * Initialize the data used by the activity
     */
    public void initData(ConversationId conversationId) {
        Contract.throwIfArgNull(conversationId, "conversationId");

        mStorageManager = new StorageManager(getApplicationContext());
        mConversation = DefaultDialogData.getInstance().getConversation(conversationId);

        Contract.assertNotNull(mConversation, "mConversation");


        mConversation.addListener(this);

        mMessages = mConversation.getMessages();
        mMessageItemListAdapter = new MessagesAdapter(this, mMessages);
    }

    /*
     * Set all view elements
     */
    private void setViewElement() {

        setTitle(mConversation.getName());

        mMessageList = (ListView) findViewById(R.id.message_List);
        mMessageList.setAdapter(mMessageItemListAdapter);
        mMessageList.setSelectionFromTop(mMessageItemListAdapter.getCount(), 0);

        mNewMessageText = (EditText) findViewById(R.id.new_message_content);
        mSendButton = (Button) findViewById(R.id.send_message_button);
        mSendButton.setEnabled(false);
    }

    /**
     * Setup all listener related to the view displayed by the activity
     */
    private void setupListener() {

        mNewMessageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Contract.throwIfArgNull(editable, "editable");

                if (editable.length() != 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String draftText = mNewMessageText.getText().toString();

                Contact.ChannelType channel = mConversation.getChannel();
                Contact.PhoneNumber number = mConversation.getPhoneNumber();

                Contract.assertNotNull(channel, "channel");
                Contract.assertNotNull(number, "number");

                for (Contact contact : mConversation.getContacts()) {
                    DialogueMessage message = new EncryptedDialogueTextMessage(getApplicationContext(),
                            contact, channel, number, draftText, DialogueMessage.MessageDirection.OUTGOING);

//                    DialogueMessage message = new DialogueTextMessage(contact, channel, number,
//                            draftText, DialogueMessage.MessageDirection.OUTGOING);

                    Contract.assertNotNull(message, "message");
                    Contract.assertNotNull(message.getBody(), "body");
                    Contract.assertNotNull(message.getPlainTextBody(), "plaintext");

                    Log.d("BLA", message.getBody().toString());
                    Log.d("ID", message.getId().toString());

                    DialogueOutgoingDispatcher.sendMessage(view.getContext(), message,
                            mConversation.getEncrypt());
                }

                mNewMessageText.setText("");
            }

        });

    }

    @Override
    protected void onResume() {
        if (mConversation.getChannel() == null || mConversation.getPhoneNumber() == null) {
            Intent intent = new Intent(this, ConversationSettingsActivity.class);
            intent.putExtra(DialogueConversation.CONVERSATION_ID, mConversation.getId());
            startActivity(intent);
        }

        super.onResume();
    }
    @Override
    protected void onStop() {
        mConversation.removeListener(this);
        mStorageManager.saveData();
        super.onStop();
    }

    @Override
    public void onConversationChanged(ConversationId conversationId) {
        Contract.throwIfArgNull(conversationId, "id");
        Contract.throwIfArg(mConversation.getId().getLong() != conversationId.getLong(), "Wrong listener");

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessageItemListAdapter.updateData(mConversation.getMessages());
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, ConversationSettingsActivity.class);
                intent.putExtra(DialogueConversation.CONVERSATION_ID, mConversation.getId());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent parent = this.getParentActivityIntent();
            startActivity(parent);
        }
        return super.onKeyDown(keyCode, event);
    }
}

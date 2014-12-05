package ch.epfl.sweng.bohdomp.dialogue.ui.conversation;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.List;
import java.util.Set;

import ch.epfl.sweng.bohdomp.dialogue.R;
import ch.epfl.sweng.bohdomp.dialogue.conversation.ChannelType;
import ch.epfl.sweng.bohdomp.dialogue.conversation.Conversation;
import ch.epfl.sweng.bohdomp.dialogue.conversation.DialogueConversation;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.Contact;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.PhoneNumber;
import ch.epfl.sweng.bohdomp.dialogue.data.DefaultDialogData;
import ch.epfl.sweng.bohdomp.dialogue.ids.ConversationId;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * @author swengTeam 2013 BohDomp
 * Activity used to show the settings about the ConversationActivity
 */
public class ConversationSettingsActivity extends Activity {
    private RadioGroup mGroup;
    private CheckBox mEncryptBox;

    private Conversation mConversation;
    private List<Contact> mContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_settings);

        Intent intent = getIntent();

        try {
            ConversationId conversationID;
            conversationID = intent.getParcelableExtra(DialogueConversation.CONVERSATION_ID);
            initData(conversationID);
            setViewElements();
            setListensers();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initData(ConversationId conversationId) {
        Contract.throwIfArgNull(conversationId, "conversationId");

        mConversation = DefaultDialogData.getInstance().getConversation(conversationId);

        Contract.assertNotNull(mConversation, "conversation");

        mContact = mConversation.getContacts();
    }

    private void setViewElements() {
        mGroup = (RadioGroup) this.findViewById(R.id.radioGroup);
        mEncryptBox = (CheckBox) findViewById(R.id.encryptBox);

        Contract.assertNotNull(mGroup, "radioGroup");

        mGroup.removeAllViews();

        Contact contact = mContact.get(0);

        Set<ChannelType> channels = contact.availableChannels();

        ChannelType convChannel = mConversation.getChannel();
        PhoneNumber convNumber = mConversation.getPhoneNumber();

        for (ChannelType channel : channels) {

            Set<PhoneNumber> numbers = contact.getPhoneNumbers(channel);

            for (PhoneNumber number :numbers) {
                RadioButton btn = new RadioButton(this);

                if (convNumber != null && convNumber.getNumber().equals(number.getNumber()) && convChannel == channel) {
                    btn.setChecked(true);
                }

                btn.setText(channel.toString() + " / " + number.getNumber());
                btn.setTag(R.id.id_channel, channel);
                btn.setTag(R.id.id_phoneNumber, number);
                mGroup.addView(btn);
            }
        }

        mEncryptBox.setVisibility(View.VISIBLE);
        mEncryptBox.setChecked(mConversation.needEncryption());

        if (contact.hasFingerprint()) {
            mEncryptBox.setText("Encrypt this conversation");
            mEncryptBox.setEnabled(true);
        } else {
            mEncryptBox.setText("Encryption not available for " + contact.getDisplayName());
            mEncryptBox.setEnabled(false);
        }
    }

    private void setListensers() {

        mGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton btn = (RadioButton) radioGroup.findViewById(i);

                ChannelType channel = (ChannelType) btn.getTag(R.id.id_channel);
                PhoneNumber number = (PhoneNumber) btn.getTag(R.id.id_phoneNumber);

                mConversation.setChannel(channel);
                mConversation.setPhoneNumber(number);

                DefaultDialogData.getInstance().updateConversation(mConversation);

                finish();
            }
        });

        mEncryptBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b != mConversation.needEncryption()) {
                    mConversation.setEncrypt(b);
                }

                finish();
            }
        });

    }
}

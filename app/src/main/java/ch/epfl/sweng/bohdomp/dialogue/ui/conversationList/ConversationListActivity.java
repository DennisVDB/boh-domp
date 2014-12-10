package ch.epfl.sweng.bohdomp.dialogue.ui.conversationList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.R;
import ch.epfl.sweng.bohdomp.dialogue.conversation.Conversation;
import ch.epfl.sweng.bohdomp.dialogue.conversation.DialogueConversation;
import ch.epfl.sweng.bohdomp.dialogue.conversation.contact.ContactFactory;
import ch.epfl.sweng.bohdomp.dialogue.data.DefaultDialogData;
import ch.epfl.sweng.bohdomp.dialogue.data.DialogueData;
import ch.epfl.sweng.bohdomp.dialogue.data.DialogueDataListener;
import ch.epfl.sweng.bohdomp.dialogue.data.StorageManager;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.FingerprintInsertionException;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.InvalidNumberException;
import ch.epfl.sweng.bohdomp.dialogue.ui.conversation.ConversationActivity;
import ch.epfl.sweng.bohdomp.dialogue.ui.newConversation.NewConversationActivity;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;
import de.timroes.android.listview.EnhancedListView;

/**
 * @author swengTeam 2013 BohDomp
 * Activity displaying the set of conversation
 */
public class ConversationListActivity extends Activity implements DialogueDataListener {

    private static final int PICK_CONTACT = 3;

    private EnhancedListView mContactListView;
    private LinearLayout mDefaultAppWarningLayout;
    private Button mChangeDefaultAppButton;
    private AlertDialog mDialogDeleteAll;
    private AlertDialog mDialogNoNfc;

    private String      myPackageName;

    private DialogueData mData;
    private ContactFactory mContactFactory;
    private List<Conversation> mConversationList;
    private ConversationListAdapter mConversationItemListAdapter;
    private StorageManager mStorageManager;

    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    private IntentFilter[] mNDefExchangeFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact_list);

        JodaTimeAndroid.init(this);

        initData();
        setViewElements();
        setupListener();


        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            setupNFC();
        }
    }

    private void setupNFC() {
        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        setupIntentFilter();

        if (mNfcAdapter != null) {
            mNfcAdapter.setNdefPushMessage(null, this);
        }

        checkIntentAtStart();
    }

    private void setupIntentFilter() {
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndefDetected.addDataType("text/fingerprint");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        mNDefExchangeFilters = new IntentFilter[] {
            ndefDetected
        };
    }

    private void checkIntentAtStart() {
        Intent intent = getIntent();
        if (intent != null) {
            onNewIntent(intent);
        }
    }

    /*
     * Initialize the data used by the activity
     */
    private void initData() {
        myPackageName = getPackageName();
        mData = DefaultDialogData.getInstance();
        mData.addListener(this);
        mContactFactory = new ContactFactory(getApplicationContext());
        mConversationList = mData.getConversations();
        mConversationItemListAdapter = new ConversationListAdapter(this, mConversationList);
        mStorageManager = new StorageManager(getApplicationContext());
        mStorageManager.retreiveData();
    }

    @Override
    public void onDialogueDataChanged() {
        mConversationList = mData.getConversations();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConversationItemListAdapter.update(mConversationList);
            }
        });
    }

    /*
     * Set all view elements
     */
    private void setViewElements() {
        this.setTitle(getString(R.string.conversationListTitle));
        mDefaultAppWarningLayout = (LinearLayout) findViewById(R.id.notDefaultWarning);
        checkDefaultApp();

        mChangeDefaultAppButton = (Button) findViewById(R.id.setDefaultAppButton);
        mContactListView = (EnhancedListView) findViewById(R.id.listConversationsView);
        mContactListView.setAdapter(mConversationItemListAdapter);
        mContactListView.setDismissCallback(new EnhancedListView.OnDismissCallback() {

            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {

                final Conversation deletedConversation = (Conversation) mConversationItemListAdapter.getItem(position);
                mConversationItemListAdapter.remove(position);

                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
                        mConversationItemListAdapter.insert(position, deletedConversation);
                    }

                    @Override
                    public void discard() {
                        mData.removeConversation(deletedConversation.getId());
                    }

                };
            }
        });

        mContactListView.enableSwipeToDismiss();
        mContactListView.setSwipeDirection(EnhancedListView.SwipeDirection.START);
        mContactListView.setUndoStyle(EnhancedListView.UndoStyle.COLLAPSED_POPUP);

        mContactListView.setSwipingLayout(R.id.swiping_layout);

        setDialogDeleteAll();
        setDialogNoNfc();
    }

    private void setDialogDeleteAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        mData.removeAllConversations();
                        dialog.cancel();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                        break;
                    default:
                        break;
                }
            }
        };

        builder.setTitle(getString(R.string.deleteAllDialog_title));
        builder.setMessage(getString(R.string.deleteAllDialog_question));
        builder.setPositiveButton("Yes", dialogClickListener);
        builder.setNegativeButton("No", dialogClickListener);

        mDialogDeleteAll = builder.create();
    }

    private void setDialogNoNfc() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.cancel();
                        break;
                    default:
                        break;
                }
            }
        };

        builder.setTitle("Warning");
        builder.setMessage("NFC is not available or not enable!");
        builder.setPositiveButton("OK", dialogClickListener);

        mDialogNoNfc = builder.create();
    }
    /**
     * Check if Dialogue is the default sms app, given the result it display or not a warning
     */
    private void checkDefaultApp() {
        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
            mDefaultAppWarningLayout.setVisibility(View.VISIBLE);
        } else {
            mDefaultAppWarningLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Setup all listener related to the view displayed by the activity
     */
    private void setupListener() {

        mChangeDefaultAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    DefaultDialogData.getInstance().retrieveOldSms(getApplicationContext());
                } catch (InvalidNumberException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
                startActivity(intent);
            }
        });

        mContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contract.throwIfArgNull(parent, "parent");
                Contract.throwIfArgNull(view, "view");

                ListView listView = (ListView) parent;
                DialogueConversation c = (DialogueConversation) listView.getItemAtPosition(position);

                Intent intent = new Intent(view.getContext(), ConversationActivity.class);
                intent.putExtra(DialogueConversation.CONVERSATION_ID, c.getId());

                c.setAllMessagesAsRead();

                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        checkDefaultApp();

        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNDefExchangeFilters, null);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        mContactListView.discardUndo();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mData.removeListener(this);
        mStorageManager.saveData();
        super.onStop();
    }

    /**
    * Method called when the new conversation is clicked
    * Start the "new message activity"
    */
    public void newConversationClicked(MenuItem item) {
        Intent intent = new Intent(this, NewConversationActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.list_conversations, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_deleteAll:
                mDialogDeleteAll.show();
                return true;
            case R.id.action_fingerPrint:
                selectFingerPrint();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectFingerPrint() {
        NfcManager manager = (NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();

        if (adapter != null && adapter.isEnabled()) {
            Intent intent = new Intent(this, FingerPrintExchangeActivity.class);
            startActivity(intent);
        } else {
            mDialogNoNfc.show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            resetNFCData();
            processIntentFromNFC(intent);
        }
    }

    private void processIntentFromNFC(Intent intent) {
        Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        NdefMessage inNDefMessage = (NdefMessage) parcelables[0];
        NdefRecord[] inNDefRecords = inNDefMessage.getRecords();
        NdefRecord nDefRecord = inNDefRecords[0];
        String inMsg = new String(nDefRecord.getPayload());

        getInfoFromNFC(inMsg);
    }

    private String mFingerPrint = null;
    private String mPhoneNumber = null;
    private String mLookUpKey = null;

    private void resetNFCData() {
        mFingerPrint = null;
        mPhoneNumber = null;
        mLookUpKey = null;
    }

    private void getInfoFromNFC(String msg) {
        if (msg.contains(FingerPrintExchangeActivity.SPLIT_NFC)) {

            String[] parts = msg.split(FingerPrintExchangeActivity.SPLIT_NFC);
            mFingerPrint = parts[0];

            if (parts.length == 2) {
                mPhoneNumber = parts[1];
                acceptFingerPrintDialog();

            } else {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);
            }

        } else {
            Toast.makeText(getApplicationContext(), "Bad Transmission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT && resultCode == Activity.RESULT_OK) {

            Uri contactData = data.getData();
            Cursor s = getContentResolver().query(contactData, null, null, null, null);

            if (s.moveToFirst()) {
                int index = s.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY);
                mLookUpKey = s.getString(index);
                acceptFingerPrintDialog();
            } else {
                Toast.makeText(this, "Failed to retrieve contact", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void acceptFingerPrintDialog() {
        Contract.assertNotNull(mFingerPrint, "fingerprint");
        Contract.assertTrue(mPhoneNumber != null || mLookUpKey != null, "Wrong state");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        setFingerPrint();
                        Intent intent = new Intent(getApplicationContext(), FingerPrintExchangeActivity.class);
                        dialog.cancel();
                        startActivity(intent);
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        setFingerPrint();
                        dialog.cancel();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                        break;
                    default:
                        break;
                }
            }
        };

        builder.setTitle("FingerPrint Validation");
        builder.setMessage("Do you want to accept the finger print");
        builder.setNegativeButton("Cancel", dialogClickListener);
        builder.setNeutralButton("Yes", dialogClickListener);
        builder.setPositiveButton("Yes and send mine", dialogClickListener);

        builder.create().show();
    }

    private void setFingerPrint() {
        try {

            if (mPhoneNumber != null) {

            } else if (mLookUpKey != null) {
                mContactFactory.insertFingerprintForLookupKey(mLookUpKey, mFingerPrint);
            }

            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();

        } catch (FingerprintInsertionException e) {
            Toast.makeText(this, "Failed to update Finger Print", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}

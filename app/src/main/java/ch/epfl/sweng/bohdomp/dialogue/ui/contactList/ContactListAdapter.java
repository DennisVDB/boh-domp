package ch.epfl.sweng.bohdomp.dialogue.ui.contactList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import ch.epfl.sweng.bohdomp.dialogue.BuildConfig;
import ch.epfl.sweng.bohdomp.dialogue.R;

import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.conversation.DialogueConversation;
import ch.epfl.sweng.bohdomp.dialogue.exceptions.NullArgumentException;

/**
 * @author swengTeam 2013 BohDomp
 *
 * A concrete implementation of {@link android.widget.BaseAdapter} that is backed by an array of
 * {@link ch.epfl.sweng.bohdomp.dialogue.conversation.Conversation}.
 *
 * It may throw:
 * - {@link java.lang.IllegalArgumentException} if the array is equal to null
 * - {@link java.lang.NullPointerException} if one of the conversation inside the array is null
 */
public class ContactListAdapter extends BaseAdapter{
    private static final String LOG_TAG = "ContactListAdapter";

    private final Context mContext;
    private List<DialogueConversation> mDialogueConversations;

    /**
     * Class containing all view inside a row of the contact list.
     * It is used to implement the view holder pattern
     */
    private static class ContactListViewHolder {
        protected ImageView contactPicture;
        protected TextView contactName;
        protected TextView contactChannels;
        protected TextView lastMessage;
        protected Button deleteConversation;
    }

    /**
     * Constructor
     * @param context The current context
     * @param items The array of conversation used to populate the list
     */
    public ContactListAdapter(Context context, List<DialogueConversation> items) {
        super();

        if (context == null) {
            throw new NullArgumentException("context");
        }

        if (items == null) {
            throw new NullArgumentException("items");
        }

        if (items.contains(null)) {
            throw new IllegalArgumentException("items contains null");
        }

        this.mContext = context;
        this.mDialogueConversations = items;
    }

    @Override
    public int getCount() {
        return mDialogueConversations.size();
    }

    @Override
    public Object getItem(int position) {
        return mDialogueConversations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mDialogueConversations.get(position).getId().getLong();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (parent == null) {
            throw new NullArgumentException("parent");
        }

        ContactListViewHolder viewHolder;

        if (convertView == null) {

            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.contact_list_row, parent, false);
            viewHolder = createViewHolder(convertView);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ContactListViewHolder) convertView.getTag();
        }

        DialogueConversation c = (DialogueConversation) getItem(position);

        if (c != null) {
            setupView(c, viewHolder);
        } else {
            throw new NullPointerException("Conversation");
        }

        return convertView;
    }

    /**
     * Create a new {@link ch.epfl.sweng.bohdomp.dialogue.ui.contactList.ContactListAdapter.ContactListViewHolder}
     * @param convertView Old view to reuse if possible
     * @return {@link ch.epfl.sweng.bohdomp.dialogue.ui.contactList.ContactListAdapter.ContactListViewHolder}
     * A new ContactListViewHolder
     */
    private ContactListViewHolder createViewHolder(View convertView) {
        if (BuildConfig.DEBUG && convertView == null) {
            throw new AssertionError("null convertView");
        }

        ContactListViewHolder viewHolder = new ContactListViewHolder();

        viewHolder.contactPicture = (ImageView) convertView.findViewById(R.id.contactPicture);
        viewHolder.contactName = (TextView) convertView.findViewById(R.id.contactName);
        viewHolder.contactChannels = (TextView) convertView.findViewById(R.id.contactChannels);
        viewHolder.lastMessage = (TextView) convertView.findViewById(R.id.contactLastMessage);

        //FIXME SHOULD BE REPLACED BY A SWIPE TO DELETE
        viewHolder.deleteConversation = (Button) convertView.findViewById(R.id.deleteConversationButton);
        viewHolder.deleteConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Delete Conversation with ID");
            }
        });

        return viewHolder;
    }

    /**
     * Setup the view using the conversation data
     * @param c The conversation used to change view to the correct values
     * @param viewHolder The View Holder containing all view to update
     */
    private void setupView(DialogueConversation c, ContactListViewHolder viewHolder) {
        //TODO
    }
}
package ch.epfl.sweng.bohdomp.dialogue.ui.conversationList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.R;
import ch.epfl.sweng.bohdomp.dialogue.conversation.Conversation;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

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
public class ConversationListAdapter extends BaseAdapter{
    private static final String LOG_TAG = "ConversationListAdapter";

    private final Context mContext;
    private List<Conversation> mConversations;

    /**
     * Class containing all view inside a row of the contact list.
     * It is used to implement the view holder pattern
     */
    private static class ContactListViewHolder {
        protected ImageView contactPicture;
        protected TextView contactName;
        protected TextView contactChannels;
        protected TextView lastMessage;
        protected TextView messagePreview;
        protected TextView unRead;
    }

    /**
     * Constructor
     * @param context The current context
     * @param items The array of conversation used to populate the list
     */
    public ConversationListAdapter(Context context, List<Conversation> items) {
        super();

        Contract.throwIfArgNull(context, "context");

        this.mContext = context;
        setItems(items);
    }

    private void setItems(List<Conversation> items) {
        Contract.throwIfArgNull(items, "items");
        Contract.throwIfArg(items.contains(null), "items contains null");

        this.mConversations = items;
    }

    public void remove(int position) {
        Contract.throwIfArg(position < 0, "Invalid position");

        mConversations.remove(position);
        notifyDataSetChanged();
    }

    public void insert(int position, Conversation item) {
        Contract.throwIfArgNull(item, "Item");
        Contract.throwIfArg(position < 0, "Invalid position");

        mConversations.add(position, item);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mConversations.size();
    }

    @Override
    public Object getItem(int position) {
        return mConversations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mConversations.get(position).getId().getLong();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contract.throwIfArgNull(parent, "parent");

        ContactListViewHolder viewHolder;

        if (convertView == null) {

            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.contact_list_row, parent, false);
            viewHolder = createViewHolder(convertView);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ContactListViewHolder) convertView.getTag();
        }

        final Conversation c = (Conversation) getItem(position);

        setupView(c, viewHolder);

        return convertView;
    }

    public void update(List<Conversation> items) {
        setItems(items);
        notifyDataSetChanged();
    }

    /**
     * Create a new {@link ConversationListAdapter.ContactListViewHolder}
     * @param convertView Old view to reuse if possible
     * @return {@link ConversationListAdapter.ContactListViewHolder}
     * A new ContactListViewHolder
     */
    private ContactListViewHolder createViewHolder(View convertView) {
        Contract.assertNotNull(convertView, "convertView");

        ContactListViewHolder viewHolder = new ContactListViewHolder();

        viewHolder.contactPicture = (ImageView) convertView.findViewById(R.id.contactPicture);
        viewHolder.contactName = (TextView) convertView.findViewById(R.id.contactName);
        viewHolder.contactChannels = (TextView) convertView.findViewById(R.id.contactChannels);
        viewHolder.lastMessage = (TextView) convertView.findViewById(R.id.contactLastMessage);
        viewHolder.messagePreview = (TextView) convertView.findViewById(R.id.messagePreview);
        viewHolder.unRead = (TextView) convertView.findViewById(R.id.unReadDot);

        return viewHolder;
    }

    /**
     * Setup the view using the conversation data
     * @param c The conversation used to change view to the correct values
     * @param viewHolder The View Holder containing all view to update
     */
    private void setupView(Conversation c, ContactListViewHolder viewHolder) {

        String name = c.getName();
        Boolean canEncrypt = c.getContacts().get(0).hasFingerprint();
        Boolean unread = c.hasUnread();

        viewHolder.contactName.setText(name);

        if (canEncrypt) {
            viewHolder.contactChannels.setText(mContext.getString(R.string.allContactChannels));
        } else {
            viewHolder.contactChannels.setText(mContext.getString(R.string.noEncryptionChannel));
        }

        if (unread) {
            viewHolder.unRead.setVisibility(View.VISIBLE);

        }

        String previewMessage = c.getMessages().get(c.getMessageCount() - 1).getPlainTextBody().getMessageBody();
        viewHolder.messagePreview.setText(previewMessage);

        viewHolder.lastMessage.setText(c.getLastConversationActivityString(mContext));
    }
}

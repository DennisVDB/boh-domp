package ch.epfl.sweng.bohdomp.dialogue.ui.conversation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ch.epfl.sweng.bohdomp.dialogue.R;
import ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage;
import ch.epfl.sweng.bohdomp.dialogue.utils.Contract;

/**
 * @author swengTeam 2013 BohDomp
 *
 * A concrete implementation of {@link android.widget.BaseAdapter} that is backed by an array of
 * {@link ch.epfl.sweng.bohdomp.dialogue.messaging.DialogueMessage}.
 *
 * It may throw:
 * - {@link java.lang.IllegalArgumentException} if the array is equal to null
 * - {@link java.lang.NullPointerException} if one of the message inside the array is null
 */
public class MessagesAdapter extends BaseAdapter {
    private static final String LOG_TAG = "MessagesAdapter";
    public static final float NOT_COMPLETELY_INVISIBLE_NOR_VISIBLE = 0.3f;
    public static final float VISIBLE = 1.0f;

    private static final int ANIMATION_DURATION = 700;
    private static final int ANIMATION_OFFSET = 700;

    private final Context mContext;
    private List<DialogueMessage> mMessagesList;

    /**
     * Class containing all view inside a row of the message list.
     * It is used to implement the view holder pattern
     */
    private static class MessageViewHolder {
        protected LinearLayout wrapperParent;
        protected LinearLayout wrapper;

        protected TextView body;
        protected TextView timeStamp;

        protected boolean anim = false;
    }

    /**
     * Constructor
     * @param context The current context
     * @param items The array of messages used to populate the list
     */
    public MessagesAdapter(Context context, List<DialogueMessage> items) {
        super();

        Contract.throwIfArgNull(context, "context");
        Contract.throwIfArgNull(items, "items");

        this.mContext = context;
        updateData(items);
    }

    @Override
    public int getCount() {
        return mMessagesList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessagesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mMessagesList.get(position).getId().getLong();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contract.throwIfArgNull(parent, "parent");

        MessageViewHolder viewHolder;

        if (convertView == null) {

            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.message_bubble_row, parent, false);
            viewHolder = createViewHolder(convertView);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (MessageViewHolder) convertView.getTag();
        }

        DialogueMessage msg = (DialogueMessage) getItem(position);

        setupView(msg, viewHolder);

        return convertView;
    }

    public void updateData(List<DialogueMessage> items) {
        Contract.throwIfArgNull(items, "items");
        Contract.throwIfArg(items.contains(null), "items contains null");

        this.mMessagesList = items;
        notifyDataSetChanged();
    }

    /**
     * Create a new {@link ch.epfl.sweng.bohdomp.dialogue.ui.conversation.MessagesAdapter}
     * @param convertView Old view to reuse if possible
     * @return {@link ch.epfl.sweng.bohdomp.dialogue.ui.conversation.MessagesAdapter.MessageViewHolder}
     * A new MessageViewHolder
     */
    private MessageViewHolder createViewHolder(View convertView) {
        Contract.assertNotNull(convertView, "convertView");

        MessageViewHolder viewHolder = new MessageViewHolder();

        viewHolder.wrapperParent = (LinearLayout) convertView.findViewById(R.id.wrapperParent);
        viewHolder.wrapper = (LinearLayout) convertView.findViewById(R.id.wrapper);

        viewHolder.body = (TextView) convertView.findViewById(R.id.body);
        viewHolder.timeStamp = (TextView) convertView.findViewById(R.id.timeStamp);

        return viewHolder;
    }

    /**
     * Setup the view using the message data
     * @param msg The message used to change view to the correct values
     * @param viewHolder The View Holder containing all view to update
     */
    private void setupView(DialogueMessage msg, MessageViewHolder viewHolder) {
        Contract.assertNotNull(msg, "message");
        Contract.assertNotNull(viewHolder, "viewHolder");

        String body = msg.getPlainTextBody().getMessageBody();
        viewHolder.body.setText(body);
        viewHolder.timeStamp.setText(msg.prettyTimeStamp(mContext));
        viewHolder.wrapper.clearAnimation();

        if (msg.getDirection() == DialogueMessage.MessageDirection.OUTGOING) {
            viewHolder.wrapperParent.setGravity(Gravity.RIGHT);

            viewHolder.wrapper.getBackground().setColorFilter(Color.parseColor("#80DEEA"), PorterDuff.Mode.MULTIPLY);
            viewHolder.wrapper.setGravity(Gravity.RIGHT);

            switch(msg.getStatus()) {
                case IN_TRANSIT:
                    viewHolder.anim = true;
                    anim(viewHolder, msg);
                    break;
                case SENT:
                    viewHolder.anim = false;
                    break;
                case DELIVERED:
                    viewHolder.anim = false;
                    break;
                case FAILED:
                    viewHolder.anim = false;
                    break;
                default:
                    break;
            }

        } else if (msg.getDirection() == DialogueMessage.MessageDirection.INCOMING) {
            viewHolder.wrapper.clearAnimation();
            viewHolder.wrapperParent.setGravity(Gravity.LEFT);

            viewHolder.wrapper.getBackground().setColorFilter(Color.parseColor("#4DD0E1"),
                    PorterDuff.Mode.MULTIPLY);
            viewHolder.wrapper.setGravity(Gravity.LEFT);

        }
    }

    private void anim(final MessageViewHolder viewHolder, final DialogueMessage msg) {
        final AlphaAnimation animation1 = new AlphaAnimation(NOT_COMPLETELY_INVISIBLE_NOR_VISIBLE, VISIBLE);
        animation1.setDuration(ANIMATION_DURATION);
        animation1.setStartOffset(ANIMATION_OFFSET);

        final AlphaAnimation animation2 = new AlphaAnimation(VISIBLE, NOT_COMPLETELY_INVISIBLE_NOR_VISIBLE);
        animation2.setDuration(ANIMATION_DURATION);
        animation2.setStartOffset(ANIMATION_OFFSET);

        animation1.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation arg0) {
                if (viewHolder.anim) {

                    viewHolder.wrapper.startAnimation(animation2);

                } else {

                    if (msg.getStatus() == DialogueMessage.MessageStatus.FAILED) {
                        viewHolder.wrapper.getBackground().setColorFilter(Color.parseColor("#F44336"),
                                PorterDuff.Mode.MULTIPLY);
                    }

                    viewHolder.wrapper.clearAnimation();
                }
            }

            @Override public void onAnimationRepeat(Animation arg0) { }
            @Override public void onAnimationStart(Animation arg0) { }
        });

        animation2.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation arg0) {
                viewHolder.wrapper.startAnimation(animation1);
            }

            @Override public void onAnimationRepeat(Animation arg0) { }
            @Override public void onAnimationStart(Animation arg0) { }

        });

        viewHolder.wrapper.startAnimation(animation1);
    }
}

package ua.nure.dzhafarov.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.models.User;
import ua.nure.dzhafarov.vkontakte.models.Message;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    abstract class MessageHolder extends RecyclerView.ViewHolder {
        
        TextView messageDateTextView;
        
        MessageHolder(View itemView) {
            super(itemView);
        }
        
        abstract void bindMessage(Message message);
    }
    
    private class MessageSendHolder extends MessageHolder {

        private TextView messageBodyTextView;
        private TextView messageTimeTextView;
        private ImageView deliveryImageView;

        MessageSendHolder(View itemView) {
            super(itemView);

            messageDateTextView = (TextView) itemView.findViewById(R.id.message_send_date);
            messageTimeTextView = (TextView) itemView.findViewById(R.id.message_send_time);
            messageBodyTextView = (TextView) itemView.findViewById(R.id.message_send_body_text_view);
            deliveryImageView = (ImageView) itemView.findViewById(R.id.delivery_image_view);
        }

        void bindMessage(Message message) {
            if (message.getSendState() == 0) {
                deliveryImageView.setImageDrawable(context.getDrawable(R.drawable.unsent));
            } else {
                if (message.getReadState() == 1) {
                    deliveryImageView.setImageDrawable(context.getDrawable(R.drawable.done_all));
                } else {
                    deliveryImageView.setImageDrawable(context.getDrawable(R.drawable.done));
                }
            }

            messageBodyTextView.setText(message.getText());
            messageTimeTextView.setText(
                    DateFormat.format(context.getString(R.string.time_form), message.getTime()));
        }
    }
    
    private class MessageGetHolder extends MessageHolder {

        private TextView messageBodyTextView;
        private TextView messageTimeTextView;

        MessageGetHolder(View itemView) {
            super(itemView);

            messageDateTextView = (TextView) itemView.findViewById(R.id.message_get_date);
            messageTimeTextView = (TextView) itemView.findViewById(R.id.message_get_time);
            messageBodyTextView = (TextView) itemView.findViewById(R.id.message_get_body_text_view);
        }
        
        void bindMessage(Message message) {
            if (message.getReadState() == 0) {
                messageBodyTextView.setBackground(context.getDrawable(R.drawable.new_message_even));
            } else {
                messageBodyTextView.setBackground(context.getDrawable(R.drawable.even));
            }
        
            messageBodyTextView.setText(message.getText());
            int padding = (int) context.getResources().getDimension(R.dimen.message_body_padding);
            messageBodyTextView.setPadding(padding, padding, padding, padding);
            messageTimeTextView.setText(DateFormat.format(context.getString(R.string.time_form), message.getTime()));
        }
    }

    private List<Message> messages;
    private Context context;

    public MessageAdapter(List<Message> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        User currUser = VKManager.getInstance().getCurrentUser();
        
        if (currUser.getId() == message.getFromId()) {
            return 0;
        } else {
            return 1;
        }
    }
    
    @Override
    public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        switch (viewType) {
            case 0:
                 return new MessageSendHolder(
                         inflater.inflate(R.layout.message_send_item_layout, parent, false)
                 );
            case 1:
                return new MessageGetHolder(
                        inflater.inflate(R.layout.message_get_item_layout, parent, false)
                );
        }
        
        return null;
    }

    @Override
    public void onBindViewHolder(MessageHolder holder, int position) {
        Message m = messages.get(position);
        holder.bindMessage(m);

        long nextTs = 0;

        if (position < messages.size() - 1) {
            Message pm = messages.get(position + 1);
            nextTs = pm.getTime();
        }

        setTimeTextVisibility(nextTs, m.getTime(), holder.messageDateTextView);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private void setTimeTextVisibility(long ts1, long ts2, TextView timeText) {
        Calendar cal1 = Calendar.getInstance(Locale.ROOT);
        Calendar cal2 = Calendar.getInstance(Locale.ROOT);
        cal1.setTimeInMillis(ts1);
        cal2.setTimeInMillis(ts2);

        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

        if (sameDay) {
            timeText.setVisibility(View.GONE);
            timeText.setText("");
        } else {
            timeText.setVisibility(View.VISIBLE);
            checkOnSameYear(cal2, timeText, ts2);
        }
    }

    private void checkOnSameYear(Calendar cal1, TextView timeText, Long ts) {
        Calendar now = Calendar.getInstance(Locale.ROOT);

        if (cal1.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
            timeText.setText(
                    DateFormat.format(context.getString(R.string.date_form_this_year), ts));
        } else {
            timeText.setText(
                    DateFormat.format(context.getString(R.string.date_form_other_year), ts));
        }
    }
}

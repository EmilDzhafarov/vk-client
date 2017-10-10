package ua.nure.dzhafarov.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.models.User;
import ua.nure.dzhafarov.vkontakte.models.Message;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    class MessageHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayout;
        private TextView messageBodyTextView;
        private TextView messageDateTextView;
        private TextView messageTimeTextView;
        private ImageView deliveryImageView;
        private CircleImageView userPhoto;

        MessageHolder(View itemView) {
            super(itemView);

            linearLayout = (LinearLayout) itemView.findViewById(R.id.linear_layout);
            messageDateTextView = (TextView) itemView.findViewById(R.id.message_date);
            messageTimeTextView = (TextView) itemView.findViewById(R.id.message_time);
            messageBodyTextView = (TextView) itemView.findViewById(R.id.message_body_text_view);
            deliveryImageView = (ImageView) itemView.findViewById(R.id.delivery_image_view);
            userPhoto = (CircleImageView) itemView.findViewById(R.id.user_photo);
        }

        void bindMessage(Message message) {
            User user = VKManager.getInstance().getCurrentUser();

            LinearLayout.LayoutParams bodyParams = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();

            if (message.getFromId() == user.getId()) {

                if (message.getSendState() == 0) {
                    deliveryImageView.setImageDrawable(context.getDrawable(R.drawable.unsent));
                } else {
                    if (message.getReadState() == 1) {
                        deliveryImageView.setImageDrawable(context.getDrawable(R.drawable.done_all));
                    } else {
                        deliveryImageView.setImageDrawable(context.getDrawable(R.drawable.done));
                    }
                }

                messageBodyTextView.setBackground(context.getDrawable(R.drawable.odd));
                messageTimeTextView.setTextColor(context.getColor(R.color.text_color_send));
                bodyParams.gravity = Gravity.END;
                deliveryImageView.setVisibility(View.VISIBLE);
            } else {
                if (message.getReadState() == 0) {
                    messageBodyTextView.setBackground(context.getDrawable(R.drawable.new_message_even));    
                } else {
                    messageBodyTextView.setBackground(context.getDrawable(R.drawable.even));    
                }
                
                messageTimeTextView.setTextColor(context.getColor(R.color.text_color_get));
                bodyParams.gravity = Gravity.START;
                deliveryImageView.setVisibility(View.GONE);
                userPhoto.setVisibility(View.GONE);
            }

            linearLayout.setLayoutParams(bodyParams);

            messageBodyTextView.setText(message.getText());
            messageBodyTextView.setPadding(12, 12, 12, 12);
            messageTimeTextView.setText(
                    DateFormat.format(context.getString(R.string.time_form), message.getTime()));
        }
    }

    private List<Message> messages;
    private Context context;

    public MessageAdapter(List<Message> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @Override
    public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.message_item_layout, parent, false);

        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageHolder holder, int position) {
        Message m = messages.get(position);
        holder.bindMessage(m);
        
        Calendar cal1 = Calendar.getInstance(Locale.ROOT);
        Calendar cal2 = Calendar.getInstance(Locale.ROOT);
        Calendar now = Calendar.getInstance(Locale.ROOT);
        cal1.setTimeInMillis(m.getTime());
        
        if (position == messages.size() - 1) {
            checkOnCurrentYear(cal1, now, holder.messageDateTextView, m);
        } else {
            for (int i = position + 1; i < messages.size(); i++) {
                cal2.setTimeInMillis(messages.get(i).getTime());
                boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

                if (!sameDay) {
                    checkOnCurrentYear(cal1, now, holder.messageDateTextView, m);
                } else {
                    holder.messageDateTextView.setVisibility(View.GONE);
                    holder.messageDateTextView.setText("");
                    break;
                }
            }
        }   
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }

    private void checkOnCurrentYear(Calendar cal1, Calendar now, TextView textView, Message m) {
        textView.setVisibility(View.VISIBLE);
        
        if (cal1.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
            textView.setText(
                    DateFormat.format(context.getString(R.string.date_form_this_year), m.getTime()));
        } else {
            textView.setText(
                    DateFormat.format(context.getString(R.string.date_form_other_year), m.getTime()));
        }
    }
}

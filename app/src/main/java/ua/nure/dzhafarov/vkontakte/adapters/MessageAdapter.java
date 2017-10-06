package ua.nure.dzhafarov.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.models.User;
import ua.nure.dzhafarov.vkontakte.models.Message;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {
    
    class MessageHolder extends RecyclerView.ViewHolder {
        
        private TextView messageBodyTextView;
        private TextView messageDateTextView;
        private TextView messageTimeTextView;
        
        MessageHolder(View itemView) {
            super(itemView);
            
            messageDateTextView = (TextView) itemView.findViewById(R.id.message_date);
            messageTimeTextView = (TextView) itemView.findViewById(R.id.message_time); 
            messageBodyTextView = (TextView) itemView.findViewById(R.id.message_body_text_view);
        }
        
        void bindMessage(Message message) {
            User user = VKManager.getInstance().getCurrentUser();
            LinearLayout.LayoutParams bodyParams = (LinearLayout.LayoutParams) messageBodyTextView.getLayoutParams();
            LinearLayout.LayoutParams timeParams = (LinearLayout.LayoutParams) messageTimeTextView.getLayoutParams();
            
            if (message.getFromId() == user.getId()) {
                messageBodyTextView.setBackground(context.getDrawable(R.drawable.odd));
                bodyParams.gravity = Gravity.END;
                timeParams.gravity = Gravity.END;
            } else {
                messageBodyTextView.setBackground(context.getDrawable(R.drawable.even));
                bodyParams.gravity = Gravity.START;
                timeParams.gravity = Gravity.START;
            }
            
            messageBodyTextView.setText(message.getText());
            messageTimeTextView.setText(DateFormat.format("HH:mm", message.getTime() * 1000));
            messageBodyTextView.setPadding(16,16,16,16);
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
        
        long previousTs;
        long currentTs = m.getTime() * 1000;
        
        if (position > 0){
            Message pm = messages.get(position - 1);
            previousTs = pm.getTime() * 1000;
        } else {
            previousTs = currentTs;
        }
        
        setTimeTextVisibility(currentTs, previousTs, holder.messageDateTextView);
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }

    private void setTimeTextVisibility(long ts1, long ts2, TextView timeText){
        if (ts2 == 0){
            timeText.setVisibility(View.VISIBLE);
            timeText.setText(DateFormat.format("MMMM dd yyyy", ts1));
        } else {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTimeInMillis(ts1);
            cal2.setTimeInMillis(ts2);

            boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);

            if (sameDay) {
                timeText.setVisibility(View.GONE);
                timeText.setText("");
            } else {
                timeText.setVisibility(View.VISIBLE);
                timeText.setText(DateFormat.format("MMMM dd yyyy", ts2));
            }
        }
    }
}

package ua.nure.dzhafarov.vkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.models.User;
import ua.nure.dzhafarov.vkontakte.models.Message;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {
    
    class MessageHolder extends RecyclerView.ViewHolder {
        
        private Message message;
        private TextView messageBodyTextView;
        
        public MessageHolder(View itemView) {
            super(itemView);
            
            messageBodyTextView = (TextView) itemView.findViewById(R.id.message_body_text_view);
        }
        
        void bindMessage(Message message) {
            User user = VKManager.getInstance().getCurrentUser();
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) messageBodyTextView.getLayoutParams();
            
            if (message.getFromId() == user.getId()) {
                messageBodyTextView.setBackground(context.getDrawable(R.drawable.odd));
                params.gravity = Gravity.END;
            } else {
                messageBodyTextView.setBackground(context.getDrawable(R.drawable.even));
                params.gravity = Gravity.START;
            }
            
            messageBodyTextView.setText(message.getBody());
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
        holder.bindMessage(messages.get(position));
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
}

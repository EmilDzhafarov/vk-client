package ua.nure.dzhafarov.vkontakte.utils;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ua.nure.dzhafarov.vkontakte.models.Community;
import ua.nure.dzhafarov.vkontakte.models.User;
import ua.nure.dzhafarov.vkontakte.models.LongPoll;
import ua.nure.dzhafarov.vkontakte.models.Message;
 
class VKFetcher {
    
    private String accessToken;

    VKFetcher(String accessToken) {
        this.accessToken = accessToken;
    }
    
    List<User> getAllUsers()  {
        List<User> users = new ArrayList<>();

        Uri friendsURI = Uri.parse("https://api.vk.com/method/friends.get")
                .buildUpon()
                .appendQueryParameter("v", "5.68")
                .appendQueryParameter("access_token", accessToken)
                .appendQueryParameter("order", "hints")
                .appendQueryParameter("lang", "en")
                .appendQueryParameter("fields", "id,first_name,last_name,online,last_seen,photo_100")
                .build();

        try {
            String jsonString = getUrlString(friendsURI.toString());
            JSONObject jsonObject = new JSONObject(jsonString).getJSONObject("response");
            JSONArray jsonArray = jsonObject.getJSONArray("items");

            for (int i = 0; i < jsonArray.length(); i++) {
                User user = getUserFromJSON(jsonArray.getJSONObject(i));
                users.add(user);
            }
            System.out.println("USER LIST ==> " + users);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return users;
    }

    List<Community> getAllCommunities() {
        List<Community> communities = new ArrayList<>();

        Uri communitiesURI = Uri.parse("https://api.vk.com/method/groups.get")
                .buildUpon()
                .appendQueryParameter("v", "5.68")
                .appendQueryParameter("access_token", accessToken)
                .appendQueryParameter("lang", "en")
                .appendQueryParameter("extended", "1")
                .appendQueryParameter("fields", "id,name,type,photo_100")
                .build();

        try {
            String jsonString = getUrlString(communitiesURI.toString());
            JSONObject jsonObject = new JSONObject(jsonString).getJSONObject("response");
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            
            for (int i = 0; i < jsonArray.length(); i++) {
                Community community = getCommunityFromJSON(jsonArray.getJSONObject(i));
                communities.add(community);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return communities;
    }

    Integer sendMessageToUser(String message, int userId) {
        Uri sendMessageURI = Uri.parse("https://api.vk.com/method/messages.send")
                .buildUpon()
                .appendQueryParameter("v", "5.68")
                .appendQueryParameter("access_token", accessToken)
                .appendQueryParameter("message", message)
                .appendQueryParameter("user_id", String.valueOf(userId))
                .build();
        
        Integer messageId = 0;
        
        try {
            String jsonStr = getUrlString(sendMessageURI.toString());
            JSONObject jsonObject = new JSONObject(jsonStr);
            messageId = jsonObject.getInt("response");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        
        return messageId;
    }
    
    LongPoll getLongPollServer() {
        Uri longPollUri = Uri.parse("https://api.vk.com/method/messages.getLongPollServer")
                .buildUpon()
                .appendQueryParameter("v", "5.68")
                .appendQueryParameter("access_token", accessToken)
                .appendQueryParameter("need_pts", "1")
                .appendQueryParameter("lp_version", "2")
                .build();
        
        
        LongPoll longPoll = new LongPoll();

        try {
            String jsonString = getUrlString(longPollUri.toString());
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject obj = jsonObject.getJSONObject("response");
            
            longPoll.setKey(obj.getString("key"));
            longPoll.setServer(obj.getString("server"));
            longPoll.setTs(obj.getLong("ts"));
            
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        
        return longPoll;
    }
    
    void connectToFromLongPollServer(Long ts, OperationListener<List<Message>> messageListener, OperationListener<int[]> eventListener) {
        LongPoll current = VKManager.getInstance().getCurrentLongPoll();
        
        String connectString = String.format(Locale.US,
                "https://%s?act=a_check&key=%s&ts=%d&wait=25&mode=2&version=2",
                current.getServer(), current.getKey(), ts);
        
        Long newTs;
        
        try {
            String jsonString = getUrlString(connectString);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray updates = jsonObject.getJSONArray("updates");

            newTs = jsonObject.getLong("ts");
            VKManager.getInstance().getCurrentLongPoll().setTs(newTs);
            
            List<Message> messages = new ArrayList<>();
            
            for (int i = 0; i < updates.length(); i++) {
                JSONArray event = updates.getJSONArray(i);
                int eventCode = event.getInt(0);
                
                if (eventCode == 7) {
                    eventListener.onSuccess(new int[] {event.getInt(0), event.getInt(1), event.getInt(2)});
                }
                
                if (eventCode == 4) {
                    Message message = new Message();
                    
                    message.setMessageId(event.getInt(1));
                    message.setTime(event.getLong(4));
                    message.setText(event.getString(5));
                    message.setTs(newTs);
                    
                    int mask = event.getInt(2);
                    int peerId = event.getInt(3);
                    
                    if (mask == 35) {
                        message.setUserId(peerId);
                        message.setFromId(VKManager.getInstance().getCurrentUser().getId());
                    }
                    
                    if (mask == 49) {
                        message.setFromId(peerId);
                        message.setUserId(VKManager.getInstance().getCurrentUser().getId());
                    }
                    
                    messages.add(message);
                }
            }

            messageListener.onSuccess(messages);
            
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    } 
    
    boolean removeUser(int id) {
        Uri userByIdUri = Uri.parse("https://api.vk.com/method/friends.delete")
                .buildUpon()
                .appendQueryParameter("user_id", String.valueOf(id))
                .appendQueryParameter("access_token", accessToken)
                .appendQueryParameter("v", "5.68")
                .build();

        try {
            String jsonString = getUrlString(userByIdUri.toString());
            JSONObject jsonObject = new JSONObject(jsonString).getJSONObject("response");
            int success = jsonObject.getInt("success");
            
            return success == 1;
            
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    int markMessageAsRead(Message message) {
        Uri markUri = Uri.parse("https://api.vk.com/method/messages.markAsRead")
                .buildUpon()
                .appendQueryParameter("access_token", accessToken)
                .appendQueryParameter("lang", "en")
                .appendQueryParameter("message_ids", String.valueOf(message.getMessageId()))
                .appendQueryParameter("v", "5.68")
                .build();

        int resultCode = -1;
        
        try {
            String jsonString = getUrlString(markUri.toString());
            resultCode = new JSONObject(jsonString).getInt("response");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return resultCode;
    }
    
    List<Message> getChatWith(User user, Message curr) {
        List<Message> messages = new ArrayList<>();

        int id;
        if (curr == null) {
            id = -1;
        } else {
            id = curr.getMessageId();
        }
        
        Uri messagesUri = Uri.parse("https://api.vk.com/method/messages.getHistory")
                .buildUpon()
                .appendQueryParameter("access_token", accessToken)
                .appendQueryParameter("lang", "en")
                .appendQueryParameter("user_id", String.valueOf(user.getId()))
                .appendQueryParameter("start_message_id", String.valueOf(id))
                .appendQueryParameter("count", "40")
                .appendQueryParameter("v", "5.68")
                .build();

        try {
            String jsonString = getUrlString(messagesUri.toString());
            JSONObject jsonObject = new JSONObject(jsonString).getJSONObject("response");
            JSONArray jsonArray = jsonObject.getJSONArray("items");

            for (int i = 0; i < jsonArray.length(); i++) {
                Message message = getMessageFromJSON(jsonArray.getJSONObject(i));
                messages.add(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return messages;
    }
    
    User getCurrentUser() {
        Uri userByIdUri = Uri.parse("https://api.vk.com/method/users.get")
                .buildUpon()
                .appendQueryParameter("access_token", accessToken)
                .appendQueryParameter("lang", "en")
                .appendQueryParameter("fields", "id,first_name,last_name,last_seen,photo_100,online")
                .appendQueryParameter("v", "5.68")
                .build();

        try {
            
            String jsonString = getUrlString(userByIdUri.toString());
            JSONArray jsonArray = new JSONObject(jsonString).getJSONArray("response");
            return getUserFromJSON(jsonArray.getJSONObject(0));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    private Message getMessageFromJSON(JSONObject curr) throws JSONException {
        Message message = new Message();
        
        message.setMessageId(curr.getInt("id"));
        message.setText(curr.getString("body"));
        message.setFromId(curr.getInt("from_id"));
        message.setUserId(curr.getInt("user_id"));
        message.setTime(curr.getLong("date"));
        message.setReadState(curr.getInt("read_state"));
        
        return message;
    }
    
    private User getUserFromJSON(JSONObject curr) throws JSONException {
        User user = new User();

        user.setId(curr.getInt("id"));
        user.setFirstName(curr.getString("first_name"));
        user.setLastName(curr.getString("last_name"));
        user.setOnline(curr.getInt("online") == 1);
        user.setPhotoURL(curr.getString("photo_100"));

        if (curr.has("last_seen")) {
            user.setLastSeen(curr.getJSONObject("last_seen").getLong("time"));
        }

        return user;
    }
    
    private Community getCommunityFromJSON(JSONObject curr) throws JSONException {
        Community community = new Community();
        
        community.setId(curr.getInt("id"));
        community.setName(curr.getString("name"));
        community.setType(curr.getString("type"));
        community.setPhotoUrl(curr.getString("photo_100"));
        
        return community;
    }

    private byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead;
            byte[] buffer = new byte[1024];

            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    private String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }
}

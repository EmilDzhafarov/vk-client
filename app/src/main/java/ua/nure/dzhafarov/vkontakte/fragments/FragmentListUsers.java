package ua.nure.dzhafarov.vkontakte.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.activities.ActivityUserProfile;
import ua.nure.dzhafarov.vkontakte.adapters.UserAdapter;
import ua.nure.dzhafarov.vkontakte.models.User;
import ua.nure.dzhafarov.vkontakte.utils.OnUserClickListener;
import ua.nure.dzhafarov.vkontakte.utils.OperationListener;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

import static ua.nure.dzhafarov.vkontakte.activities.ActivityUserProfile.REQUEST_USER_PROFILE;

public class FragmentListUsers extends Fragment {
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private UserAdapter userAdapter;
    private VKManager vkManager;
    private List<User> users;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_friends_layout, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadingUsers();
            }
        });
        swipeRefreshLayout.setColorSchemeColors(getActivity().getColor(R.color.colorPrimary));

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_friends);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        users = new ArrayList<>();
        userAdapter = new UserAdapter(users, getActivity(), new OnUserClickListener<User>() {
            @Override
            public void onUserClicked(User user, View view) {
                if (view.getId() == R.id.option_dots) {
                    showPopupMenu(view, user);    
                } else {
                    Intent intent = new Intent(getActivity(), ActivityUserProfile.class);
                    intent.putExtra(REQUEST_USER_PROFILE, user);
                    startActivity(intent);
                }
            }
        });
        recyclerView.setAdapter(userAdapter);
        vkManager = VKManager.getInstance();
        
        loadingUsers();
    }
    
    private void sendMessageInUI() {
        Activity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(
                                    getActivity(),
                                    "Your message has been sent",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        }
    }
    
    private void loadingUsers() {
        swipeRefreshLayout.setRefreshing(true);
        vkManager.loadUsers(new OperationListener<List<User>>() {
            @Override
            public void onSuccess(final List<User> friends) {
                loadFriendsInUI(friends);
            }
        });
    }

    private void updateTitle(Activity activity) {
        activity.setTitle(String.format(Locale.ROOT, "%d Friends", users.size()));
    }

    private void showPopupMenu(View view, final User user) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.inflate(R.menu.option_card_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        VKManager.getInstance().removeFriend(user, new OperationListener<User>() {
                            @Override
                            public void onSuccess(User object) {
                                removeFriendInUI(object);
                            }
                        });
                        break;
                    case R.id.write_message:
                        createMessageDialog(user);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void loadFriendsInUI(final List<User> friends) {
        final Activity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            users.clear();
                            users.addAll(friends);
                            userAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                            updateTitle(activity);
                        }
                    }
            );
        }
    }
    
    private void removeFriendInUI(final User user) {
        final Activity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            int pos = users.indexOf(user);
                            users.remove(pos);
                            userAdapter.notifyItemRemoved(pos);
                            updateTitle(activity);

                            Toast.makeText(
                                    getActivity(),
                                    String.format(
                                            Locale.ROOT,
                                            "%s %s has been removed from your user list",
                                            user.getFirstName(), user.getLastName()),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        }
    }
    
    private void createMessageDialog(final User user) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.send_message_layout, null);

        final EditText editText = (EditText) view.findViewById(R.id.message);
        final Button sendBtn = (Button) view.findViewById(R.id.send_button_message);
        final Button cancelBtn = (Button) view.findViewById(R.id.cancel_button_message);

        final AlertDialog dialog = builder.setTitle("Enter your message here: ").setView(view).show();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKManager.getInstance().sendMessage(
                        editText.getText().toString(),
                        user.getId(),
                        new OperationListener<Void>() {
                            @Override
                            public void onSuccess(Void object) {
                                sendMessageInUI();
                            }
                        });
                dialog.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    
}

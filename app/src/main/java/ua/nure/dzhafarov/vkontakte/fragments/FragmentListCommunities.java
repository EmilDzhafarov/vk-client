package ua.nure.dzhafarov.vkontakte.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.activities.ActivityUserProfile;
import ua.nure.dzhafarov.vkontakte.adapters.CommunityAdapter;
import ua.nure.dzhafarov.vkontakte.models.Community;
import ua.nure.dzhafarov.vkontakte.utils.OnUserClickListener;
import ua.nure.dzhafarov.vkontakte.utils.OperationListener;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

public class FragmentListCommunities extends Fragment {
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CommunityAdapter communityAdapter;
    private VKManager vkManager;
    private List<Community> communities;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_communities_layout, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout_communities);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadingCommunities();
            }
        });
        swipeRefreshLayout.setColorSchemeColors(getActivity().getColor(R.color.colorPrimary));

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_communities);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        communities = new ArrayList<>();
        communityAdapter = new CommunityAdapter(communities, getActivity(), new OnUserClickListener<Community>() {
            @Override
            public void onUserClicked(Community community, View view) {
                Intent intent = new Intent(getActivity(), ActivityUserProfile.class);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(communityAdapter);

        vkManager = VKManager.getInstance();
        
        loadingCommunities();
    }

    private void loadingCommunities() {
        swipeRefreshLayout.setRefreshing(true);
        vkManager.loadCommunities(new OperationListener<List<Community>>() {
            @Override
            public void onSuccess(List<Community> object) {
                loadCommunitiesInUI(object);
            }

            @Override
            public void onFailure(String message) {
                Activity activity = getActivity();
                
                if (activity != null) {
                    Toast.makeText(
                            activity,
                            message,
                            Toast.LENGTH_SHORT
                    ).show();   
                }
            }
        });
    }
    
    private void loadCommunitiesInUI(final List<Community> comm) {
        Activity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            communities.clear();
                            communities.addAll(comm);
                            communityAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
            );
        }
    }
    
}

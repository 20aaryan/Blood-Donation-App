package com.example.blooddonationapp.AdminSideFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.blooddonationapp.Activities.CreateFeed;
import com.example.blooddonationapp.Activities.WebViewActivity;
import com.example.blooddonationapp.Adapters.FeedAdapter;
import com.example.blooddonationapp.ModelClasses.Feed;
import com.example.blooddonationapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeedFragmentAdmin#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedFragmentAdmin extends Fragment {

    RecyclerView feedRv;
    List<Feed> feedList;
    FeedAdapter.RvClickListener clickListener;
    FloatingActionButton createFab;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FeedFragmentAdmin() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FeedFragmentAdmin.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedFragmentAdmin newInstance(String param1, String param2) {
        FeedFragmentAdmin fragment = new FeedFragmentAdmin();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed_admin, container, false);

        createFab = view.findViewById(R.id.createFeedBtn);
        createFab.setOnClickListener(v -> startActivity( new Intent(
                getContext(), CreateFeed.class )));

        feedList = new LinkedList<>();
        feedRv = view.findViewById(R.id.feedAdminRV);

        setRvOnClickListener();
        feedRv.setLayoutManager(new LinearLayoutManager(getContext()));

        FeedAdapter feedAdapter = new FeedAdapter(getContext(), feedList, clickListener);
        feedRv.setAdapter(feedAdapter);

        //HERE: create/fetch the data here
//        feedList.add(0, new Feed("hello just a check", R.drawable.background_1, "https://www.youtube.com/watch?v=vBxNDtyE_Co"));
//        feedList.add(0, new Feed("again just a check 😉", R.drawable.pic1, "https://www.google.com/search?q=hello"));

        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();

        fireStore.collection("Feed").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                // after getting the data we are calling on success method
                // and inside this method we are checking if the received
                // query snapshot is empty or not.
                if (!queryDocumentSnapshots.isEmpty()) {
                    // if the snapshot is not empty we are
                    // hiding our progress bar and adding
                    // our data in a list.
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        // after getting this list we are passing
                        // that list to our object class.

                        Feed f = d.toObject(Feed.class);

                        // and we will pass this object class inside our arraylist
                        feedList.add(0, f);
                    }
                    // after adding the data to recycler view.
                    // we are calling recycler view notifyDataSetChanged
                    // method to notify that data has been changed in recycler view.
                    feedAdapter.notifyDataSetChanged();
                } else {
                    // if the snapshot is empty we are displaying a toast message.
                       Toast.makeText(getContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                }

            }
        });


        return view;
    }

    private void setRvOnClickListener() {
        clickListener = new FeedAdapter.RvClickListener() {
            @Override
            public void onClick(View v, int position) {
                Intent intent = new Intent(getContext(), WebViewActivity.class);
                intent.putExtra("link", feedList.get(position).getLink());
                startActivity(intent);
            }
        };
    }
}
package com.techexpert.indianvaarta.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.techexpert.indianvaarta.AddMembersActivity;
import com.techexpert.indianvaarta.GroupChatActivity;
import com.techexpert.indianvaarta.MainActivity;
import com.techexpert.indianvaarta.R;
import com.techexpert.indianvaarta.contacts;

import java.security.acl.Group;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment
{
    private RecyclerView myContactsList;

    private DatabaseReference ContactsRef, UsersRef;

    private TextView textView;

    private String group_id;

    private FloatingActionButton floatingActionButton;

    ArrayList<String> arr;

    public ContactFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        // Inflate the layout for this fragment
        View contactsView = inflater.inflate(R.layout.fragment_contact, container, false);

        textView = contactsView.findViewById(R.id.main_text2);
        myContactsList = contactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        floatingActionButton = contactsView.findViewById(R.id.floating_button);

        //we will get current user id by firebase auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUserID = mAuth.getCurrentUser().getUid();

        //getting reference of contacts saved in
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        arr = new ArrayList<>();

        AddMembersActivity membersActivity = (AddMembersActivity) getActivity();
        group_id = membersActivity.GroupId();

        return contactsView;
    }

    //retrieving data from firebase and putting into recycler view
    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(ContactsRef,contacts.class)
                .build();


        FirebaseRecyclerAdapter<contacts,ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<contacts, ContactsViewHolder>(options)
        {

            //setting all the elements in the recyclerview list
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull contacts model)
            {

                //retrieving all the user id of users which are saved in current user contacts
                String UsersIds = getRef(position).getKey();

                Log.e("UsersIds", UsersIds);

                myContactsList.setVisibility(View.VISIBLE);
                textView.setVisibility(View.INVISIBLE);

                //to get all the info of user
                UsersRef.child(UsersIds).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {

                            if(dataSnapshot.child("userState").hasChild("state"))
                            {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                //checking state if online or offline
                                if(state.equals("online"))
                                {
                                    holder.OnlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if(state.equals("offline"))
                                {
                                    holder.OnlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }
                            //for those who have not updated their user profile and had just had just made the id
                            else
                            {
                                holder.OnlineIcon.setVisibility(View.INVISIBLE);
                            }


                            if(dataSnapshot.hasChild("image"))
                            {
                                String UserImage = dataSnapshot.child("image").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();
                                String profileName = dataSnapshot.child("name").getValue().toString();

                                //display retrieved info
                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                                Picasso.get().load(UserImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }
                            else
                            {
                                String profileStatus = dataSnapshot.child("status").getValue().toString();
                                String profileName = dataSnapshot.child("name").getValue().toString();

                                //display retrieved info
                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                            }

                            String uid = dataSnapshot.child("uid").getValue().toString();


                            holder.itemView.setOnClickListener(view ->
                            {

                                FirebaseDatabase.getInstance().getReference().child("Users").child(uid).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("Group"))
                                        {
                                            if(snapshot.child("Group").hasChild(group_id))
                                            {
                                                holder.select_item.setVisibility(View.VISIBLE);
                                                holder.select_item.setText("Member");
                                                holder.select_item.setTextColor(Color.GREEN);
                                                holder.relativeLayout.setBackgroundResource(R.color.colorBackground);
                                                //Toast.makeText(getContext(), "Already a member", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                if(!holder.select_item.getText().equals("Selected") && !holder.select_item.getText().equals("Member"))
                                {
                                    holder.select_item.setVisibility(View.VISIBLE);
                                    holder.select_item.setText("Selected");
                                    arr.add(uid);
                                    floatingActionButton.setVisibility(View.VISIBLE);
                                    if(holder.select_item.getText().equals("Member"))
                                        holder.relativeLayout.setBackgroundResource(R.color.colorBackground);
                                    else
                                        holder.relativeLayout.setBackgroundResource(R.color.colorButton);
                                }
                                else if(holder.select_item.getText().equals("Selected"))
                                {
                                    arr.remove(uid);
                                    holder.select_item.setVisibility(View.GONE);
                                    holder.select_item.setText("");
                                    holder.relativeLayout.setBackgroundResource(R.color.colorBackground);
                                }

                                if(arr.isEmpty() || arr.contains(group_id))
                                    floatingActionButton.setVisibility(View.INVISIBLE);

                                floatingActionButton.setOnClickListener(view1 -> AddMembersToGroup(arr));
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            //linking with the xml file
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout2, parent,false);
                //object of ContactsViewHolder class
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class ContactsViewHolder extends  RecyclerView.ViewHolder
    {

        TextView userName, userStatus, select_item;
        CircleImageView profileImage;
        ImageView OnlineIcon;
        RelativeLayout relativeLayout;

        public ContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name2);
            userStatus = itemView.findViewById(R.id.user_status2);
            profileImage = itemView.findViewById(R.id.users_profile_image2);
            OnlineIcon = itemView.findViewById(R.id.user_online_status);
            select_item = itemView.findViewById(R.id.elem_select);
            relativeLayout = itemView.findViewById(R.id.rl);
        }
    }

    private void AddMembersToGroup(ArrayList<String> arr)
    {
        DatabaseReference GroupRef= FirebaseDatabase.getInstance().getReference().child("Group");
        DatabaseReference GroupMemberRef= FirebaseDatabase.getInstance().getReference().child("Group Members");
        DatabaseReference UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        AddMembersActivity membersActivity = (AddMembersActivity)getActivity();
        String group_id = membersActivity.GroupId();
        String group_name = membersActivity.GroupName();
        String group_desc = membersActivity.GroupDesc();
        String group_pic = membersActivity.GroupImage();
        for(String s: arr)
        {
            UsersRef.child(s).child("Group").child(group_id).setValue("Member").addOnCompleteListener(task -> {

            });
            GroupRef.child(s).child(group_id).child("name").setValue(group_name).addOnCompleteListener(task -> {

            });
            GroupRef.child(s).child(group_id).child("status").setValue(group_desc).addOnCompleteListener(task -> Toast.makeText(getContext(), "Members Added", Toast.LENGTH_SHORT).show());

            GroupMemberRef.child(group_id).child(s).setValue("Member").addOnCompleteListener(task -> {

            });
        }
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
    }

}

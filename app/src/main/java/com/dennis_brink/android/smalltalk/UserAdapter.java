package com.dennis_brink.android.smalltalk;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    //List<String> userList;
    List<SmallTalkUser> userList;
    String userName;
    Context mContext;

    //FirebaseDatabase fb;
    //DatabaseReference fbref;

    public UserAdapter(List<SmallTalkUser> userList, String userName, Context mContext) {

        this.userList = userList;
        this.userName = userName;
        this.mContext = mContext;

        //fb = FirebaseDatabase.getInstance();
        //fbref = fb.getReference();

    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create the view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {

        SmallTalkUser smallTalkUser = userList.get(position);

        holder.txtUserDisplayName.setText(smallTalkUser.getName());
        if(!smallTalkUser.getUrl().equals("null")) {
            Picasso.get().load(smallTalkUser.getUrl()).into(holder.circleImageViewUser, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    holder.circleImageViewUser.setImageResource(R.drawable.ic_baseline_account_circle_24);
                }
            });

        } else {
            holder.circleImageViewUser.setImageResource(R.drawable.ic_baseline_account_circle_24);
        }

        holder.cardView.setOnClickListener(view -> {
            Intent i = new Intent(mContext, SmallTalkActivity.class);
            i.putExtra("source", userName);
            i.putExtra("target", smallTalkUser.getName());
            mContext.startActivity(i);
            // do not finish
        });


        /* This is the way the course said I had to do it but I could not dispose of the event listener
           this way...and...I don't like the app having to go to the database everytime new items are in
           the viewport. I thinks it's very costly and potentially a performance killer. So in stead of
           creating a list of keys, I created a list of user objects. This may get heavy on memory though
           so I may reconsider. I left this in the comments so it can be restored :)
        fbref.child("users").child(userList.get(position)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("username").getValue().toString();
                String url =  snapshot.child("avatar").getValue().toString();

                Log.d("DENNIS_B", "(UserAdapter) - onBindViewHolder(): reading from database for " + name);

                holder.txtUserDisplayName.setText(name);
                if(!url.equals("null")) {
                    Picasso.get().load(url).into(holder.circleImageViewUser, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            holder.circleImageViewUser.setImageResource(R.drawable.ic_baseline_account_circle_24);
                        }
                    });

                } else {
                    holder.circleImageViewUser.setImageResource(R.drawable.ic_baseline_account_circle_24);
                }

                holder.cardView.setOnClickListener(view -> {
                    Intent i = new Intent(mContext, SmallTalkActivity.class);
                    i.putExtra("source", userName);
                    i.putExtra("target", name);
                    mContext.startActivity(i);
                    // do not finish
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        */
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserHolder extends RecyclerView.ViewHolder{

        private TextView txtUserDisplayName;
        private CircleImageView circleImageViewUser;
        private CardView cardView;

        public UserHolder(@NonNull View itemView) {
            super(itemView);

            txtUserDisplayName = itemView.findViewById(R.id.textViewDisplayName);
            circleImageViewUser = itemView.findViewById(R.id.imgCircleAccountCard);
            cardView = itemView.findViewById(R.id.cardViewUser);

        }
    }

}

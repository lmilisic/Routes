package routes.example.routes;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.sql.Timestamp;
import java.time.chrono.MinguoChronology;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    Context context;
    ArrayList<Blog> blogs;
    FirebaseDatabase firebaseDatabase;
    List<Comment> listComment;
    public MyAdapter(Context c, ArrayList<Blog> p){
        context = c;
        blogs = p;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder (LayoutInflater.from(context).inflate (R.layout.blog_row,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.name.setText (blogs.get(position).getName ());
        holder.username.setText (blogs.get(position).getUsername ());
        holder.link.setText (blogs.get(position).getLink ());
        firebaseDatabase = FirebaseDatabase.getInstance ();
        final DatabaseReference openedRef = firebaseDatabase.getReference("Posts").child(blogs.get(position).getKey ());
        openedRef.addValueEventListener(new ValueEventListener () {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(Main2Activity.Personname)){
                    Open open = dataSnapshot.child(Main2Activity.Personname).getValue(Open.class);
                    final Long a = open.getTimestamp();
                    final DatabaseReference commentRef = firebaseDatabase.getReference("Comments");
                    commentRef.addValueEventListener(new ValueEventListener () {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(blogs.get(position).getKey ())){
                                listComment = new ArrayList<> ();
                                for (DataSnapshot snap:dataSnapshot.child(blogs.get(position).getKey ()).getChildren()) {
                                    Comment comment = snap.getValue(Comment.class);
                                    Long b = (long) comment.getTimestamp();
                                    Log.d("Ovo1", String.valueOf(b));
                                    if (a<b){
                                        holder.notification.setVisibility(View.VISIBLE);
                                        Log.d("Nove1", "Nove poruke");
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
                Log.d("Nove2", String.valueOf((! dataSnapshot.hasChild(Main2Activity.Personname)))+" "+ blogs.get(position).getName ());
                if (! dataSnapshot.hasChild(Main2Activity.Personname)){

                    final DatabaseReference commentRef = firebaseDatabase.getReference("Comments");
                    commentRef.addValueEventListener(new ValueEventListener () {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                            if (dataSnapshot1.hasChild(blogs.get(position).getKey ())){
                                holder.notification.setVisibility(View.VISIBLE);
                                Log.d("Nove2", "Nove poruke2");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.itemView.setOnClickListener (new View.OnClickListener (){
            @Override
            public void onClick(View view){
                Intent intent = new Intent (context, PostDetails.class);
                intent.putExtra ("name",blogs.get(position).getName ());
                intent.putExtra ("description",blogs.get(position).getDescription ());
                intent.putExtra ("km",blogs.get(position).getKm ().toString ());
                intent.putExtra ("link",blogs.get(position).getLink ());
                intent.putExtra ("time",blogs.get(position).getTime ());
                intent.putExtra ("username",blogs.get(position).getUsername ());
                intent.putExtra ("key",blogs.get(position).getKey ());
                firebaseDatabase = FirebaseDatabase.getInstance ();
                DatabaseReference openedRef = firebaseDatabase.getReference("Posts").child(blogs.get(position).getKey ()).child(Main2Activity.Personname);
                Open open = new Open (System.currentTimeMillis());
                openedRef.setValue(open).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                });
                context.startActivity (intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return blogs.size ();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name,username,link;
        ImageView notification;
        public MyViewHolder(@NonNull View itemView) {
            super (itemView);
            name = (TextView)itemView.findViewById (R.id.post_title);
            notification = (ImageView)itemView.findViewById (R.id.newnotification);
            username = (TextView)itemView.findViewById (R.id.post_username);
            link = (TextView)itemView.findViewById (R.id.post_link);

        }
    }


}

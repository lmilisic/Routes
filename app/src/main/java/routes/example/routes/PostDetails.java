package routes.example.routes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static routes.example.routes.AddPost.FCM_MESSAGE_URL;

public class PostDetails extends AppCompatActivity {
    String TAG;
    Button btnAddComment;
    FirebaseDatabase firebaseDatabase;
    public static String Key;
    public EditText commentContent;
    RecyclerView RvComment;
    CommentAdapter commentAdapter;
    List<Comment> listComment;
    String users = "Who's coming: ";
    public Integer lock1 = 0;
    OkHttpClient mClient = new OkHttpClient();
    public static String namepost ;
    public static String descriptionpost ;
    public static String kmpost ;
    public static String timepost ;
    public static String usernamepost ;
    public static String linkpost ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_post_details);
        firebaseDatabase = FirebaseDatabase.getInstance ();
        btnAddComment = findViewById (R.id.buttoncomment);
        commentContent = findViewById (R.id.commenttext);
        LinearLayoutManager lin = new LinearLayoutManager((PostDetails.this));
        RvComment = findViewById(R.id.rv_comment);
        RvComment.setLayoutManager(lin);
        RvComment.setHasFixedSize(true);
        getIncomingIntent ();
        iniRvComment();
        btnAddComment.setOnClickListener (new View.OnClickListener () {

            @Override
            public void onClick(View view) {
                btnAddComment.setVisibility (View.INVISIBLE);
                DatabaseReference commentReference = firebaseDatabase.getReference ("Comments").child (Key).push();
                String comment_content = commentContent.getText ().toString ();
                String uname = Main2Activity.Personname;
                Comment comment = new Comment (comment_content,uname);
                commentReference.setValue (comment).addOnSuccessListener (new OnSuccessListener<Void> () {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showMessage("Comment added!");
                        commentContent.setText ("");
                        sendMessage("Novi komentar!", Main2Activity.Personname + " je dodao novi komentar!", "@mipmap:ic_launcher_foreground", "Novi komentar");
                        btnAddComment.setVisibility (View.VISIBLE);
                    }
                }).addOnFailureListener (new OnFailureListener () {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showMessage("Failed to add comment!");
                        commentContent.setText ("");
                        btnAddComment.setVisibility (View.VISIBLE);
                    }
                });

            }
        });



    }

    @Override
    public void onBackPressed() {
        firebaseDatabase = FirebaseDatabase.getInstance ();
        DatabaseReference openedRef = firebaseDatabase.getReference("Posts").child(Key).child(Main2Activity.Personname);
        Open open = new Open (System.currentTimeMillis());
        openedRef.setValue(open).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startActivity (new Intent(PostDetails.this,Main2Activity.class));
                finish();
            }
        });

    }

    public void sendMessage(final String title, final String body, final String icon, final String message) {
        new AsyncTask<String, String, String> () {
            @Override
            protected String doInBackground(String... params) {
                try {
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", body);
                    notification.put("title", title);
                    notification.put("icon", icon);

                    JSONObject data = new JSONObject();
                    data.put("message", message);
                    root.put("notification", notification);
                    root.put("data", data);
                    root.put("to", "/topics/rute");

                    String result = postToFCM(root.toString());
                    return result;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject resultJson = new JSONObject (result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    String postToFCM(String bodyString) throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyString);
        Request request = new Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key=" + "AAAAg75iSx4:APA91bHrZhfgdtttEqNXZBUFUGd7_cv4GGFNGXr-z7g6hwmSkjK6ZfLxzCfOTep6MUJmFCI1tbYueoajCGWjiMeMpV6mc4V3WcOND8hvyaPwINlsQ8aAhpsNU-tuKF9LTcAyedS9z9xP")
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

    private void iniRvComment() {

        RvComment.setLayoutManager(new LinearLayoutManager (this));

        DatabaseReference commentRef = firebaseDatabase.getReference("Comments").child(Key);
        commentRef.addValueEventListener(new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listComment = new ArrayList<> ();
                for (DataSnapshot snap:dataSnapshot.getChildren()) {

                    Comment comment = snap.getValue(Comment.class);
                    listComment.add(comment) ;

                }

                commentAdapter = new CommentAdapter(PostDetails.this,listComment);
                RvComment.setAdapter(commentAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }
    private void showMessage(String message) {
        Toast.makeText (this,message,Toast.LENGTH_LONG).show ();
    }

    private void  getIncomingIntent(){

        if(getIntent ().hasExtra ("name") && getIntent ().hasExtra ("description") && getIntent ().hasExtra ("km") && getIntent ().hasExtra ("link") && getIntent ().hasExtra ("time") && getIntent ().hasExtra ("username")){
            String name = getIntent ().getStringExtra ("name");
            String description = getIntent ().getStringExtra ("description");
            String km = getIntent ().getStringExtra ("km");
            String time = getIntent ().getStringExtra ("time");
            String username = getIntent ().getStringExtra ("username");
            String link = getIntent ().getStringExtra ("link");
            String key = getIntent ().getStringExtra ("key");
            String opened = getIntent ().getStringExtra ("opened");
            Key = key;
            namepost = name;
            descriptionpost = description;
            kmpost = km;
            timepost = time;
            usernamepost = username;
            linkpost = link;
            set(name,description,km,time,username,link,key,opened);
        }
    }
    private void set(String name, String description, String km, String time, String username, String link, final String key,final String opened){

        DatabaseReference buttonRef = firebaseDatabase.getReference("Lock").child(Key);
        DatabaseReference goingRef = firebaseDatabase.getReference("Going").child(Key);
        goingRef.addValueEventListener(new ValueEventListener () {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(Main2Activity.Personname)) {
                    TextView are = findViewById (R.id.arearent);
                    are.setText ("are");
                    are.setTextColor(0xFF00FF00);
                }
                else {
                    TextView arent = findViewById (R.id.arearent);
                    arent.setText ("aren't");
                    arent.setTextColor(0xFFFF0000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        buttonRef.addValueEventListener(new ValueEventListener () {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Lock lock = dataSnapshot.getValue(Lock.class);

                if (lock.lock == 1) {
                    Log.d("lock", String.valueOf(lock.lock));
                    findViewById (R.id.buttonbegin).setVisibility (View.GONE);
                    TextView you = findViewById (R.id.you);
                    you.setText ("Route is ");
                    TextView arearent = findViewById (R.id.arearent);
                    arearent.setText ("finished");
                    arearent.setTextColor(0xFF000000);
                    TextView going = findViewById (R.id.going);
                    going.setText ("!");
                    findViewById (R.id.going1).setVisibility (View.GONE);
                    findViewById (R.id.no).setVisibility (View.GONE);
                    findViewById (R.id.yes).setVisibility (View.GONE);
                    findViewById (R.id.users).setVisibility (View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        DatabaseReference mDatabase= FirebaseDatabase.getInstance().getReference().child("Going").child(Key);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap:dataSnapshot.getChildren()){
                    Going going = snap.getValue(Going.class);
                    users = users + going.getUname().toString()+" ";
                    TextView users1 = findViewById (R.id.users);
                    users1.setText (users);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Button button_delete = findViewById (R.id.buttondelete);
        final Button button_begin = findViewById (R.id.buttonbegin);
        final Button button_edit = findViewById (R.id.buttonedit);
        button_edit.setVisibility(View.INVISIBLE);
        final Button button_details = findViewById (R.id.buttondetails);
        TextView name1 = findViewById (R.id.name);
        name1.setText (name);
        TextView description1 = findViewById (R.id.description);
        description1.setText (description);
        TextView km1 = findViewById (R.id.km);
        km1.setText (km+"km");
        TextView time1 = findViewById (R.id.time_details);
        time1.setText (time);
        TextView username1 = findViewById (R.id.username);
        username1.setText ("Created by: "+username);
        TextView link1 = findViewById (R.id.link);
        link1.setText (link);
        button_delete.setVisibility (View.GONE);
        if (Main2Activity.Personname.equals (username)){
            button_delete.setVisibility (View.VISIBLE);
            button_edit.setVisibility (View.VISIBLE);
        }
        button_delete.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                delete(key);
            }
        });
        button_begin.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (PostDetails.this, GoogleMap.class);
                startActivity(intent);
                finish();
            }
        });
        button_details.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (PostDetails.this, Rute.class);
                startActivity(intent);
                finish();
            }
        });
        button_edit.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (PostDetails.this, Edit.class);
                startActivity(intent);
                finish();
            }
        });
        findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance ();
                DatabaseReference goingReference = database.getReference ("Going").child (key).child(Main2Activity.Personname);
                String uname = Main2Activity.Personname;
                Going going = new Going (uname);
                goingReference.setValue (going).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("GOING", "poslano");
                        TextView are = findViewById (R.id.arearent);
                        are.setText ("are");
                        are.setTextColor(0xFF00FF00);
                    }
                });
            }
        });
        findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference delete = FirebaseDatabase.getInstance ().getReference ("Going").child (key).child(Main2Activity.Personname);
                delete.removeValue ();
                TextView arent = findViewById (R.id.arearent);
                arent.setText ("aren't");
                arent.setTextColor(0xFFFF0000);
            }
        });
    }
    private void delete(String key){
        DatabaseReference delete = FirebaseDatabase.getInstance ().getReference ("Posts").child (key);
        delete.removeValue ();
        DatabaseReference delete1 = FirebaseDatabase.getInstance ().getReference ("Maps").child (key);
        delete1.removeValue ();
        DatabaseReference delete2 = FirebaseDatabase.getInstance ().getReference ("Lock").child (key);
        delete2.removeValue ();
        DatabaseReference delete3 = FirebaseDatabase.getInstance ().getReference ("Comments").child (key);
        delete3.removeValue ();
        DatabaseReference delete4 = FirebaseDatabase.getInstance ().getReference ("Going").child (key);
        delete4.removeValue ();
        Intent intent = new Intent (PostDetails.this, Main2Activity.class);
        startActivity (intent);
        finish();
    }
}


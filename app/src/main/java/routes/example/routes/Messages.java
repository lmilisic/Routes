package routes.example.routes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import static routes.example.routes.PostDetails.Key;

public class Messages extends AppCompatActivity {
    String TAG;
    Button btnAddComment;
    FirebaseDatabase firebaseDatabase;
    public EditText commentContent;
    RecyclerView RvComment;
    CommentAdapter commentAdapter;
    List<Comment> listComment;
    OkHttpClient mClient = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        firebaseDatabase = FirebaseDatabase.getInstance ();
        btnAddComment = findViewById (R.id.buttoncomment1);
        commentContent = findViewById (R.id.commenttext1);
        LinearLayoutManager lin = new LinearLayoutManager((Messages.this));
        RvComment = findViewById(R.id.rv_comment1);
        RvComment.setLayoutManager(lin);
        RvComment.setHasFixedSize(true);
        iniRvComment();
        btnAddComment.setOnClickListener (new View.OnClickListener () {

            @Override
            public void onClick(View view) {
                btnAddComment.setVisibility (View.INVISIBLE);
                if (commentContent.getText().toString() != null && !commentContent.getText().toString().isEmpty()){
                DatabaseReference commentReference = firebaseDatabase.getReference ("General chat").child("Messages").push();
                String comment_content = commentContent.getText ().toString ();
                String uname = Main2Activity.Personname;
                Comment comment = new Comment (comment_content,uname);
                commentReference.setValue (comment).addOnSuccessListener (new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showMessage("Comment added!");
                        commentContent.setText ("");
                        sendMessage("Novi komentar!", Main2Activity.Personname + " je dodao novi komentar u general chat!", "@mipmap:ic_launcher_foreground", "Novi komentar");
                        btnAddComment.setVisibility (View.VISIBLE);
                    }
                }).addOnFailureListener (new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showMessage("Failed to add comment!");
                        commentContent.setText ("");
                        btnAddComment.setVisibility (View.VISIBLE);
                    }
                });

            }
            else{
            Toast.makeText(Messages.this, "Tekst ne smije biti prazan", Toast.LENGTH_SHORT).show();
            btnAddComment.setVisibility (View.VISIBLE);}
            }
        });
    }
    public void sendMessage(final String title, final String body, final String icon, final String message) {
        new AsyncTask<String, String, String>() {
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

        RvComment.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference commentRef = firebaseDatabase.getReference("General chat").child("Messages");
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listComment = new ArrayList<>();
                for (DataSnapshot snap:dataSnapshot.getChildren()) {

                    Comment comment = snap.getValue(Comment.class);
                    listComment.add(comment) ;

                }

                commentAdapter = new CommentAdapter(Messages.this,listComment);
                RvComment.setAdapter(commentAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }
    @Override
    public void onBackPressed() {
        DatabaseReference openedRef = FirebaseDatabase.getInstance ().getReference("General chat").child(Main2Activity.Personname);
        Open open = new Open (System.currentTimeMillis());
        openedRef.setValue(open).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startActivity (new Intent(Messages.this,Main2Activity.class));
                finish();
            }
        });

    }
    private void showMessage(String message) {
        Toast.makeText (this,message,Toast.LENGTH_LONG).show ();
    }
}

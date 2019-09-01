package routes.example.routes;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddPost extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private String[] Id = new String[1];
    public String TAG;
    public static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    OkHttpClient mClient = new OkHttpClient();
    GoogleSignInClient mGoogleSignInClient;
    Button add;
    ProgressBar pb;
    TextView popupTitle;
    TextView popupDescription;
    TextView popupLink;
    TextView popupDate;
    TextView popupTime;
    TextView popupKm;
    String popupUsername;
    String date1 = "0";
    String time1 = "0";
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabase1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult> () {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        Id[0] = task.getResult().getToken();
                    }
                });
        FirebaseMessaging.getInstance().subscribeToTopic("rute");
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_add_post);
        popupTitle = findViewById (R.id.popupTitle);
        popupDescription = findViewById (R.id.popupDescription);
        popupLink = findViewById (R.id.popupLink);
        popupDate = findViewById (R.id.popupDate);
        popupTime = findViewById (R.id.popupTime);
        popupKm = findViewById (R.id.popupKm);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient (this,gso);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount (this);
        if (acct != null){
            popupUsername = ((GoogleSignInAccount) acct).getDisplayName ().toString ();
        }
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });
        mDatabase = FirebaseDatabase.getInstance().getReference();
        add = findViewById (R.id.edit);
        pb = findViewById (R.id.progressBar2);
        add.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                add.setVisibility (View.INVISIBLE);
                pb.setVisibility (View.VISIBLE);
                FirebaseDatabase database = FirebaseDatabase.getInstance ();
                DatabaseReference myRef = database.getReference ("Posts").push();
                String key = myRef.getKey ();
                DatabaseReference goingReference = database.getReference ("Going").child (key).child(Main2Activity.Personname);
                String uname = Main2Activity.Personname;
                Going going = new Going (uname);
                goingReference.setValue (going).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("GOING", "poslano");
                    }
                });
                Event event = new Event(popupUsername,popupTitle.getText ().toString (),popupDescription.getText ().toString (),date1+" "+time1,popupKm.getText ().toString (),popupLink.getText ().toString (),key);
                event.setPostKey (key);
                sendMessage("Nova ruta!", Main2Activity.Personname + " je dodao novu rutu "+popupTitle.getText ()+"!", "@mipmap:ic_launcher_foreground", "Nova ruta");
                mDatabase = FirebaseDatabase.getInstance().getReference();
                Lock lock = new Lock(0);
                mDatabase1 = FirebaseDatabase.getInstance().getReference();
                mDatabase1.child("Lock").child(key).setValue(lock);
                startActivity (new Intent (AddPost.this,Main2Activity.class));
                myRef.setValue (event);
                finish();
            }
        });

    }
    private void showDatePickerDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
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


    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        String date = i2 + "/" + (i1+1) + "/" + i;
        popupDate.setText(date);
        date1 = date;
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        String time = i + ":" + i1;
        if (String.valueOf(i).length()==1 && String.valueOf(i1).length()==2){
            time = "0"+i+":"+i1;
        }
        if (String.valueOf(i1).length()==1 && String.valueOf(i).length()==2){
            time = i+":"+"0"+i1;
        }
        if (String.valueOf(i).length()==1 && String.valueOf(i1).length()==1){
            time = "0"+i+":"+"0"+i1;
        }
        popupTime.setText(time);
        time1 = time;
    }
}

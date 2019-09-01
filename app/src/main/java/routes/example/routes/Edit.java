package routes.example.routes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Edit extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabase1;
    Button add;
    String date2 = "0";
    String time2 = "0";
    public static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    OkHttpClient mClient = new OkHttpClient();
    GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        final EditText editTitle = (EditText)findViewById(R.id.replaceTitle);
        editTitle.setText(PostDetails.namepost, TextView.BufferType.EDITABLE);
        final EditText editLink = (EditText)findViewById(R.id.replaceLink);
        editLink.setText(PostDetails.linkpost, TextView.BufferType.EDITABLE);
        final EditText editDescription = (EditText)findViewById(R.id.replaceDescription);
        editDescription.setText(PostDetails.descriptionpost, TextView.BufferType.EDITABLE);
        final EditText editKm = (EditText)findViewById(R.id.replaceKm);
        editKm.setText(PostDetails.kmpost, TextView.BufferType.EDITABLE);
        TextView date = findViewById(R.id.replaceDate);
        TextView time = findViewById(R.id.replaceTime);
        String[] splited = PostDetails.timepost.split("\\s+");
        date.setText(splited[0]);
        time.setText(splited[1]);
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
        add.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                add.setVisibility (View.INVISIBLE);
                FirebaseDatabase database = FirebaseDatabase.getInstance ();
                DatabaseReference myRef = database.getReference ("Posts").child(PostDetails.Key);
                Event event = new Event(Main2Activity.Personname,editTitle.getText ().toString (),editDescription.getText ().toString (),date2+" "+time2,editKm.getText ().toString (),editLink.getText ().toString (),PostDetails.Key);
                event.setPostKey (PostDetails.Key);
                sendMessage("Promijenjena ruta", Main2Activity.Personname + " je promijenio rutu "+editTitle.getText ()+"!", "@mipmap:ic_launcher_foreground", "Promijenjena ruta");
                mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase1 = FirebaseDatabase.getInstance().getReference();
                startActivity (new Intent(Edit.this,Main2Activity.class));
                myRef.setValue (event);
                finish();
            }
        });

    }
    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        String date = i2 + "/" + (i1+1) + "/" + i;
        TextView date1 = findViewById(R.id.replaceDate);
        date1.setText(date);
        date2 = date;
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
        TextView time1 = findViewById(R.id.replaceTime);
        time1.setText(time);
        time2 = time;
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
}

package routes.example.routes;

import android.app.Person;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ButtonBarLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.ParcelUuid;
import android.provider.ContactsContract;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.security.PublicKey;
import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity{
    GoogleSignInClient mGoogleSignInClient;
    DatabaseReference reference;
    RecyclerView recyclerView;
    ArrayList<Blog> list;
    MyAdapter adapter;
    public static String Personname;
    @Override
    protected void onStart() {
        super.onStart ();
        reference.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list = new ArrayList<> ();
                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren ()){
                    Blog post = dataSnapshot1.getValue(Blog.class);
                    list.add (post);
                }
                adapter = new MyAdapter (Main2Activity.this,list);
                recyclerView.setAdapter (adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main2);
        Toolbar toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);
        FloatingActionButton fab1 = findViewById (R.id.fab1);
        FloatingActionButton fab = findViewById (R.id.fab);
        fab.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                startActivity (new Intent (Main2Activity.this, AddPost.class));
                finish();
            }
        });
        fab1.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                DatabaseReference openedRef = FirebaseDatabase.getInstance ().getReference("General chat").child(Main2Activity.Personname);
                Open open = new Open (System.currentTimeMillis());
                openedRef.setValue(open).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                });
                startActivity (new Intent (Main2Activity.this, Messages.class));
                finish();
            }
        });
        findViewById (R.id.button_sign_out).setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                signOut ();
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder (GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail ()
                .build ();
        mGoogleSignInClient = GoogleSignIn.getClient (this, gso);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount (this);
        if (acct != null) {
            String personName = ((GoogleSignInAccount) acct).getDisplayName ();
            Personname = personName;
            setTitle ("Hello " + personName);

        }
        LinearLayoutManager lin = new LinearLayoutManager((Main2Activity.this));
        lin.setStackFromEnd(true);
        lin.setReverseLayout(true);
        recyclerView = findViewById(R.id.myRecycler);
        recyclerView.setLayoutManager(lin);
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<Blog> ();
        reference = FirebaseDatabase.getInstance ().getReference ().child ("Posts");
        DatabaseReference reference1 = FirebaseDatabase.getInstance ().getReference ().child ("General chat");
        reference1.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Messages")){
                    if (dataSnapshot.hasChild(Personname)){
                        Open open = dataSnapshot.child(Main2Activity.Personname).getValue(Open.class);
                        final Long a = open.getTimestamp();
                        for (DataSnapshot snap:dataSnapshot.child("Messages").getChildren()) {
                            Comment comment = snap.getValue(Comment.class);
                            Long b = (long) comment.getTimestamp();
                            Log.d("Ovo1", String.valueOf(b));
                            if (a<b){
                                findViewById(R.id.newnotificationmessages).setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    else {
                        findViewById(R.id.newnotificationmessages).setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity (new Intent (Main2Activity.this,MainActivity.class));
                        finish();
                    }
                });
    }


}

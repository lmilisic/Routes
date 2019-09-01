package routes.example.routes;

import com.google.firebase.database.ServerValue;

public class Comment {
    private String content,uname;
    private Object timestamp;

    public Comment(String content, String uname) {
        this.content = content;
        this.uname = uname;
        this.timestamp = ServerValue.TIMESTAMP;
    }

    public Comment(String content, String uname, Object timestamp) {
        this.content = content;
        this.uname = uname;
        this.timestamp = timestamp;
    }

    public Comment(){

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}

package routes.example.routes;

public class Open {
    private Long timestamp;

    public Open(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Open() {
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

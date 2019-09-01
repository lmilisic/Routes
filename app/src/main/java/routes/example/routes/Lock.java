package routes.example.routes;

public class Lock {
    public Integer lock;

    public Lock() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Lock(Integer lock) {
        this.lock = lock;
    }
}

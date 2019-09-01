package routes.example.routes;

public class Blog {
    public String username;
    public String name;
    public String description;
    public String time;
    public String km;
    public String link;
    public String key;

    public Blog() {

    }

    public Blog(String username, String name, String description, String time, String km, String link, String key) {
        this.username = username;
        this.name = name;
        this.description = description;
        this.time = time;
        this.km = km;
        this.link = link;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTime() {
        return time;
    }

    public String getKm() {
        return km;
    }

    public String getLink() {
        return link;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public void setLink(String link) {
        this.link = link;
    }
}

package routes.example.routes;

import android.content.Intent;

public class Map {
    public Double lng;
    public Double lat;
    public String postKey;
    public Long timestamp;

    public Map() {
    }

    public Map(Double lng, Double lat, Long timestamp) {
        this.lng = lng;
        this.lat = lat;
        this.timestamp = timestamp;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

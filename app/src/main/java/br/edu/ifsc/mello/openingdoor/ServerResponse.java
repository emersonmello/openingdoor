package br.edu.ifsc.mello.openingdoor;

/**
 * Created by mello on 14/06/16.
 */
public class ServerResponse {
    private String AAID;
    private String KeyID;
    private String deviceId;
    private String username;
    private String status;

    public ServerResponse(String AAID, String keyID, String deviceId, String username, String status) {
        this.AAID = AAID;
        KeyID = keyID;
        this.deviceId = deviceId;
        this.username = username;
        this.status = status;
    }

    public String getAAID() {
        return AAID;
    }

    public void setAAID(String AAID) {
        this.AAID = AAID;
    }

    public String getKeyID() {
        return KeyID;
    }

    public void setKeyID(String keyID) {
        KeyID = keyID;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

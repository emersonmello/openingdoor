package br.edu.ifsc.mello.openingdoor;

/**
 * Created by mello on 14/06/16.
 */
public class ServerResponseReg {
    private ServerResponse authenticator;
    private String PublicKey;
    private String SignCounter;
    private String AuthenticatorVersion;
    private String tcDisplayPNGCharacteristics;
    private String username;
    private String userId;
    private String deviceId;
    private String timeStamp;
    private String status;
    private String attestCert;
    private String attestDataToSign;
    private String attestSignature;
    private String attestVerifiedStatus;

    public ServerResponseReg(ServerResponse authenticator, String publicKey, String signCounter, String authenticatorVersion, String tcDisplayPNGCharacteristics, String username, String userId, String deviceId, String timeStamp, String status, String attestCert, String attestDataToSign, String attestSignature, String attestVerifiedStatus) {
        this.authenticator = authenticator;
        PublicKey = publicKey;
        SignCounter = signCounter;
        AuthenticatorVersion = authenticatorVersion;
        this.tcDisplayPNGCharacteristics = tcDisplayPNGCharacteristics;
        this.username = username;
        this.userId = userId;
        this.deviceId = deviceId;
        this.timeStamp = timeStamp;
        this.status = status;
        this.attestCert = attestCert;
        this.attestDataToSign = attestDataToSign;
        this.attestSignature = attestSignature;
        this.attestVerifiedStatus = attestVerifiedStatus;
    }

    public ServerResponse getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(ServerResponse authenticator) {
        this.authenticator = authenticator;
    }

    public String getPublicKey() {
        return PublicKey;
    }

    public void setPublicKey(String publicKey) {
        PublicKey = publicKey;
    }

    public String getSignCounter() {
        return SignCounter;
    }

    public void setSignCounter(String signCounter) {
        SignCounter = signCounter;
    }

    public String getAuthenticatorVersion() {
        return AuthenticatorVersion;
    }

    public void setAuthenticatorVersion(String authenticatorVersion) {
        AuthenticatorVersion = authenticatorVersion;
    }

    public String getTcDisplayPNGCharacteristics() {
        return tcDisplayPNGCharacteristics;
    }

    public void setTcDisplayPNGCharacteristics(String tcDisplayPNGCharacteristics) {
        this.tcDisplayPNGCharacteristics = tcDisplayPNGCharacteristics;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAttestCert() {
        return attestCert;
    }

    public void setAttestCert(String attestCert) {
        this.attestCert = attestCert;
    }

    public String getAttestDataToSign() {
        return attestDataToSign;
    }

    public void setAttestDataToSign(String attestDataToSign) {
        this.attestDataToSign = attestDataToSign;
    }

    public String getAttestSignature() {
        return attestSignature;
    }

    public void setAttestSignature(String attestSignature) {
        this.attestSignature = attestSignature;
    }

    public String getAttestVerifiedStatus() {
        return attestVerifiedStatus;
    }

    public void setAttestVerifiedStatus(String attestVerifiedStatus) {
        this.attestVerifiedStatus = attestVerifiedStatus;
    }
}

package jp.gihyo.webauthn.entity;

public class User {
    public byte[] id;
    public String email;

    public byte[] getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(byte[] id) {
        this.id = id;
    }
}

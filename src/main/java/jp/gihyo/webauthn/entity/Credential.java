package jp.gihyo.webauthn.entity;

public class Credential {
    public byte[] credentialId;
    public byte[] userId;
    public byte[] publicKey;
    public long signatureCounter;
}

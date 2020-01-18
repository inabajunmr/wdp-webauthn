package jp.gihyo.webauthn.entity;

import java.util.Arrays;

public class Credential {
    public byte[] credentialId;
    public byte[] userId;
    public byte[] publicKey;
    public long signatureCounter;

    @Override
    public String toString() {
        return "Credential{" +
                "credentialId=" + Arrays.toString(credentialId) +
                ", userId=" + Arrays.toString(userId) +
                ", publicKey=" + Arrays.toString(publicKey) +
                ", signatureCounter=" + signatureCounter +
                '}';
    }

    public byte[] getCredentialId() {
        return credentialId;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getUserId() {
        return userId;
    }

    public long getSignatureCounter() {
        return signatureCounter;
    }

    public void setCredentialId(byte[] credentialId) {
        this.credentialId = credentialId;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public void setSignatureCounter(long signatureCounter) {
        this.signatureCounter = signatureCounter;
    }

    public void setUserId(byte[] userId) {
        this.userId = userId;
    }
}

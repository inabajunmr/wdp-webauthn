package jp.gihyo.webauthn.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webauthn4j.authenticator.AuthenticatorImpl;
import com.webauthn4j.data.*;
import com.webauthn4j.data.attestation.authenticator.AAGUID;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.COSEKey;
import com.webauthn4j.data.attestation.statement.AttestationStatement;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.validator.WebAuthnAuthenticationContextValidator;
import jp.gihyo.webauthn.entity.User;
import jp.gihyo.webauthn.repository.CredentialRepository;
import jp.gihyo.webauthn.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WebAuthnAuthenticationService {

    private final UserRepository userRepository;

    private final CredentialRepository credentialRepository;

    public WebAuthnAuthenticationService(UserRepository userRepository, CredentialRepository credentialRepository){
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
    }

    public Optional<User> find(String email){
        return userRepository.find(email);
    }

    public PublicKeyCredentialRequestOptions requestOptions(User user){
        var challenge = new DefaultChallenge();
        var timeout = 120000L;
        var rpId = "localhost";

        var credentials = credentialRepository.finds(user.id);
        var allowCredentials = credentials.stream()
                .map(credential ->
                        new PublicKeyCredentialDescriptor(
                                PublicKeyCredentialType.PUBLIC_KEY,
                                credential.credentialId,
                                Set.of()
                        ))
                .collect(Collectors.toList());

        var userVerification = UserVerificationRequirement.REQUIRED;

        return new PublicKeyCredentialRequestOptions(
                challenge,
                timeout,
                rpId,
                allowCredentials,
                userVerification,
                null
        );
    }

    public void assertionFinish(Challenge challenge,
                                byte[] credentialId,
                                byte[] clientDataJSON,
                                byte[] authenticatorData,
                                byte[] signature) throws IOException {
        var origin = Origin.create("http://localhost:8080");
        var rpId = "localhost";
        var challengeBase64 = new DefaultChallenge(
                Base64.getEncoder().encodeToString(challenge.getValue())
        );

        var serverProperty = new ServerProperty(
                origin, rpId, challengeBase64, null
        );

        var userVerificationRequired = true;
        var authenticationContext = new WebAuthnAuthenticationContext(
                credentialId,
                clientDataJSON,
                authenticatorData,
                signature,
                serverProperty,
                userVerificationRequired
        );

        var credential = credentialRepository.find(credentialId).orElseThrow();

        var publicKey = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .readValue(credential.publicKey, COSEKey.class);

        AAGUID aaguid = null;
        AttestationStatement attestationStatement = null;
        var authenticator = new AuthenticatorImpl(
                new AttestedCredentialData(aaguid, credentialId, publicKey),
                attestationStatement,
                credential.signatureCounter
        );

        var validator = new WebAuthnAuthenticationContextValidator();

        // TODO 
    }
}

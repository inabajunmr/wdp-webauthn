package jp.gihyo.webauthn.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webauthn4j.data.*;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.validator.WebAuthnRegistrationContextValidator;
import jp.gihyo.webauthn.entity.Credential;
import jp.gihyo.webauthn.entity.User;
import jp.gihyo.webauthn.repository.CredentialRepository;
import jp.gihyo.webauthn.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WebAuthnRegistrationService {

    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;

    public WebAuthnRegistrationService(CredentialRepository credentialRepository,
                                       UserRepository userRepository) {
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
    }

    public PublicKeyCredentialCreationOptions creationOptions(User user){
        var rpId = "localhost";
        var rpName = "Gihyo Relying Party";
        var rp = new PublicKeyCredentialRpEntity(
                rpId, rpName
        );

        var userId = user.id;
        var userName = user.email;
        var userDisplayName = "";
        var userInfo = new PublicKeyCredentialUserEntity(
                userId, userName, userDisplayName
        );
        var challenge = new DefaultChallenge();

        var es256 = new PublicKeyCredentialParameters(
                PublicKeyCredentialType.PUBLIC_KEY,
                COSEAlgorithmIdentifier.ES256
        );
        var rs256 = new PublicKeyCredentialParameters(
                PublicKeyCredentialType.PUBLIC_KEY,
                COSEAlgorithmIdentifier.ES256
        );
        var publicKeyCredParams = List.of(es256, rs256);

        var timeout = 120000L;

        var credentials = credentialRepository.finds(user.id);
        var excludeCredentials = credentials.stream()
                .map(credential -> new PublicKeyCredentialDescriptor(
                        PublicKeyCredentialType.PUBLIC_KEY,
                        credential.credentialId,
                        Set.of()
                )).collect(Collectors.toList());

        var authenticatorAttachment =
                AuthenticatorAttachment.PLATFORM;
        var requiredResidentKey = false;
        var userVerification = UserVerificationRequirement.REQUIRED;
        var authenticatorSelection =
                new AuthenticatorSelectionCriteria(
                        authenticatorAttachment,
                        requiredResidentKey,
                        userVerification
                        );

        var attestation = AttestationConveyancePreference.NONE;
        return new PublicKeyCredentialCreationOptions(
                rp,
                userInfo,
                challenge,
                publicKeyCredParams,
                timeout,
                excludeCredentials,
                authenticatorSelection,
                attestation,
                null
        );
    }

    public User findOrElseCreate(String email) {
        return userRepository.find(email)
                .orElseGet(() -> createUser(email));
    }

    private User createUser(String email) {
        var userId = new byte[32];
        new SecureRandom().nextBytes(userId);

        var user = new User();
        user.id = userId;
        user.email = email;
        return user;
    }

    public void creationFinish(
            User user,
            Challenge challenge,
            byte[] clientDataJSON,
            byte[] attestationObject) throws JsonProcessingException {
        var origin = Origin.create("http://localhost:8080");
        var rpId = "localhost";
        var challengeBase64 = new DefaultChallenge(
                Base64.getEncoder().encode(challenge.getValue())
        );

        var serverProperty = new ServerProperty(
                origin, rpId, challengeBase64, null
        );

        var userVerificationRequired = true;

        // TODO
        var registrationContext = new WebAuthnRegistrationContext(
                clientDataJSON,
                attestationObject,
                serverProperty,
                userVerificationRequired
        );

        var validator = WebAuthnRegistrationContextValidator
                .createNonStrictRegistrationContextValidator();

        var response = validator.validate(registrationContext);

        var credentialId = response
                .getAttestationObject()
                .getAuthenticatorData()
                .getAttestedCredentialData()
                .getCredentialId();
        var publicKey = response
                .getAttestationObject()
                .getAuthenticatorData()
                .getAttestedCredentialData()
                .getCOSEKey();
        var signatureCounter = response
                .getAttestationObject()
                .getAuthenticatorData()
                .getSignCount();

        if(userRepository.find(user.email).isEmpty()) {
            userRepository.insert(user);
        }

        var publicKeyBin = new ObjectMapper().writeValueAsBytes(publicKey);

        var credential = new Credential();
        credential.credentialId = credentialId;
        credential.publicKey = publicKeyBin;
        credential.signatureCounter = signatureCounter;
        credential.userId = user.id;
        credentialRepository.insert(credential);
    }

}

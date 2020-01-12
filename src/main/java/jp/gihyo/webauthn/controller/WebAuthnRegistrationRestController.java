package jp.gihyo.webauthn.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import com.webauthn4j.data.client.challenge.Challenge;
import jp.gihyo.webauthn.entity.User;
import jp.gihyo.webauthn.service.WebAuthnRegistrationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

@RestController
public class WebAuthnRegistrationRestController {

    Logger log = Logger.getLogger(WebAuthnRegistrationRestController.class.getName());

    private static String ATTESTATION_CHALLENGE_KEY = "attestationChallenge";
    private static String ATTESTATION_USER_KEY = "attestationUser";

    private final WebAuthnRegistrationService webAuthnRegistrationService;

    public WebAuthnRegistrationRestController(WebAuthnRegistrationService webAuthnRegistrationService) {
        this.webAuthnRegistrationService = webAuthnRegistrationService;
    }

    private static class AttestationOptionParam {
        public String email;

        @Override
        public String toString() {
            return "AttestationOptionParam{" +
                    "email='" + email + '\'' +
                    '}';
        }
    }

    @PostMapping("/attestation/options")
    public PublicKeyCredentialCreationOptions postAttestationOptions(
            @RequestBody AttestationOptionParam params,
            HttpServletRequest httpServletRequest) {
        log.info(params.toString());
        var user = webAuthnRegistrationService.findOrElseCreate(params.email);
        var options = webAuthnRegistrationService.creationOptions(user);

        var session = httpServletRequest.getSession();
        session.setAttribute(ATTESTATION_CHALLENGE_KEY, options.getChallenge());
        session.setAttribute(ATTESTATION_USER_KEY, user);

        return options;
    }

    private static class AttestationResultParam {
        public byte[] clientDataJSON;
        public byte[] attestationObject;
    }

    @PostMapping("/attestation/result")
    public void postAttestationOptions(
            @RequestBody AttestationResultParam params,
            HttpServletRequest httpServletRequest
    ) throws JsonProcessingException {
        var httpSession = httpServletRequest.getSession();
        var challenge = (Challenge) httpSession
                .getAttribute("attestationChallenge");
        var user = (User)httpSession.getAttribute("attestationUser");
        webAuthnRegistrationService.creationFinish(
                user,
                challenge,
                params.clientDataJSON,
                params.attestationObject
        );
    }
}

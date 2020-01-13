package jp.gihyo.webauthn.controller;

import com.webauthn4j.data.PublicKeyCredentialRequestOptions;
import com.webauthn4j.data.client.challenge.Challenge;
import jp.gihyo.webauthn.service.WebAuthnAuthenticationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

@RestController
public class WebAuthnAuthenticationRestController {

    private final Logger logger = Logger.getLogger(WebAuthnAuthenticationRestController.class.getName());
    private final WebAuthnAuthenticationService authenticationService;
    private static final String ASSERTION_CHALLENGE_KEY = "assertionChallenge";

    public WebAuthnAuthenticationRestController(WebAuthnAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private static class AssertionOptionsParam {
        public String email;
    }

    @PostMapping("/assertion/options")
    public PublicKeyCredentialRequestOptions postAssertionOptions(
            @RequestBody AssertionOptionsParam params,
            HttpServletRequest servletRequest) {

        logger.info("Call:" + servletRequest.getRequestURI() + " email:" + params.email);

        var user = authenticationService.find(params.email).orElseThrow();
        var options = authenticationService.requestOptions(user);

        var session = servletRequest.getSession();
        session.setAttribute(ASSERTION_CHALLENGE_KEY, options.getChallenge());

        return options;
    }

    private static class AuthenticationResultParam {
        public byte[] credentialId;
        public byte[] clientDataJSON;
        public byte[] authenticatorData;
        public byte[] signature;
        public byte[] userHandle;
    }

    @PostMapping("/assertion/result")
    public void postAssertionResult(
            @RequestBody AuthenticationResultParam params,
            HttpServletRequest servletRequest) {
        var httpSession = servletRequest.getSession();
        var challenge = (Challenge)httpSession.getAttribute(ASSERTION_CHALLENGE_KEY);


    )
}

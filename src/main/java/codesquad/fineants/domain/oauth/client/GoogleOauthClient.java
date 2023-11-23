package codesquad.fineants.domain.oauth.client;

import codesquad.fineants.domain.oauth.properties.OauthProperties;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
public class GoogleOauthClient extends OauthClient {
    private final String scope;
    private final String iss;
    private final String aud;

    private final OauthProperties.Google.AuthorizationCode properties;

    public GoogleOauthClient(OauthProperties.Google google) {
        super(google.getClientId(),
                google.getClientSecret(),
                google.getTokenUri(),
                google.getUserInfoUri(),
                google.getRedirectUri(),
                null,
                google.getAuthorizeUri(),
                google.getResponseType());
        this.scope = google.getScope();
        this.iss = google.getIss();
        this.aud = google.getAud();
        this.properties = google.getAuthorizationCode();
    }

    @Override
    public MultiValueMap<String, String> createTokenBody(final String authorizationCode, final String redirectUrl,
                                                         final String codeVerifier, String state) {
        final String grantType = "authorization_code";
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(properties.getCode(), authorizationCode);
        formData.add(properties.getClientId(), getClientId());
        formData.add(properties.getClientSecret(), getClientSecret());
        formData.add(properties.getRedirectUri(), redirectUrl);
        formData.add(properties.getCodeVerifier(), codeVerifier);
        formData.add(properties.getGrantType(), grantType);
        return formData;
    }

    @Override
    public OauthUserProfileResponse createOauthUserProfileResponse(final Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String picture = (String) attributes.get("picture");
        return new OauthUserProfileResponse(email, picture);
    }

    @Override
    public String createAuthURL(AuthorizationRequest request) {
        return getAuthorizeUri() + "?"
                + "response_type=" + getResponseType() + "&"
                + "client_id=" + getClientId() + "&"
                + "redirect_uri=" + getRedirectUri() + "&"
                + "scope=" + scope + "&"
                + "state=" + request.getState() + "&"
                + "nonce=" + request.getNonce() + "&"
                + "code_challenge=" + request.getCodeChallenge() + "&"
                + "code_challenge_method=S256";
    }

    @Override
    public void validatePayload(DecodedIdTokenPayload payload, LocalDateTime now, String nonce) {
        payload.validateIdToken(iss, aud, now, nonce);
    }

    @Override
    public boolean isSupportOICD() {
        return false;
    }
}

package co.fineants.api.domain.member.service;

public interface TokenManagementService {
	void banRefreshToken(String token);

	void banAccessToken(String token);

	boolean isAlreadyLogout(String token);

	void clear();
}

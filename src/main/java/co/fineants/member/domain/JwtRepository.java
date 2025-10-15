package co.fineants.member.domain;

public interface JwtRepository {
	void banRefreshToken(String token);

	void banAccessToken(String token);

	boolean isAlreadyLogout(String token);

	void clear();
}

package co.fineants.member.infrastructure;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.member.domain.JwtRepository;

class RedisJwtRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private JwtRepository jwtRepository;

	@DisplayName("액세스 토큰을 블랙리스트에 등록한다")
	@Test
	void banAccessToken() {
		jwtRepository.banAccessToken("accessToken");

		boolean result = jwtRepository.isAlreadyLogout("accessToken");

		Assertions.assertThat(result).isTrue();
	}

	@DisplayName("액세스토큰의 값이 null이면 등록되지 않고 예외가 발생하지 않는다")
	@Test
	void banAccessToken_whenTokenIsNull_thenNotThrowException() {
		String token = null;

		Assertions.assertThatNoException()
			.isThrownBy(() -> jwtRepository.banAccessToken(token));
	}

	@DisplayName("토큰의 값이 null이면 true를 반환한다")
	@Test
	void isAlreadyLogout_whenTokenIsNull_thenReturnFalse() {
		String token = null;

		boolean isLogout = jwtRepository.isAlreadyLogout(token);

		Assertions.assertThat(isLogout).isTrue();
	}
}

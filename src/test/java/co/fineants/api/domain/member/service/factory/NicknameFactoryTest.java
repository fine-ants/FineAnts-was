package co.fineants.api.domain.member.service.factory;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.domain.entity.Nickname;

class NicknameFactoryTest extends AbstractContainerBaseTest {

	@Autowired
	private NicknameFactory factory;

	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#validNicknameValues")
	void create_whenNicknameValueIsValid_thenNicknameIsNotNull(String value) {
		Nickname nickname = factory.create(value);

		Assertions.assertThat(nickname).isNotNull();
	}
}

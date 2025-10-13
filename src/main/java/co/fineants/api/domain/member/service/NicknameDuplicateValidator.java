package co.fineants.api.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NicknameDuplicateValidator {

	/**
	 * 닉네임 중복 검증
	 *
	 * @param nickname 닉네임
	 * @return true: 중복, false: 중복 아님
	 */
	@Transactional
	public boolean isDuplicate(String nickname) {
		return true;
	}
}

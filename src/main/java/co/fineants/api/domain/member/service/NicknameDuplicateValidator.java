package co.fineants.api.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NicknameDuplicateValidator {

	@Transactional
	public boolean verify(String nickname) {
		return false;
	}
}

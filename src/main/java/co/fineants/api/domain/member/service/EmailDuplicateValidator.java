package co.fineants.api.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailDuplicateValidator {

	@Transactional(readOnly = true)
	public boolean hasMemberWith(String provider, String email) {
		return true;
	}
}

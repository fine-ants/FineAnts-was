package codesquad.fineants.global.common.authorized.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.purchasehistory.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.purchasehistory.repository.PurchaseHistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PurchaseHistoryAuthorizedService implements AuthorizedService<PurchaseHistory> {

	private final PurchaseHistoryRepository repository;

	@Override
	public List<PurchaseHistory> findResourceAllBy(List<Long> ids) {
		return repository.findAllById(ids);
	}

	@Override
	public boolean isAuthorized(Object resource, Long memberId) {
		return ((PurchaseHistory)resource).hasAuthorization(memberId);
	}
}

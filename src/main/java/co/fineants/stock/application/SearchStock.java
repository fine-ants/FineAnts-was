package co.fineants.stock.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.stock.infrastructure.StockQueryDslRepository;
import co.fineants.stock.presentation.dto.response.StockSearchItem;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchStock {

	private final StockQueryDslRepository repository;

	@Transactional(readOnly = true)
	public List<StockSearchItem> search(String keyword) {
		return repository.getStock(keyword).stream()
			.map(StockSearchItem::from)
			.toList();
	}
}

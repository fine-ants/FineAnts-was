package co.fineants.stock.application;

import org.springframework.stereotype.Service;

import co.fineants.stock.infrastructure.StockQueryDslRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchStock {

	private final StockQueryDslRepository repository;

}

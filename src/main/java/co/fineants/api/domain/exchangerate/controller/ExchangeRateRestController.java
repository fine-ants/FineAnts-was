package co.fineants.api.domain.exchangerate.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.domain.exchangerate.domain.dto.request.ExchangeRateCreateRequest;
import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateDeleteRequest;
import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateListResponse;
import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateUpdateResponse;
import co.fineants.api.domain.exchangerate.service.ExchangeRateService;
import co.fineants.api.domain.exchangerate.service.ExchangeRateUpdateService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.success.ExchangeRateSuccessCode;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateRestController {

	private final ExchangeRateService service;
	private final ExchangeRateUpdateService exchangeRateUpdateService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	@Secured("ROLE_ADMIN")
	public ApiResponse<Void> createExchangeRate(@RequestBody ExchangeRateCreateRequest request) {
		service.createExchangeRate(request.getCode());
		return ApiResponse.success(ExchangeRateSuccessCode.CREATE_EXCHANGE_RATE);
	}

	@GetMapping
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public ApiResponse<ExchangeRateListResponse> readExchangeRates() {
		ExchangeRateListResponse response = service.readExchangeRates();
		return ApiResponse.success(ExchangeRateSuccessCode.READ_EXCHANGE_RATE, response);
	}

	@PutMapping
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public ApiResponse<Void> updateExchangeRates() {
		exchangeRateUpdateService.updateExchangeRates();
		return ApiResponse.success(ExchangeRateSuccessCode.UPDATE_EXCHANGE_RATE);
	}

	@PatchMapping("/base")
	@Secured("ROLE_ADMIN")
	public ApiResponse<Void> patchBase(@RequestParam String code) {
		service.patchBase(code);
		return ApiResponse.success(ExchangeRateSuccessCode.PATCH_EXCHANGE_RATE);
	}

	@PatchMapping("/rate")
	@Secured("ROLE_ADMIN")
	public ApiResponse<ExchangeRateUpdateResponse> updateRate(@RequestParam String code, @RequestParam Double newRate) {
		Map<String, Double> result = service.updateRate(code, newRate);
		ExchangeRateUpdateResponse response = new ExchangeRateUpdateResponse(result);
		return ApiResponse.success(ExchangeRateSuccessCode.UPDATE_EXCHANGE_RATE, response);
	}

	@DeleteMapping
	@Secured("ROLE_ADMIN")
	public ApiResponse<Void> deleteExchangeRates(@RequestBody ExchangeRateDeleteRequest codes) {
		service.deleteExchangeRates(codes.getCodes());
		return ApiResponse.success(ExchangeRateSuccessCode.DELETE_EXCHANGE_RATE);
	}
}

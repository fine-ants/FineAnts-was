package co.fineants.api.global.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.logging.log4j.util.Strings;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.domain.role.domain.Role;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.errors.exception.business.MemberNotFoundException;
import co.fineants.api.global.errors.exception.business.NotFoundException;
import co.fineants.api.global.errors.exception.business.RoleNotFoundException;
import co.fineants.api.global.init.properties.MemberProperties;
import co.fineants.api.global.init.properties.RoleProperties;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.FetchStockService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SetupDataLoader {
	private final RoleRepository roleRepository;
	private final MemberRepository memberRepository;
	private final StockRepository stockRepository;
	private final StockDividendRepository stockDividendRepository;
	private final FetchDividendService fetchDividendService;
	private final FetchStockService fetchStockService;
	private final RoleSetupDataLoader roleSetupDataLoader;
	private final RoleProperties roleProperties;
	private final MemberSetupDataLoader memberSetupDataLoader;
	private final MemberProperties memberProperties;

	@Transactional
	public void setupResources() {
		roleSetupDataLoader.setupRoles(roleProperties);
		memberSetupDataLoader.setupMembers(memberProperties);
		setAdminAuthentication();
		setupStockResources();
		setupStockDividendResources();
	}

	private void setAdminAuthentication() {
		MemberProperties.MemberAuthProperty adminProperty = memberProperties.getProperties().stream()
			.filter(prop -> prop.getRoleName().equals("ROLE_ADMIN"))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No admin properties configured"));

		Member admin = memberRepository.findMemberByEmailAndProvider(adminProperty.getEmail(),
				adminProperty.getProvider())
			.orElseThrow(() -> new MemberNotFoundException(adminProperty.getEmail()));
		Role roleAdmin = roleRepository.findRoleByRoleName(adminProperty.getRoleName())
			.orElseThrow(supplierNotFoundRoleException());
		Set<String> roleNames = Set.of(roleAdmin.getRoleName());
		MemberAuthentication memberAuthentication = MemberAuthentication.from(admin, roleNames);
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
			memberAuthentication,
			Strings.EMPTY,
			memberAuthentication.getSimpleGrantedAuthority()
		);
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
	}

	@NotNull
	private static Supplier<NotFoundException> supplierNotFoundRoleException() {
		return () -> new RoleNotFoundException(Strings.EMPTY);
	}

	private void setupStockResources() {
		List<Stock> stocks = stockRepository.saveAll(fetchStockService.fetchStocks());
		log.info("setupStock count is {}", stocks.size());
	}

	private void setupStockDividendResources() {
		List<StockDividend> stockDividends = fetchDividendService.fetchDividendEntityIn(stockRepository.findAll());
		List<StockDividend> savedStockDividends = new ArrayList<>();
		for (StockDividend stockDividend : stockDividends) {
			if (stockDividendRepository.findByTickerSymbolAndRecordDate(stockDividend.getStock().getTickerSymbol(),
				stockDividend.getDividendDates().getRecordDate()).isEmpty()) {
				StockDividend saveStockDividend = stockDividendRepository.save(stockDividend);
				savedStockDividends.add(saveStockDividend);
			}
		}
		log.info("saved StockDividends count is {}", savedStockDividends.size());
	}
}

package co.fineants.api.global.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.member.domain.entity.NotificationPreference;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.domain.role.domain.Role;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.errors.exception.business.MemberNotFoundException;
import co.fineants.api.global.errors.exception.business.NotFoundException;
import co.fineants.api.global.errors.exception.business.RoleNotFoundException;
import co.fineants.api.global.init.properties.AdminProperties;
import co.fineants.api.global.init.properties.ManagerProperties;
import co.fineants.api.global.init.properties.RoleProperties;
import co.fineants.api.global.init.properties.UserProperties;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.FetchStockService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetupDataLoader {
	private final RoleRepository roleRepository;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final AdminProperties adminProperties;
	private final ManagerProperties managerProperties;
	private final UserProperties userProperties;
	private final RoleProperties roleProperties;
	private final StockRepository stockRepository;
	private final StockDividendRepository stockDividendRepository;
	private final FetchDividendService fetchDividendService;
	private final FetchStockService fetchStockService;

	@Transactional
	public void setupResources() {
		setupSecurityResources();
		setupMemberResources();
		setAdminAuthentication();
		setupStockResources();
		setupStockDividendResources();
	}

	private void setupSecurityResources() {
		roleProperties.getRolePropertyList().forEach(this::saveRoleIfNotFound);
	}

	private void saveRoleIfNotFound(RoleProperties.RoleProperty roleProperty) {
		roleRepository.save(findOrCreateRole(roleProperty));
	}

	@NotNull
	private Role findOrCreateRole(RoleProperties.RoleProperty roleProperty) {
		return roleRepository.findRoleByRoleName(roleProperty.getRoleName())
			.orElseGet(roleProperty::toRoleEntity);
	}

	private void setupMemberResources() {
		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER")
			.orElseThrow(supplierNotFoundRoleException());
		Role managerRole = roleRepository.findRoleByRoleName("ROLE_MANAGER")
			.orElseThrow(supplierNotFoundRoleException());
		Role adminRole = roleRepository.findRoleByRoleName("ROLE_ADMIN")
			.orElseThrow(supplierNotFoundRoleException());

		createMemberIfNotFound(
			userProperties.getEmail(),
			userProperties.getNickname(),
			userProperties.getPassword(),
			Set.of(userRole));
		createMemberIfNotFound(
			adminProperties.getEmail(),
			adminProperties.getNickname(),
			adminProperties.getPassword(),
			Set.of(adminRole));
		createMemberIfNotFound(
			managerProperties.getEmail(),
			managerProperties.getNickname(),
			managerProperties.getPassword(),
			Set.of(managerRole));
	}

	@NotNull
	private static Supplier<NotFoundException> supplierNotFoundRoleException() {
		return () -> new RoleNotFoundException(Strings.EMPTY);
	}

	private void createMemberIfNotFound(String email, String nickname, String password,
		Set<Role> roleSet) {
		Member member = findOrCreateNewMember(email, nickname, password, roleSet);
		memberRepository.save(member);
	}

	private Member findOrCreateNewMember(String email, String nickname, String password, Set<Role> roleSet) {
		return memberRepository.findMemberByEmailAndProvider(email, "local")
			.orElseGet(supplierNewMember(email, nickname, password, roleSet));
	}

	@NotNull
	private Supplier<Member> supplierNewMember(String email, String nickname, String password, Set<Role> roleSet) {
		return () -> {
			MemberProfile profile = MemberProfile.localMemberProfile(email, nickname, passwordEncoder.encode(password),
				null);
			NotificationPreference notificationPreference = NotificationPreference.allActive();
			Member newMember = Member.createMember(profile, notificationPreference);
			Set<Long> roleIds = roleSet.stream()
				.map(Role::getId)
				.collect(Collectors.toSet());
			newMember.addRoleIds(roleIds);
			return newMember;
		};
	}

	private void setAdminAuthentication() {
		Member admin = memberRepository.findMemberByEmailAndProvider(adminProperties.getEmail(), "local")
			.orElseThrow(() -> new MemberNotFoundException(adminProperties.getEmail()));
		Role roleAdmin = roleRepository.findRoleByRoleName("ROLE_ADMIN")
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

package co.fineants.api.global.init;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractContainerBaseTest;
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
import co.fineants.api.domain.stock.service.StockCsvReader;
import co.fineants.api.global.init.properties.MemberProperties;
import co.fineants.api.infra.s3.service.WriteDividendService;
import co.fineants.api.infra.s3.service.WriteStockService;

class SetupDataLoaderTest extends AbstractContainerBaseTest {

	@Autowired
	private SetupDataLoader setupDataLoader;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MemberProperties memberProperties;

	@Autowired
	private StockCsvReader stockCsvReader;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@Autowired
	private WriteDividendService writeDividendService;

	@Autowired
	private WriteStockService writeStockService;

	@Transactional
	@DisplayName("서버는 권한 및 역할 등의 리소스들을 생성한다")
	@Test
	void setupResources() {
		// given
		int limit = 100;
		List<Stock> stocks = writeStocks(limit);
		List<StockDividend> stockDividends = writeStockDividends(stocks, limit);
		// when
		setupDataLoader.setupResources();
		// then
		assertRoles();
		assertMembers();
		assertThat(stockRepository.findAll())
			.containsExactlyInAnyOrderElementsOf(stocks);
		assertThat(stockDividendRepository.findAll())
			.hasSizeGreaterThanOrEqualTo(1)
			.containsExactlyInAnyOrderElementsOf(stockDividends);
	}

	private void assertRoles() {
		assertThat(roleRepository.findAll())
			.hasSize(3)
			.containsExactlyInAnyOrder(
				Role.create("ROLE_ADMIN", "관리자"),
				Role.create("ROLE_MANAGER", "매니저"),
				Role.create("ROLE_USER", "회원")
			);
	}

	private void assertMembers() {
		NotificationPreference notificationPreference = NotificationPreference.allActive();
		List<Member> expectedMembers = new ArrayList<>();
		for (MemberProperties.MemberAuthProperty property : memberProperties.getProperties()) {
			MemberProfile profile = MemberProfile.localMemberProfile(property.getEmail(),
				property.getNickname(), property.getPassword(), null);
			expectedMembers.add(Member.createMember(profile, notificationPreference));
		}
		assertThat(memberRepository.findAll())
			.hasSize(3)
			.containsExactlyElementsOf(expectedMembers);
	}

	private List<Stock> writeStocks(int limit) {
		List<Stock> stocks = stockCsvReader.readStockCsv().stream()
			.limit(limit)
			.toList();
		writeStockService.writeStocks(stocks);
		return stocks;
	}

	private List<StockDividend> writeStockDividends(List<Stock> stocks, int limit) {
		List<StockDividend> stockDividends = stockCsvReader.readDividendCsv(stocks).stream()
			.limit(limit)
			.toList();
		writeDividendService.writeDividend(stockDividends);
		return stockDividends;
	}
}

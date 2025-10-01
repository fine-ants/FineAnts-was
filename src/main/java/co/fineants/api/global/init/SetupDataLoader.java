package co.fineants.api.global.init;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.global.init.properties.MemberProperties;
import co.fineants.api.global.init.properties.RoleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SetupDataLoader {
	private final RoleSetupDataLoader roleSetupDataLoader;
	private final RoleProperties roleProperties;
	private final MemberSetupDataLoader memberSetupDataLoader;
	private final MemberProperties memberProperties;
	private final StockSetupDataLoader stockSetupDataLoader;
	private final StockDividendSetupDataLoader stockDividendSetupDataLoader;

	@Transactional
	public void setupResources() {
		roleSetupDataLoader.setupRoles(roleProperties);
		memberSetupDataLoader.setupMembers(memberProperties);
		stockSetupDataLoader.setupStocks();
		stockDividendSetupDataLoader.setupStockDividends();
	}
}

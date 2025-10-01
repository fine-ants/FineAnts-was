package co.fineants.api.global.init;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import co.fineants.api.global.init.properties.MemberProperties;
import co.fineants.api.global.init.properties.RoleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile(value = {"local", "dev", "release", "production"})
@Service
@RequiredArgsConstructor
@Slf4j
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
	private final RoleSetupDataLoader roleSetupDataLoader;
	private final RoleProperties roleProperties;
	private final MemberSetupDataLoader memberSetupDataLoader;
	private final MemberProperties memberProperties;
	private final StockSetupDataLoader stockSetupDataLoader;
	private final StockDividendSetupDataLoader stockDividendSetupDataLoader;
	private boolean alreadySetup = false;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (alreadySetup) {
			return;
		}
		roleSetupDataLoader.setupRoles(roleProperties);
		memberSetupDataLoader.setupMembers(memberProperties);
		stockSetupDataLoader.setupStocks();
		stockDividendSetupDataLoader.setupStockDividends();
		alreadySetup = true;
	}
}

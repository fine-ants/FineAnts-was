package co.fineants.api.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import co.fineants.api.domain.kis.properties.KisProperties;
import co.fineants.api.domain.kis.properties.KisTrIdProperties;
import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import co.fineants.api.global.common.csv.CsvProperties;
import co.fineants.api.global.init.properties.MemberProperties;
import co.fineants.api.global.init.properties.RoleProperties;
import co.fineants.api.global.security.ajax.config.ActuatorProperties;
import co.fineants.member.config.NicknameProperties;

@EnableAspectJAutoProxy
@EnableConfigurationProperties(value = {
	PortfolioProperties.class,
	KisProperties.class,
	RoleProperties.class,
	KisTrIdProperties.class,
	ActuatorProperties.class,
	NicknameProperties.class,
	CsvProperties.class,
	MemberProperties.class
})
@Configuration
public class SpringConfig {
}

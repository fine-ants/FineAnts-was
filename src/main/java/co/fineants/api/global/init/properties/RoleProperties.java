package co.fineants.api.global.init.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import co.fineants.api.domain.role.domain.Role;
import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "role")
public class RoleProperties {

	private final List<RoleProperty> properties;

	@ConstructorBinding
	public RoleProperties(List<RoleProperty> properties) {
		this.properties = properties;
	}

	@Getter
	public static class RoleProperty {
		private final String roleName;
		private final String roleDesc;

		@ConstructorBinding
		public RoleProperty(String roleName, String roleDesc) {
			this.roleName = roleName;
			this.roleDesc = roleDesc;
		}

		public Role toRoleEntity() {
			return Role.create(roleName, roleDesc);
		}
	}
}

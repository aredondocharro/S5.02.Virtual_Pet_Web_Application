package cat.itacademy.s05.t02;

import cat.itacademy.s05.t02.persistence.entity.PermissionEntity;
import cat.itacademy.s05.t02.persistence.entity.RoleEntity;
import cat.itacademy.s05.t02.persistence.entity.RoleEnum;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import cat.itacademy.s05.t02.persistence.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Set;

@SpringBootApplication
public class S05T02Application {

	public static void main(String[] args) {
		SpringApplication.run(S05T02Application.class, args);
	}

	@Bean
	CommandLineRunner init(UserRepository userRepository) {
		return args -> {
			// Create permissions
			PermissionEntity createPermission = PermissionEntity.builder()
					.name("CREATE")
					.build();
			PermissionEntity readPermission = PermissionEntity.builder()
					.name("READ")
					.build();
			PermissionEntity updatePermission = PermissionEntity.builder()
					.name("UPDATE")
					.build();
			PermissionEntity deletePermission = PermissionEntity.builder()
					.name("DELETE")
					.build();

			//Create roles
			RoleEntity roleAdmin = RoleEntity.builder()
					.roleEnum(RoleEnum.ADMIN)
					.permissionList(Set.of(createPermission, readPermission, updatePermission, deletePermission))
					.build();
			RoleEntity roleUser = RoleEntity.builder()
					.roleEnum(RoleEnum.USER)
					.permissionList(Set.of(readPermission, updatePermission))
					.build();
			RoleEntity roleGuest = RoleEntity.builder()
					.roleEnum(RoleEnum.GUEST)
					.permissionList(Set.of(readPermission))
					.build();
			RoleEntity roleDeveloper = RoleEntity.builder()
					.roleEnum(RoleEnum.DEVELOPER)
					.permissionList(Set.of(createPermission, readPermission, updatePermission))
					.build();
			RoleEntity roleTester = RoleEntity.builder()
					.roleEnum(RoleEnum.TESTER)
					.permissionList(Set.of(readPermission, updatePermission, deletePermission))
					.build();

			//Create users
			UserEntity userAlex = UserEntity.builder()
					.username("alex")
					.password("$2a$10$dEYdPzwZG.ZknO9YAMwJjO/YREpA4cZ0PvOSUbj//JtHQUSLeRJg6")
					.isEnabled(true)
					.accountNonExpired(true)
					.credentialsNonExpired(true)
					.accountNonLocked(true)
					.roles(Set.of(roleAdmin))
					.build();

			UserEntity userJohn = UserEntity.builder()
					.username("john")
					.password("$2a$10$dEYdPzwZG.ZknO9YAMwJjO/YREpA4cZ0PvOSUbj//JtHQUSLeRJg6")
					.isEnabled(true)
					.accountNonExpired(true)
					.credentialsNonExpired(true)
					.accountNonLocked(true)
					.roles(Set.of(roleUser))
					.build();

			UserEntity userAlice = UserEntity.builder()
					.username("alice")
					.password("$2a$10$dEYdPzwZG.ZknO9YAMwJjO/YREpA4cZ0PvOSUbj//JtHQUSLeRJg6")
					.isEnabled(true)
					.accountNonExpired(true)
					.credentialsNonExpired(true)
					.accountNonLocked(true)
					.roles(Set.of(roleDeveloper))
					.build();

			UserEntity userBob = UserEntity.builder()
					.username("bob")
					.password("$2a$10$dEYdPzwZG.ZknO9YAMwJjO/YREpA4cZ0PvOSUbj//JtHQUSLeRJg6")
					.isEnabled(true)
					.accountNonExpired(true)
					.credentialsNonExpired(true)
					.accountNonLocked(true)
					.roles(Set.of(roleGuest))
					.build();

			UserEntity userCharlie = UserEntity.builder()
					.username("charlie")
					.password("$2a$10$dEYdPzwZG.ZknO9YAMwJjO/YREpA4cZ0PvOSUbj//JtHQUSLeRJg6")
					.isEnabled(true)
					.accountNonExpired(true)
					.credentialsNonExpired(true)
					.accountNonLocked(true)
					.roles(Set.of(roleTester))
					.build();
			// Save users to the repository
			userRepository.saveAll(List.of(userAlex, userJohn, userAlice, userBob, userCharlie));
		};
	}
}

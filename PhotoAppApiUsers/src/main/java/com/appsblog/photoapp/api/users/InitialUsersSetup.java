package com.appsblog.photoapp.api.users;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.appsblog.photoapp.api.users.data.AuthorityEntity;
import com.appsblog.photoapp.api.users.data.AuthorityRepository;
import com.appsblog.photoapp.api.users.data.RoleEntity;
import com.appsblog.photoapp.api.users.data.RoleRepository;
import com.appsblog.photoapp.api.users.data.UserEntity;
import com.appsblog.photoapp.api.users.data.UsersRepository;
import com.appsblog.photoapp.api.users.shared.Roles;

import jakarta.transaction.Transactional;

@Component
public class InitialUsersSetup {
	
	@Autowired
	AuthorityRepository authorityRepository;
	
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	UsersRepository usersRepository;
	
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Transactional
	@EventListener
	public void onApplicationEvent(ApplicationReadyEvent event) {
		logger.info("From Application Ready Event");
		AuthorityEntity readAuthority = createAuthority("READ");
		AuthorityEntity writeAuthority = createAuthority("WRITE");
		AuthorityEntity deleteAuthority = createAuthority("DELETE");
		
		createRole(Roles.ROLE_USER.name(), List.of(readAuthority, writeAuthority));
		RoleEntity roleAdmin = createRole(Roles.ROLE_ADMIN.name(), List.of(readAuthority, writeAuthority, deleteAuthority));
	
		if (roleAdmin == null) return;
		
		UserEntity adminUser = new UserEntity();
		adminUser.setFirstName("Admin");
		adminUser.setLastName("01");
		adminUser.setEmail("admin@test.com");
		adminUser.setUserId(UUID.randomUUID().toString());
		adminUser.setEncryptedPassword(bCryptPasswordEncoder.encode("testadmin"));
		adminUser.setRoles(List.of(roleAdmin));
		
		UserEntity storedAdminUser = usersRepository.findByEmail("admin@test.com");
		
		if (storedAdminUser == null) {
			usersRepository.save(adminUser);
		}
	}
	
	@Transactional
	private AuthorityEntity createAuthority(String name) {
		AuthorityEntity authority = authorityRepository.findByName(name);
		
		if (authority == null) {
			authority = new AuthorityEntity(name);
			authorityRepository.save(authority);
		}
		
		return authority;
	}
	
	@Transactional
	private RoleEntity createRole(String name, Collection<AuthorityEntity> authorities) {
		RoleEntity role = roleRepository.findByName(name);
		
		if (role == null) {
			role = new RoleEntity(name, authorities);
			roleRepository.save(role);
		}
		
		return role;
	}
}

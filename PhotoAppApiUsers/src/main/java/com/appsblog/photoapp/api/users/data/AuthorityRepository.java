package com.appsblog.photoapp.api.users.data;

import org.springframework.data.repository.CrudRepository;

public interface AuthorityRepository extends CrudRepository<AuthorityEntity, Long> {

	AuthorityEntity findByName(String name);
}

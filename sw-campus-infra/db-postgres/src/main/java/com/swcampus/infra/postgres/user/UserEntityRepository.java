package com.swcampus.infra.postgres.user;

import org.springframework.stereotype.Repository;

import com.swcampus.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserEntityRepository implements UserRepository {

	private final UserJpaRepository userJpaRepository;
}

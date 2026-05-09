package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Role;
import com.keldorn.phenylalaninecalculatorapi.domain.enums.Roles;
import com.keldorn.phenylalaninecalculatorapi.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role findByRoleNameOrThrow(Roles role) {
        log.debug("Finding Role by Roles: {}", role);
        return roleRepository.findByName(role)
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
    }

}

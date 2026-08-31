package io.harbor.example.domain.role;

import io.harbor.example.domain.role.dto.Role;
import io.harbor.example.domain.role.dto.command.RoleChangeCommand;
import io.harbor.example.shared.dto.result.CommonCreateResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleFacade {
    private final RoleRepository roleRepository;

    public CommonCreateResult<UUID> create(@NonNull RoleChangeCommand command) {
        RoleEntity entity = RoleEntity.of(command);
        roleRepository.insert(entity);
        return new CommonCreateResult<>(entity.getId());
    }

    public Optional<Role> findById(@NonNull UUID id) {
        return roleRepository.findById(id).map(RoleEntity::toDto);
    }

    public void update(@NonNull UUID id, @NonNull RoleChangeCommand command) {
        RoleEntity roleEntity = roleRepository.findByIdForUpdateOrThrow(id);
        roleEntity.update(command);
        roleRepository.update(roleEntity);
    }

    void deleteByIdV1(@NonNull UUID id) {
        RoleEntity roleEntity = roleRepository.findByIdForUpdateOrThrow(id);
        roleRepository.delete(roleEntity);
    }

    void deleteByIdV2(@NonNull UUID id) {
        roleRepository.deleteById(id);
    }
}

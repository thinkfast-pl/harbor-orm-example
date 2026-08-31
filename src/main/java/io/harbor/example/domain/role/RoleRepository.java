package io.harbor.example.domain.role;

import io.harbor.api.HarborSession;
import io.harbor.core.repository.EntityRepository;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
class RoleRepository extends EntityRepository<RoleEntity, UUID> {
    private static final QRoleEntity ROLE = new QRoleEntity("r");

    RoleRepository(@NonNull HarborSession session) {
        super(session, ROLE);
    }
}

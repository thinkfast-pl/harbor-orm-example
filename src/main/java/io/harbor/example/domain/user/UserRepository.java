package io.harbor.example.domain.user;

import io.harbor.api.HarborSession;
import io.harbor.core.repository.EntityRepository;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

@Repository
class UserRepository extends EntityRepository<UserEntity, Long> {
    private static final QUserEntity USER = new QUserEntity("u");

    UserRepository(@NonNull HarborSession session) {
        super(session, USER);
    }
}

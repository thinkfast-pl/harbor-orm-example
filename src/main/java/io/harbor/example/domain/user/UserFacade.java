package io.harbor.example.domain.user;

import io.harbor.example.domain.user.dto.User;
import io.harbor.example.domain.user.dto.command.UserCreateCommand;
import io.harbor.example.domain.user.dto.command.UserUpdateCommand;
import io.harbor.example.shared.dto.result.CommonCreateResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserFacade {
    private final UserRepository userRepository;

    public CommonCreateResult<Long> create(@NonNull UserCreateCommand command) {
        UserEntity userEntity = UserEntity.of(command);
        userRepository.insert(userEntity);
        return new CommonCreateResult<>(userEntity.getId());
    }

    public User findById(@NonNull Long id) {
        return userRepository.findByIdOrThrow(id).toDto();
    }

    public void update(@NonNull Long id, @NonNull UserUpdateCommand command) {
        UserEntity userEntity = userRepository.findByIdForUpdateOrThrow(id);
        userEntity.update(command);
        userRepository.update(userEntity);
    }

    public void updateEmail(@NonNull Long id, @NonNull String email) {
        UserEntity userEntity = userRepository.findByIdForUpdateOrThrow(id);
        userEntity.updateEmail(email);
        userRepository.update(userEntity);
    }

    public void delete(@NonNull Long id) {
        UserEntity userEntity = userRepository.findByIdOrThrow(id);
        userRepository.delete(userEntity);
    }
}

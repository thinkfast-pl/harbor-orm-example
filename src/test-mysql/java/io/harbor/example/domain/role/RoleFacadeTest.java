package io.harbor.example.domain.role;

import io.harbor.api.repository.EntityNotFoundException;
import io.harbor.example.MySqlIntegrationTest;
import io.harbor.example.domain.role.dto.Permission;
import io.harbor.example.domain.role.dto.Role;
import io.harbor.example.domain.role.dto.command.RoleChangeCommand;
import io.harbor.example.shared.dto.result.CommonCreateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleFacadeTest extends MySqlIntegrationTest {

    @Autowired
    private RoleFacade roleFacade;

    @Test
    void createReturnsGeneratedId() {
        CommonCreateResult<UUID> result = roleFacade.create(new RoleChangeCommand("admin", List.of(Permission.ROLE_ADD)));

        assertThat(result.getId()).isNotNull();
    }

    @Test
    void createdRoleCanBeFoundById() {
        List<Permission> permissions = List.of(Permission.ROLE_ADD, Permission.USER_EDIT);
        UUID id = roleFacade.create(new RoleChangeCommand("moderator", permissions)).getId();

        Optional<Role> found = roleFacade.findById(id);

        assertThat(found).isPresent();
        Role role = found.get();
        assertThat(role.getId()).isEqualTo(id);
        assertThat(role.getName()).isEqualTo("moderator");
        assertThat(role.getPermissions()).containsExactlyInAnyOrderElementsOf(permissions);
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(roleFacade.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void updateChangesNameAndReplacesPermissions() {
        UUID id = roleFacade.create(new RoleChangeCommand("editor", List.of(Permission.ROLE_ADD, Permission.ROLE_EDIT))).getId();

        roleFacade.update(id, new RoleChangeCommand("editor-renamed", List.of(Permission.USER_ADD, Permission.USER_DELETE)));

        Optional<Role> found = roleFacade.findById(id);
        assertThat(found).isPresent();
        Role role = found.get();
        assertThat(role.getName()).isEqualTo("editor-renamed");
        assertThat(role.getPermissions()).containsExactlyInAnyOrder(Permission.USER_ADD, Permission.USER_DELETE);
    }

    @Test
    void updateThrowsForUnknownId() {
        RoleChangeCommand command = new RoleChangeCommand("ghost", List.of(Permission.ROLE_ADD));

        assertThatThrownBy(() -> roleFacade.update(UUID.randomUUID(), command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteByIdV1RemovesRole() {
        UUID id = roleFacade.create(new RoleChangeCommand("temporary-v1", List.of(Permission.ROLE_DELETE))).getId();

        roleFacade.deleteByIdV1(id);

        assertThat(roleFacade.findById(id)).isEmpty();
    }

    @Test
    void deleteByIdV1ThrowsForUnknownId() {
        assertThatThrownBy(() -> roleFacade.deleteByIdV1(UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteByIdV2RemovesRole() {
        UUID id = roleFacade.create(new RoleChangeCommand("temporary-v2", List.of(Permission.ROLE_DELETE))).getId();

        roleFacade.deleteByIdV2(id);

        assertThat(roleFacade.findById(id)).isEmpty();
    }

    @Test
    void deleteByIdV2IsNoOpForUnknownId() {
        UUID existingId = roleFacade.create(new RoleChangeCommand("survivor", List.of(Permission.USER_EDIT))).getId();

        assertThatCode(() -> roleFacade.deleteByIdV2(UUID.randomUUID())).doesNotThrowAnyException();

        assertThat(roleFacade.findById(existingId)).isPresent();
    }
}

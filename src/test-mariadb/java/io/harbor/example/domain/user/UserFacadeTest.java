package io.harbor.example.domain.user;

import io.harbor.api.repository.EntityNotFoundException;
import io.harbor.example.MariaDbIntegrationTest;
import io.harbor.example.domain.role.RoleFacade;
import io.harbor.example.domain.role.dto.Permission;
import io.harbor.example.domain.role.dto.command.RoleChangeCommand;
import io.harbor.example.domain.user.dto.User;
import io.harbor.example.domain.user.dto.UserType;
import io.harbor.example.domain.user.dto.command.UserCreateCommand;
import io.harbor.example.domain.user.dto.command.UserUpdateCommand;
import io.harbor.example.shared.dto.Address;
import io.harbor.example.shared.model.CallbackLog;
import io.harbor.example.shared.dto.result.CommonCreateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserFacadeTest extends MariaDbIntegrationTest {

    private static final long UNKNOWN_ID = 999_999_999L;
    private static final Address RESIDENTIAL_ADDRESS = new Address("Main St", "Springfield", 10, "USA");
    private static final Address CONTACT_ADDRESS = new Address("Oak Ave", "Shelbyville", 5, "USA");
    private static final Address UPDATED_RESIDENTIAL_ADDRESS = new Address("New St", "Ogdenville", 7, "Canada");
    private static final Address UPDATED_CONTACT_ADDRESS = new Address("Elm Rd", "North Haverbrook", 3, "Canada");
    private static final Address NULL_FIELDS_ADDRESS = new Address(null, null, null, null);

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private RoleFacade roleFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createReturnsGeneratedId() {
        UUID roleId = createRole("user-create");

        CommonCreateResult<Long> result = userFacade.create(createCommand("create@example.com", roleId));

        assertThat(result.getId()).isNotNull();
    }

    @Test
    void createdUserCanBeFoundById() {
        UUID roleId = createRole("user-find");
        Long id = userFacade.create(createCommand("find@example.com", roleId)).getId();

        User user = userFacade.findById(id);

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo("find@example.com");
        assertThat(user.getType()).isEqualTo(UserType.REGULAR);
        assertThat(user.isActive()).isTrue();
        assertThat(user.getResidentialAddress()).isEqualTo(RESIDENTIAL_ADDRESS);
        assertThat(user.getContactAddress()).isEqualTo(CONTACT_ADDRESS);
        assertThat(user.getRolesIds()).containsExactly(roleId);
    }

    @Test
    void findByIdThrowsForUnknownId() {
        assertThatThrownBy(() -> userFacade.findById(UNKNOWN_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateChangesFieldsAndReplacesRoles() {
        UUID initialRoleId = createRole("user-update-initial");
        UUID replacementRoleId = createRole("user-update-replacement");
        Long id = userFacade.create(createCommand("update@example.com", initialRoleId)).getId();

        userFacade.update(id, updateCommand(replacementRoleId));

        User user = userFacade.findById(id);
        assertThat(user.getEmail()).isEqualTo("update@example.com");
        assertThat(user.getType()).isEqualTo(UserType.ADMIN);
        assertThat(user.isActive()).isFalse();
        assertThat(user.getResidentialAddress()).isEqualTo(UPDATED_RESIDENTIAL_ADDRESS);
        assertThat(user.getContactAddress()).isEqualTo(UPDATED_CONTACT_ADDRESS);
        assertThat(user.getRolesIds()).containsExactly(replacementRoleId);
    }

    @Test
    void updateThrowsForUnknownId() {
        UUID roleId = createRole("user-update-unknown");
        UserUpdateCommand command = updateCommand(roleId);

        assertThatThrownBy(() -> userFacade.update(UNKNOWN_ID, command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateEmailIsSilentlyIgnoredBecauseEmailIsNotUpdatable() {
        UUID roleId = createRole("user-email-not-updatable");
        Long id = userFacade.create(createCommand("immutable@example.com", roleId)).getId();

        userFacade.updateEmail(id, "changed@example.com");

        assertThat(userFacade.findById(id).getEmail()).isEqualTo("immutable@example.com");
    }

    @Test
    void contactAddressWithNullFieldsIsPersistedAndReadBack() {
        UUID roleId = createRole("user-null-contact-read");
        Long id = userFacade.create(new UserCreateCommand(
                "null-contact-read@example.com", UserType.REGULAR, true,
                RESIDENTIAL_ADDRESS, NULL_FIELDS_ADDRESS, Set.of(roleId))).getId();

        User user = userFacade.findById(id);

        assertThat(user.getContactAddress()).isEqualTo(NULL_FIELDS_ADDRESS);
        assertThat(user.getResidentialAddress()).isEqualTo(RESIDENTIAL_ADDRESS);
    }

    @Test
    void contactAddressWithNullFieldsCanBeUpdatedToNonNulls() {
        UUID roleId = createRole("user-null-contact-update");
        Long id = userFacade.create(new UserCreateCommand(
                "null-contact-update@example.com", UserType.REGULAR, true,
                RESIDENTIAL_ADDRESS, NULL_FIELDS_ADDRESS, Set.of(roleId))).getId();

        userFacade.update(id, new UserUpdateCommand(
                UserType.REGULAR, true, RESIDENTIAL_ADDRESS, UPDATED_CONTACT_ADDRESS, Set.of(roleId)));

        User user = userFacade.findById(id);
        assertThat(user.getContactAddress()).isEqualTo(UPDATED_CONTACT_ADDRESS);
        assertThat(user.getResidentialAddress()).isEqualTo(RESIDENTIAL_ADDRESS);
    }

    @Test
    void userCreatedWithoutRolesCanHaveRolesAddedOnUpdate() {
        Long id = userFacade.create(new UserCreateCommand(
                "no-roles-then-some@example.com", UserType.REGULAR, true,
                RESIDENTIAL_ADDRESS, CONTACT_ADDRESS, Set.of())).getId();

        assertThat(userFacade.findById(id).getRolesIds()).isEmpty();

        UUID roleId = createRole("user-roles-added");
        userFacade.update(id, new UserUpdateCommand(
                UserType.REGULAR, true, RESIDENTIAL_ADDRESS, CONTACT_ADDRESS, Set.of(roleId)));

        assertThat(userFacade.findById(id).getRolesIds()).containsExactly(roleId);
    }

    @Test
    void userCreatedWithRolesCanHaveAllRolesRemovedOnUpdate() {
        UUID roleId = createRole("user-roles-removed");
        Long id = userFacade.create(createCommand("some-roles-then-none@example.com", roleId)).getId();

        userFacade.update(id, new UserUpdateCommand(
                UserType.REGULAR, true, RESIDENTIAL_ADDRESS, CONTACT_ADDRESS, Set.of()));

        assertThat(userFacade.findById(id).getRolesIds()).isEmpty();
    }

    @Test
    void deleteRemovesUser() {
        UUID roleId = createRole("user-delete");
        Long id = userFacade.create(createCommand("delete@example.com", roleId)).getId();

        userFacade.delete(id);

        assertThatThrownBy(() -> userFacade.findById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteThrowsForUnknownId() {
        assertThatThrownBy(() -> userFacade.delete(UNKNOWN_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createThrowsForEmailWithoutAtChar() {
        UUID roleId = createRole("user-invalid-email");
        UserCreateCommand command = createCommand("invalid-email.example.com", roleId);

        assertThatThrownBy(() -> userFacade.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email: invalid-email.example.com");
    }

    @Test
    void createThrowsForCountryNamedCountry() {
        UUID roleId = createRole("user-invalid-country");
        UserCreateCommand command = new UserCreateCommand(
                "invalid-country@example.com", UserType.REGULAR, true,
                new Address("Main St", "Springfield", 10, "Country"), CONTACT_ADDRESS, Set.of(roleId));

        assertThatThrownBy(() -> userFacade.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid country: Country");
    }

    @Test
    void updateEmailThrowsForEmailWithoutAtChar() {
        UUID roleId = createRole("user-invalid-email-update");
        Long id = userFacade.create(createCommand("valid-before-update@example.com", roleId)).getId();

        assertThatThrownBy(() -> userFacade.updateEmail(id, "invalid-email.example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email on update: invalid-email.example.com");
    }

    @Test
    void updateThrowsForCountryNamedCountry() {
        UUID roleId = createRole("user-invalid-country-update");
        Long id = userFacade.create(createCommand("valid-country-before-update@example.com", roleId)).getId();

        UserUpdateCommand command = new UserUpdateCommand(
                UserType.REGULAR, true,
                new Address("Main St", "Springfield", 10, "Country"), CONTACT_ADDRESS, Set.of(roleId));

        assertThatThrownBy(() -> userFacade.update(id, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid country on update: Country");
    }

    @Test
    void deleteThrowsForEmailWithoutAtChar() {
        Long id = insertUserRowBypassingValidation("invalid-delete.example.com", "USA");

        assertThatThrownBy(() -> userFacade.delete(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email on delete: invalid-delete.example.com");
    }

    @Test
    void deleteThrowsForCountryNamedCountry() {
        Long id = insertUserRowBypassingValidation("valid-delete@example.com", "Country");

        assertThatThrownBy(() -> userFacade.delete(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid country on delete: Country");
    }

    @Test
    void deleteByIdWithoutFetchingInvokesPreDeleteCallbacks() {
        Long id = insertUserRowBypassingValidation("invalid-delete-by-id.example.com", "USA");

        assertThatThrownBy(() -> userRepository.deleteById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email on delete: invalid-delete-by-id.example.com");
    }

    @Test
    void postInsertCallbacksAreInvokedOnEntityAndEmbeddables() {
        UUID roleId = createRole("user-post-insert");
        CallbackLog.clear();

        userFacade.create(createCommand("post-insert@example.com", roleId));

        assertThat(CallbackLog.entries()).containsExactly(
                "address post-insert: Springfield",
                "address post-insert: Shelbyville",
                "user post-insert: post-insert@example.com");
    }

    @Test
    void postUpdateCallbacksAreInvokedOnEntityAndEmbeddables() {
        UUID roleId = createRole("user-post-update");
        Long id = userFacade.create(createCommand("post-update@example.com", roleId)).getId();
        CallbackLog.clear();

        userFacade.update(id, updateCommand(roleId));

        assertThat(CallbackLog.entries()).containsExactly(
                "address post-update: Ogdenville",
                "address post-update: North Haverbrook",
                "user post-update: post-update@example.com");
    }

    @Test
    void postDeleteCallbacksAreInvokedOnEntityAndEmbeddables() {
        UUID roleId = createRole("user-post-delete");
        Long id = userFacade.create(createCommand("post-delete@example.com", roleId)).getId();
        CallbackLog.clear();

        userFacade.delete(id);

        assertThat(CallbackLog.entries()).containsExactly(
                "address post-delete: Springfield",
                "address post-delete: Shelbyville",
                "user post-delete: post-delete@example.com");
    }

    private Long insertUserRowBypassingValidation(String email, String residentialCountry) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO users (email, type, active, residential_country) VALUES (?, 'REGULAR', true, ?) RETURNING id",
                Long.class, email, residentialCountry);
    }

    private UUID createRole(String name) {
        return roleFacade.create(new RoleChangeCommand(name, List.of(Permission.USER_EDIT))).getId();
    }

    private static UserCreateCommand createCommand(String email, UUID roleId) {
        return new UserCreateCommand(email, UserType.REGULAR, true, RESIDENTIAL_ADDRESS, CONTACT_ADDRESS, Set.of(roleId));
    }

    private static UserUpdateCommand updateCommand(UUID roleId) {
        return new UserUpdateCommand(UserType.ADMIN, false, UPDATED_RESIDENTIAL_ADDRESS, UPDATED_CONTACT_ADDRESS, Set.of(roleId));
    }
}

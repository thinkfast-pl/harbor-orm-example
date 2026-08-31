package io.harbor.example.domain.role;

import io.harbor.api.annotations.*;
import io.harbor.api.dialect.StandardDialects;
import io.harbor.example.domain.role.dto.Permission;
import io.harbor.example.domain.role.dto.Role;
import io.harbor.example.domain.role.dto.command.RoleChangeCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(table = "roles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
class RoleEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ElementCollection(
            table = "roles_permissions",
            joinColumns = @JoinColumn(name = "role_id", fieldType = UUID.class)
    )
    private List<
            @Column(name = "permission", nullable = false)
            @Type(dialect = StandardDialects.POSTGRES, columnType = "permission_type")
            @Enumerated Permission> permissions;

    static RoleEntity of(@NonNull RoleChangeCommand command) {
        return new RoleEntity(
                UUID.randomUUID(),
                command.getName(),
                new ArrayList<>(command.getPermissions())
        );
    }

    void update(@NonNull RoleChangeCommand command) {
        this.name = command.getName();
        this.permissions.clear();
        this.permissions.addAll(command.getPermissions());
    }

    Role toDto() {
        return new Role(id, name, new ArrayList<>(permissions));
    }
}

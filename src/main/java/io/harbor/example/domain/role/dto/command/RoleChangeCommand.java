package io.harbor.example.domain.role.dto.command;

import io.harbor.example.domain.role.dto.Permission;
import lombok.Value;

import java.util.List;

@Value
public class RoleChangeCommand {
    String name;
    List<Permission> permissions;
}

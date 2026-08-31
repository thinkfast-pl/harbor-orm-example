package io.harbor.example.domain.user.dto.command;

import io.harbor.example.domain.user.dto.UserType;
import io.harbor.example.shared.dto.Address;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
public class UserUpdateCommand {
    UserType type;
    boolean active;
    Address residentialAddress;
    Address contactAddress;
    Set<UUID> rolesIds;
}

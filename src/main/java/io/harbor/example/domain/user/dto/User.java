package io.harbor.example.domain.user.dto;

import io.harbor.example.shared.dto.Address;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
public class User {
    Long id;
    String email;
    UserType type;
    boolean active;
    Address residentialAddress;
    Address contactAddress;
    Set<UUID> rolesIds;
}

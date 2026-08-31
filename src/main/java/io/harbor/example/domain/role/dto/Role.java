package io.harbor.example.domain.role.dto;

import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class Role {
    UUID id;
    String name;
    List<Permission> permissions;
}

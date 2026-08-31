package io.harbor.example.domain.customer.dto.command;

import io.harbor.example.shared.dto.Address;
import lombok.Value;

import java.util.List;

@Value
public class CustomerChangeCommand {
    String email;
    List<Address> addresses;
}

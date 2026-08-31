package io.harbor.example.domain.customer.dto;

import io.harbor.example.shared.dto.Address;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
public class Customer {
    Long id;
    String email;
    LocalDate createDate;
    LocalDate updateDate;
    List<Address> addresses;
}

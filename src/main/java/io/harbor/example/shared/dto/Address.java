package io.harbor.example.shared.dto;

import lombok.Value;

@Value
public class Address {
    String address;
    String town;
    Integer buildingNo;
    String country;
}

package io.harbor.example.shared.model;

import io.harbor.api.annotations.Column;
import io.harbor.api.annotations.Embeddable;
import io.harbor.api.annotations.PostDelete;
import io.harbor.api.annotations.PostInsert;
import io.harbor.api.annotations.PostUpdate;
import io.harbor.api.annotations.PreDelete;
import io.harbor.api.annotations.PreInsert;
import io.harbor.api.annotations.PreUpdate;
import io.harbor.example.shared.dto.Address;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class AddressEmbedded {

    @Column(nullable = true)
    private String address;

    @Column(nullable = true)
    private String town;

    @Column(nullable = true)
    private Integer buildingNo;

    @Column(nullable = true)
    private String country;

    public static AddressEmbedded of(@NonNull Address address) {
        return new AddressEmbedded(address.getAddress(), address.getTown(), address.getBuildingNo(), address.getCountry());
    }

    @PreInsert
    private void validateCountryOnInsert() {
        validateCountry("Invalid country: ");
    }

    @PreUpdate
    private void validateCountryOnUpdate() {
        validateCountry("Invalid country on update: ");
    }

    @PreDelete
    private void validateCountryOnDelete() {
        validateCountry("Invalid country on delete: ");
    }

    private void validateCountry(String messagePrefix) {
        if ("Country".equals(country)) {
            throw new IllegalArgumentException(messagePrefix + country);
        }
    }

    @PostInsert
    private void logPostInsert() {
        CallbackLog.record("address post-insert: " + town);
    }

    @PostUpdate
    private void logPostUpdate() {
        CallbackLog.record("address post-update: " + town);
    }

    @PostDelete
    private void logPostDelete() {
        CallbackLog.record("address post-delete: " + town);
    }

    public void update(@NonNull Address address) {
        this.address = address.getAddress();
        this.town = address.getTown();
        this.buildingNo = address.getBuildingNo();
        this.country = address.getCountry();
    }

    public Address toDto() {
        return new Address(address, town, buildingNo, country);
    }
}

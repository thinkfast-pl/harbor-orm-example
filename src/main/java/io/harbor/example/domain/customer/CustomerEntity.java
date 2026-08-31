package io.harbor.example.domain.customer;

import io.harbor.api.annotations.*;
import io.harbor.example.domain.customer.dto.Customer;
import io.harbor.example.domain.customer.dto.command.CustomerChangeCommand;
import io.harbor.example.shared.model.AddressEmbedded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity(table = "customers")
@NoArgsConstructor
@AllArgsConstructor
@Getter
class CustomerEntity {

    @Id
    @SequenceGenerated(sequence = "customers_seq")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(updatable = false, nullable = false)
    private LocalDate createDate;

    @Column(insertable = false, nullable = true)
    private LocalDate updateDate;

    @ElementCollection(
            table = "customers_addresses",
            joinColumns = @JoinColumn(name = "customer_id", fieldType = Long.class)
    )
    private List<@Embedded AddressEmbedded> addresses;

    static CustomerEntity of(CustomerChangeCommand command) {
        return of(command, null);
    }

    static CustomerEntity of(CustomerChangeCommand command, LocalDate updateDate) {
        return new CustomerEntity(
                null,
                command.
                        getEmail(),
                LocalDate.now(),
                updateDate,
                command.getAddresses().stream().map(AddressEmbedded::of).toList()
        );
    }

    void updateCreateDate(LocalDate createDate) {
        this.createDate = createDate;
    }

    void update(CustomerChangeCommand command) {
        email = command.getEmail();
        updateDate = LocalDate.now();
        addresses = command.getAddresses().stream().map(AddressEmbedded::of).toList();
    }

    Customer toDto() {
        return new Customer(
                id,
                email,
                createDate,
                updateDate,
                addresses.stream().map(AddressEmbedded::toDto).toList()
        );
    }
}

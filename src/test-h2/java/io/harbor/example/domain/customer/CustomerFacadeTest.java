package io.harbor.example.domain.customer;

import io.harbor.api.repository.EntityNotFoundException;
import io.harbor.example.H2IntegrationTest;
import io.harbor.example.domain.customer.dto.Customer;
import io.harbor.example.domain.customer.dto.command.CustomerChangeCommand;
import io.harbor.example.shared.dto.Address;
import io.harbor.example.shared.dto.result.CommonCreateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerFacadeTest extends H2IntegrationTest {

    private static final long UNKNOWN_ID = 999_999_999L;
    private static final Address FIRST_ADDRESS = new Address("Main St", "Springfield", 10, "USA");
    private static final Address SECOND_ADDRESS = new Address("Oak Ave", "Shelbyville", 5, "USA");
    private static final Address UPDATED_ADDRESS = new Address("New St", "Ogdenville", 7, "Canada");
    private static final Address NULL_FIELDS_ADDRESS = new Address(null, null, null, null);

    @Autowired
    private CustomerFacade customerFacade;

    @Test
    void createReturnsGeneratedId() {
        CommonCreateResult<Long> result = customerFacade.create(
                new CustomerChangeCommand("create@example.com", List.of(FIRST_ADDRESS)));

        assertThat(result.getId()).isNotNull();
    }

    @Test
    void createdCustomerCanBeFoundById() {
        List<Address> addresses = List.of(FIRST_ADDRESS, SECOND_ADDRESS);
        Long id = createCustomer("find@example.com", addresses);

        Optional<Customer> found = customerFacade.findById(id);

        assertThat(found).isPresent();
        Customer customer = found.get();
        assertThat(customer.getId()).isEqualTo(id);
        assertThat(customer.getEmail()).isEqualTo("find@example.com");
        assertThat(customer.getCreateDate()).isNotNull();
        assertThat(customer.getUpdateDate()).isNull();
        assertThat(customer.getAddresses()).containsExactlyInAnyOrderElementsOf(addresses);
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(customerFacade.findById(UNKNOWN_ID)).isEmpty();
    }

    @Test
    void updateChangesEmailAndReplacesAddresses() {
        Long id = createCustomer("update@example.com", List.of(FIRST_ADDRESS, SECOND_ADDRESS));
        LocalDate createDate = customerFacade.findById(id).orElseThrow().getCreateDate();

        customerFacade.update(id, new CustomerChangeCommand("update-renamed@example.com", List.of(UPDATED_ADDRESS)));

        Optional<Customer> found = customerFacade.findById(id);
        assertThat(found).isPresent();
        Customer customer = found.get();
        assertThat(customer.getEmail()).isEqualTo("update-renamed@example.com");
        assertThat(customer.getCreateDate()).isEqualTo(createDate);
        assertThat(customer.getUpdateDate()).isNotNull();
        assertThat(customer.getAddresses()).containsExactlyInAnyOrder(UPDATED_ADDRESS);
    }

    @Test
    void updateThrowsForUnknownId() {
        CustomerChangeCommand command = new CustomerChangeCommand("ghost@example.com", List.of(FIRST_ADDRESS));

        assertThatThrownBy(() -> customerFacade.update(UNKNOWN_ID, command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateCreateDateIsSilentlyIgnoredBecauseCreateDateIsNotUpdatable() {
        Long id = createCustomer("immutable-create-date@example.com", List.of(FIRST_ADDRESS));
        LocalDate originalCreateDate = customerFacade.findById(id).orElseThrow().getCreateDate();

        customerFacade.updateCreateDate(id, originalCreateDate.minusYears(10));

        assertThat(customerFacade.findById(id).orElseThrow().getCreateDate()).isEqualTo(originalCreateDate);
    }

    @Test
    void updateDateProvidedOnCreateIsSilentlyIgnoredBecauseUpdateDateIsNotInsertable() {
        Long id = customerFacade.createWithUpdateDate(
                new CustomerChangeCommand("not-insertable-update-date@example.com", List.of(FIRST_ADDRESS)),
                LocalDate.of(2020, 1, 1)).getId();

        assertThat(customerFacade.findById(id).orElseThrow().getUpdateDate()).isNull();
    }

    @Test
    void deleteRemovesCustomer() {
        Long id = createCustomer("delete@example.com", List.of(FIRST_ADDRESS));

        customerFacade.deleteById(id);

        assertThat(customerFacade.findById(id)).isEmpty();
    }

    @Test
    void deleteIsNoOpForUnknownId() {
        Long existingId = createCustomer("survivor@example.com", List.of(FIRST_ADDRESS));

        assertThatCode(() -> customerFacade.deleteById(UNKNOWN_ID)).doesNotThrowAnyException();

        assertThat(customerFacade.findById(existingId)).isPresent();
    }

    @Test
    void addressWithNullFieldsIsPersistedAndReadBack() {
        Long id = createCustomer("null-address@example.com", List.of(NULL_FIELDS_ADDRESS));

        Optional<Customer> found = customerFacade.findById(id);

        assertThat(found).isPresent();
        assertThat(found.get().getAddresses()).containsExactlyInAnyOrder(NULL_FIELDS_ADDRESS);
    }

    @Test
    void customerCreatedWithEmptyAddressesCanHaveAddressesAdded() {
        Long id = createCustomer("no-addresses-then-some@example.com", List.of());

        assertThat(customerFacade.findById(id).orElseThrow().getAddresses()).isEmpty();

        customerFacade.update(id, new CustomerChangeCommand("no-addresses-then-some@example.com", List.of(FIRST_ADDRESS)));

        assertThat(customerFacade.findById(id).orElseThrow().getAddresses()).containsExactlyInAnyOrder(FIRST_ADDRESS);
    }

    @Test
    void customerCreatedWithAddressesCanHaveAllAddressesRemoved() {
        Long id = createCustomer("some-addresses-then-none@example.com", List.of(FIRST_ADDRESS, SECOND_ADDRESS));

        customerFacade.update(id, new CustomerChangeCommand("some-addresses-then-none@example.com", List.of()));

        assertThat(customerFacade.findById(id).orElseThrow().getAddresses()).isEmpty();
    }

    @Test
    void streamAllDeliversAllCustomersWithAddresses() {
        Long withAddressesId = createCustomer("stream-addresses@example.com", List.of(FIRST_ADDRESS, SECOND_ADDRESS));
        Long withoutAddressesId = createCustomer("stream-no-addresses@example.com", List.of());
        List<Customer> expected = List.of(
                customerFacade.findById(withAddressesId).orElseThrow(),
                customerFacade.findById(withoutAddressesId).orElseThrow());

        List<Customer> streamed = new ArrayList<>();
        customerFacade.streamAll(streamed::add);

        assertThat(streamed)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expected);
    }

    @Test
    void streamAllConsumesNothingWhenNoCustomersExist() {
        List<Customer> streamed = new ArrayList<>();

        customerFacade.streamAll(streamed::add);

        assertThat(streamed).isEmpty();
    }

    @Test
    void streamAllDeliversAllCustomersWhenCountExceedsFetchSize() {
        int customerCount = 1001; // one more than the stream's default fetch size of 1000
        List<String> expectedEmails = new ArrayList<>();
        for (int i = 0; i < customerCount; i++) {
            String email = "stream-bulk-" + i + "@example.com";
            createCustomer(email, List.of());
            expectedEmails.add(email);
        }

        List<String> streamedEmails = new ArrayList<>();
        customerFacade.streamAll(customer -> streamedEmails.add(customer.getEmail()));

        assertThat(streamedEmails).containsExactlyInAnyOrderElementsOf(expectedEmails);
    }

    private Long createCustomer(String email, List<Address> addresses) {
        return customerFacade.create(new CustomerChangeCommand(email, addresses)).getId();
    }
}

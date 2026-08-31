package io.harbor.example.domain.customer;

import io.harbor.example.domain.customer.dto.Customer;
import io.harbor.example.domain.customer.dto.command.CustomerChangeCommand;
import io.harbor.example.shared.dto.result.CommonCreateResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerFacade {
    private final CustomerRepository customerRepository;

    public CommonCreateResult<Long> create(@NonNull CustomerChangeCommand command) {
        CustomerEntity entity = CustomerEntity.of(command);
        customerRepository.insert(entity);
        return new CommonCreateResult<>(entity.getId());
    }

    CommonCreateResult<Long> createWithUpdateDate(@NonNull CustomerChangeCommand command, @NonNull LocalDate updateDate) {
        CustomerEntity entity = CustomerEntity.of(command, updateDate);
        customerRepository.insert(entity);
        return new CommonCreateResult<>(entity.getId());
    }

    public Optional<Customer> findById(@NonNull Long id) {
        return customerRepository.findById(id).map(CustomerEntity::toDto);
    }

    void update(@NonNull Long id, @NonNull CustomerChangeCommand command) {
        CustomerEntity entity = customerRepository.findByIdForUpdateOrThrow(id);
        entity.update(command);
        customerRepository.update(entity);
    }

    void updateCreateDate(@NonNull Long id, @NonNull LocalDate createDate) {
        CustomerEntity entity = customerRepository.findByIdForUpdateOrThrow(id);
        entity.updateCreateDate(createDate);
        customerRepository.update(entity);
    }

    void deleteById(@NonNull Long id) {
        customerRepository.deleteById(id);
    }

    void streamAll(@NonNull Consumer<Customer> customerConsumer) {
        try (Stream<CustomerEntity> customerEntityStream = customerRepository.streamAll()) {
            customerEntityStream.forEach(customerEntity -> customerConsumer.accept(customerEntity.toDto()));
        }
    }
}

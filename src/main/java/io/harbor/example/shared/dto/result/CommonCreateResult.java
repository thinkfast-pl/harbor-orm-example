package io.harbor.example.shared.dto.result;

import lombok.NonNull;
import lombok.Value;

@Value
public class CommonCreateResult<T> {

    @NonNull
    T id;
}

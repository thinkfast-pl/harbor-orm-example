package io.harbor.example.domain.document.dto;

import lombok.Value;

@Value
public class DocumentSignature {
    String signer;
    String signature;
}

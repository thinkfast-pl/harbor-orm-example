package io.harbor.example.domain.document.dto;

import lombok.Value;

import java.util.UUID;

@Value
public class Document {
    UUID id;
    DocumentBody body;
    DocumentSignature signature;
}

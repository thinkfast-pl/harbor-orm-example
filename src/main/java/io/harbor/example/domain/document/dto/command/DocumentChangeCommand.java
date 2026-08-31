package io.harbor.example.domain.document.dto.command;

import io.harbor.example.domain.document.dto.DocumentBody;
import io.harbor.example.domain.document.dto.DocumentSignature;
import lombok.Value;

@Value
public class DocumentChangeCommand {
    DocumentBody body;
    DocumentSignature signature;
}

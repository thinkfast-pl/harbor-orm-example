package io.harbor.example.domain.document;

import io.harbor.api.annotations.*;
import io.harbor.example.domain.document.dto.Document;
import io.harbor.example.domain.document.dto.DocumentBody;
import io.harbor.example.domain.document.dto.DocumentSignature;
import io.harbor.example.domain.document.dto.command.DocumentChangeCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(table = "documents")
@NoArgsConstructor
@AllArgsConstructor
@Getter
class DocumentEntity {

    @Id
    private UUID id;

    @Json
    @Column(nullable = false)
    private DocumentBody body;

    @Json
    @Column(nullable = false)
    @Convert(converter = DocumentSignatureConverter.class)
    private DocumentSignature signature;

    static DocumentEntity of(UUID id, DocumentChangeCommand command) {
        return new DocumentEntity(
                id,
                command.getBody(),
                command.getSignature()
        );
    }

    void update(DocumentChangeCommand command) {
        body = command.getBody();
        signature = command.getSignature();
    }

    Document toDto() {
        return new Document(
                id,
                body,
                signature
        );
    }
}

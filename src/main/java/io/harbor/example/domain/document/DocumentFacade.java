package io.harbor.example.domain.document;

import io.harbor.example.domain.document.dto.Document;
import io.harbor.example.domain.document.dto.command.DocumentChangeCommand;
import io.harbor.example.shared.dto.result.CommonCreateResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentFacade {
    private final DocumentRepository documentRepository;

    public CommonCreateResult<UUID> create(@NonNull DocumentChangeCommand command) {
        final UUID id = UUID.randomUUID();
        documentRepository.insert(DocumentEntity.of(id, command));
        return new CommonCreateResult<>(id);
    }

    public Optional<Document> findById(@NonNull UUID id) {
        return documentRepository.findById(id).map(DocumentEntity::toDto);
    }

    public void update(@NonNull UUID id, @NonNull DocumentChangeCommand command) {
        DocumentEntity documentEntity = documentRepository.findByIdForUpdateOrThrow(id);
        documentEntity.update(command);
        documentRepository.update(documentEntity);
    }

    public void delete(@NonNull UUID id) {
        documentRepository.deleteById(id);
    }
}

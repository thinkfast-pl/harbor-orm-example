package io.harbor.example.domain.document;

import io.harbor.api.HarborSession;
import io.harbor.core.repository.EntityRepository;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
class DocumentRepository extends EntityRepository<DocumentEntity, UUID> {
    private static final QDocumentEntity DOCUMENT = new QDocumentEntity("d");

    DocumentRepository(@NonNull HarborSession session) {
        super(session, DOCUMENT);
    }
}

package io.harbor.example.domain.document;

import io.harbor.api.repository.EntityNotFoundException;
import io.harbor.example.MySqlIntegrationTest;
import io.harbor.example.domain.document.dto.Document;
import io.harbor.example.domain.document.dto.DocumentBody;
import io.harbor.example.domain.document.dto.DocumentSignature;
import io.harbor.example.domain.document.dto.command.DocumentChangeCommand;
import io.harbor.example.shared.dto.result.CommonCreateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentFacadeTest extends MySqlIntegrationTest {

    @Autowired
    private DocumentFacade documentFacade;

    private static DocumentChangeCommand command(String title) {
        return new DocumentChangeCommand(
                new DocumentBody(true, LocalDate.of(2026, 8, 10), title, "content of " + title),
                new DocumentSignature("signer-of-" + title, "sig-" + title)
        );
    }

    @Test
    void createReturnsGeneratedId() {
        CommonCreateResult<UUID> result = documentFacade.create(command("contract"));

        assertThat(result.getId()).isNotNull();
    }

    @Test
    void createdDocumentCanBeFoundById() {
        DocumentBody body = new DocumentBody(true, LocalDate.of(2026, 1, 15), "nda", "confidential content");
        DocumentSignature signature = new DocumentSignature("Alice", "alice-sig");
        UUID id = documentFacade.create(new DocumentChangeCommand(body, signature)).getId();

        Optional<Document> found = documentFacade.findById(id);

        assertThat(found).isPresent();
        Document document = found.get();
        assertThat(document.getId()).isEqualTo(id);
        assertThat(document.getBody()).isEqualTo(body);
        assertThat(document.getSignature()).isEqualTo(signature);
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(documentFacade.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void updateReplacesBodyAndSignature() {
        UUID id = documentFacade.create(command("draft")).getId();
        DocumentBody newBody = new DocumentBody(false, LocalDate.of(2026, 2, 1), "final", "final content");
        DocumentSignature newSignature = new DocumentSignature("Bob", "bob-sig");

        documentFacade.update(id, new DocumentChangeCommand(newBody, newSignature));

        Optional<Document> found = documentFacade.findById(id);
        assertThat(found).isPresent();
        Document document = found.get();
        assertThat(document.getBody()).isEqualTo(newBody);
        assertThat(document.getSignature()).isEqualTo(newSignature);
    }

    @Test
    void updateThrowsForUnknownId() {
        DocumentChangeCommand command = command("ghost");

        assertThatThrownBy(() -> documentFacade.update(UUID.randomUUID(), command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteRemovesDocument() {
        UUID id = documentFacade.create(command("temporary")).getId();

        documentFacade.delete(id);

        assertThat(documentFacade.findById(id)).isEmpty();
    }

    @Test
    void deleteIsNoOpForUnknownId() {
        UUID existingId = documentFacade.create(command("survivor")).getId();

        assertThatCode(() -> documentFacade.delete(UUID.randomUUID())).doesNotThrowAnyException();

        assertThat(documentFacade.findById(existingId)).isPresent();
    }
}

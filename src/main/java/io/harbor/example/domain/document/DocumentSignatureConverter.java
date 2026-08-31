package io.harbor.example.domain.document;

import com.google.gson.Gson;
import io.harbor.api.converter.AttributeConverter;
import io.harbor.example.domain.document.dto.DocumentSignature;

public class DocumentSignatureConverter implements AttributeConverter<DocumentSignature, String> {

    private static final Gson GSON = new Gson();

    @Override
    public String convertToDatabaseColumn(DocumentSignature attribute) {
        if (attribute == null) return null;
        return GSON.toJson(attribute);
    }

    @Override
    public DocumentSignature convertToEntityAttribute(String attribute) {
        if (attribute == null) return null;
        return GSON.fromJson(attribute, DocumentSignature.class);
    }
}

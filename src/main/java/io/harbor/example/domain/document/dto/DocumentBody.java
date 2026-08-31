package io.harbor.example.domain.document.dto;

import lombok.Value;

import java.time.LocalDate;

@Value
public class DocumentBody {
    boolean accepted;
    LocalDate date;
    String title;
    String content;
}

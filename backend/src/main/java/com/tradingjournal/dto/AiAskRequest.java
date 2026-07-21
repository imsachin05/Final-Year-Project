package com.tradingjournal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiAskRequest {
    @NotBlank
    private String question;
}

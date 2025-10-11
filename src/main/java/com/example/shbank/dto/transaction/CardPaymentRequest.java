package com.example.shbank.dto.transaction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardPaymentRequest {

    @NotNull
    private Long accountId;

    @NotNull
    @Min(1)
    private Integer amount;

    @NotBlank
    private String merchantName;
}
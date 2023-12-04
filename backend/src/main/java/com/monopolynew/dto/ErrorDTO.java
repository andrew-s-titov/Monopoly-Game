package com.monopolynew.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {

    @Builder.Default
    private UUID id = UUID.randomUUID();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Integer code;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String message;
}

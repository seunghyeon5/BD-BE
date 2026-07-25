package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDuplicateCheckResponse {
    private String field;
    private String value;
    private boolean available;
}

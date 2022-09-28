package com.dok.ebankingbackend.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
}

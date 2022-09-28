package com.dok.ebankingbackend.dtos;

import lombok.Data;

@Data
public class TransferDTO {
    private String accountSource;
    private String accountDestination;
    private double amount ;
}

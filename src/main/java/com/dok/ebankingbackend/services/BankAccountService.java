package com.dok.ebankingbackend.services;

import com.dok.ebankingbackend.dtos.*;
import com.dok.ebankingbackend.entities.BankAccount;
import com.dok.ebankingbackend.entities.CurrentAccount;
import com.dok.ebankingbackend.entities.Customer;
import com.dok.ebankingbackend.entities.SavingAccount;
import com.dok.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.dok.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.dok.ebankingbackend.exceptions.CustomerNotFoundException;

import java.util.List;

public interface BankAccountService {
    CustomerDTO createCustomer(CustomerDTO customerDTO);

    CustomerDTO updateCustomer(CustomerDTO customerDTO);

    void deleteCustomer(Long customerId);

    CurrentBankAccountDTO createCurrentAccount(Long customerId, double initialBalance, double overDraft) throws CustomerNotFoundException;

    SavingBankAccountDTO createSavingAccount(Long customerId, double initialBalance, double interest) throws CustomerNotFoundException;


    List<CustomerDTO> getAllCustomer();

    CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException;

    BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException;

    void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException;

    void credit(String accountId, double amount, String description) throws BankAccountNotFoundException;

    void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException;

    List<BankAccountDTO> bankAccountList();

    List<AccountOperationDTO> operationHistories(String accountId);

    AccountHistoriesDTO accountHistories(String accountId, int page, int size) throws BankAccountNotFoundException;

    List<CustomerDTO> searchCustomers(String keyWord);
}

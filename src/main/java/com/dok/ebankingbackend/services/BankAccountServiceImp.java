package com.dok.ebankingbackend.services;

import com.dok.ebankingbackend.dtos.*;
import com.dok.ebankingbackend.entities.*;
import com.dok.ebankingbackend.enums.OperationType;
import com.dok.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.dok.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.dok.ebankingbackend.exceptions.CustomerNotFoundException;
import com.dok.ebankingbackend.mappers.BankAccountMapperImp;
import com.dok.ebankingbackend.repositories.AccountOperationRepository;
import com.dok.ebankingbackend.repositories.BankAccountRepository;
import com.dok.ebankingbackend.repositories.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImp implements BankAccountService{

    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private BankAccountMapperImp dtoMapper;

//    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Override
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        log.info("Creating new customer");
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        Customer createdCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(createdCustomer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("updating  customer");
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        Customer createdCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(createdCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId){
        customerRepository.deleteById(customerId);
    }

    @Override
    public CurrentBankAccountDTO createCurrentAccount(Long customerId, double initialBalance, double overDraft) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) throw new CustomerNotFoundException("Customer not found");

        CurrentAccount currentAccount = new CurrentAccount();

        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreatedAt(new Date());
        currentAccount.setBalance(initialBalance);
        currentAccount.setOverDraft(overDraft);
        currentAccount.setCustomer(customer);

        CurrentAccount createdCurrentAccount = bankAccountRepository.save(currentAccount);

        return dtoMapper.fromCurrentAccount(createdCurrentAccount);
    }

    @Override
    public SavingBankAccountDTO createSavingAccount(Long customerId, double initialBalance, double interest) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) throw new CustomerNotFoundException("Customer not found");

        SavingAccount savingAccount = new SavingAccount();

        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(new Date());
        savingAccount.setBalance(initialBalance);
        savingAccount.setInterestRate(interest);
        savingAccount.setCustomer(customer);

        SavingAccount createdsavingAccount = bankAccountRepository.save(savingAccount);

        return dtoMapper.fromSavingAccount(createdsavingAccount);
    }


    @Override
    public List<CustomerDTO> getAllCustomer() {
        List<Customer> customers = customerRepository.findAll();
        List<CustomerDTO> customerDTOS = customers.stream()
                .map(customer -> dtoMapper.fromCustomer(customer))
                .collect(Collectors.toList());

        return customerDTOS;
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
       Customer customer = customerRepository.findById(customerId)
               .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
       return dtoMapper.fromCustomer(customer);
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
         BankAccount bankAccount = bankAccountRepository.findById(accountId)
                         .orElseThrow(()-> new BankAccountNotFoundException("Bank account not found"));
        if (bankAccount instanceof SavingAccount){
            SavingAccount savingAccount = (SavingAccount) bankAccount;
            return dtoMapper.fromSavingAccount(savingAccount);
        }else {
            CurrentAccount currentAccount = (CurrentAccount) bankAccount;
            return dtoMapper.fromCurrentAccount(currentAccount);
        }
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
//        BankAccount bankAccount = getBankAccount(accountId);
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(()-> new BankAccountNotFoundException("Bank account not found"));
        if (bankAccount.getBalance() < amount)  throw new BalanceNotSufficientException("Balance not sufficient");

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setOperationDate(new Date());
        accountOperation.setDescription(description);
        accountOperation.setBankAccount(bankAccount);

        accountOperationRepository.save(accountOperation);

        bankAccount.setBalance(bankAccount.getBalance() - amount);
        bankAccountRepository.save(bankAccount);

    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
//        BankAccount bankAccount = getBankAccount(accountId);
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(()-> new BankAccountNotFoundException("Bank account not found"));

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setOperationDate(new Date());
        accountOperation.setDescription(description);
        accountOperation.setBankAccount(bankAccount);

        accountOperationRepository.save(accountOperation);

        bankAccount.setBalance(bankAccount.getBalance() + amount);
        bankAccountRepository.save(bankAccount);

    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIdSource, amount, "Transfer to" + accountIdDestination);
        credit(accountIdDestination, amount, "Transfer from " + accountIdSource);

    }

    @Override
    public List<BankAccountDTO> bankAccountList(){
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        List<BankAccountDTO> bankAccountDTOS = bankAccounts.stream().map( bankAccount ->{
                  if (bankAccount instanceof SavingAccount) {
                      SavingAccount savingAccount = (SavingAccount) bankAccount;
                      return dtoMapper.fromSavingAccount(savingAccount);
                  }else {
                      CurrentAccount currentAccount = (CurrentAccount) bankAccount;
                      return dtoMapper.fromCurrentAccount(currentAccount);
                  }
                }).collect(Collectors.toList());

        return bankAccountDTOS;
    }

    @Override
    public List<AccountOperationDTO> operationHistories(String accountId){
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);
        return accountOperations.stream().map(operationHistory ->dtoMapper.fromAccountOperation(operationHistory))
                .collect(Collectors.toList());
    }

    @Override
    public AccountHistoriesDTO accountHistories(String accountId, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElse(null);
        if (bankAccount == null) throw new  BankAccountNotFoundException("Account not found");
       Page<AccountOperation> accountOperations= accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, PageRequest.of(page, size));
        AccountHistoriesDTO accountHistoriesDTO = new AccountHistoriesDTO();
        List<AccountOperationDTO> accountOperationDTOS = accountOperations.getContent().stream().map(accountOperation -> dtoMapper.fromAccountOperation(accountOperation))
                       .collect(Collectors.toList());

       accountHistoriesDTO.setAccountOperationDTO(accountOperationDTOS);
       accountHistoriesDTO.setAccountId(bankAccount.getId());
       accountHistoriesDTO.setBalance(bankAccount.getBalance());
       accountHistoriesDTO.setCurrentPage(page);
       accountHistoriesDTO.setTotalPages(accountOperations.getTotalPages());
       return accountHistoriesDTO;
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyWord) {
        List<Customer> customers = customerRepository.searchCustomers(keyWord);
        List<CustomerDTO> customerDTOS = customers.stream().map(customer -> dtoMapper.fromCustomer(customer))
                .collect(Collectors.toList());
        return customerDTOS;
    }
}

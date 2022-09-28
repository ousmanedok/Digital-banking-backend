package com.dok.ebankingbackend;

import com.dok.ebankingbackend.dtos.BankAccountDTO;
import com.dok.ebankingbackend.dtos.CurrentBankAccountDTO;
import com.dok.ebankingbackend.dtos.CustomerDTO;
import com.dok.ebankingbackend.dtos.SavingBankAccountDTO;
import com.dok.ebankingbackend.entities.*;
import com.dok.ebankingbackend.enums.AccountStatus;
import com.dok.ebankingbackend.enums.OperationType;
import com.dok.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.dok.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.dok.ebankingbackend.exceptions.CustomerNotFoundException;
import com.dok.ebankingbackend.repositories.AccountOperationRepository;
import com.dok.ebankingbackend.repositories.BankAccountRepository;
import com.dok.ebankingbackend.repositories.CustomerRepository;
import com.dok.ebankingbackend.services.BankAccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class EbankingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbankingBackendApplication.class, args);
	}

	@Bean
	CommandLineRunner start(BankAccountService bankAccountService){
		return args -> {
			Stream.of("Ousmane", "Mamady", "Siaka").forEach(name ->{
				CustomerDTO customer = new CustomerDTO();
				customer.setName(name);
				customer.setEmail(name+"@gmail.com");

				bankAccountService.createCustomer(customer);

			});

			bankAccountService.getAllCustomer().forEach(customer -> {
				try {
					bankAccountService.createCurrentAccount(customer.getId(), Math.random()*10000, 50000);
					bankAccountService.createSavingAccount(customer.getId(), Math.random()*5000, 5.5);


					} catch ( CustomerNotFoundException  e) {
									e.printStackTrace();
					}
			});

			List<BankAccountDTO> bankAccountList = bankAccountService.bankAccountList();
			for (BankAccountDTO bankAccount:bankAccountList){
				for (int i=0; i<10; i++){
					String accountId;
					if (bankAccount instanceof SavingBankAccountDTO){
						accountId = ((SavingBankAccountDTO) bankAccount).getId();
					} else accountId = ((CurrentBankAccountDTO) bankAccount).getId();

					bankAccountService.credit(accountId, 10000+Math.random()*10000, "Credit");

					bankAccountService.debit(accountId, 5000+Math.random()*100, "Debit");

				}
			}

		};
	}




	//@Transactional
	//@Bean
	CommandLineRunner start(BankAccountRepository bankAccountRepository){
		return args -> {
			BankAccount bankAccount = bankAccountRepository.findById("0c47eb9c-4a51-4ed5-9395-d8a26a4a2c47").orElse(null);
			if (bankAccount != null){
				System.out.println("**************************************************************");
				System.out.println(bankAccount.getId());
				System.out.println(bankAccount.getBalance());
				System.out.println(bankAccount.getStatus());
				System.out.println(bankAccount.getCreatedAt());
				System.out.println(bankAccount.getCustomer().getName());
				if (bankAccount instanceof CurrentAccount){
					System.out.println("Over draft " + ((CurrentAccount)bankAccount).getOverDraft());

				} else if (bankAccount instanceof SavingAccount) {
					System.out.println("Interest " +((SavingAccount)bankAccount).getInterestRate());
				}
				bankAccount.getAccountOperations().forEach(operation ->{
					System.out.println("---------------------------------------------------");
					System.out.println(operation.getType() + "\t" +operation.getOperationDate() + "\t" + operation.getAmount());



				});
			}
		};
	}

	//@Bean
	CommandLineRunner start(CustomerRepository customerRepository,
							BankAccountRepository bankAccountRepository,
							AccountOperationRepository accountOperationRepository) {
		return args -> {
			Stream.of("Ousmane", "Iya", "Siaka").forEach(name -> {
				Customer customer = new Customer();
				customer.setName(name);
				customer.setEmail(name + "@gmail.com");
				customerRepository.save(customer);
			});
			customerRepository.findAll().forEach(customer -> {
				CurrentAccount currentAccount = new CurrentAccount();
				currentAccount.setId(UUID.randomUUID().toString());
				currentAccount.setBalance(Math.random() * 90000);
				currentAccount.setCreatedAt(new Date());
				currentAccount.setStatus(AccountStatus.CREATED);
				currentAccount.setCustomer(customer);
				currentAccount.setOverDraft(100000);
				bankAccountRepository.save(currentAccount);

				SavingAccount savingAccount = new SavingAccount();
				savingAccount.setId(UUID.randomUUID().toString());
				savingAccount.setBalance(Math.random() * 90000);
				savingAccount.setCreatedAt(new Date());
				savingAccount.setStatus(AccountStatus.CREATED);
				savingAccount.setCustomer(customer);
				savingAccount.setInterestRate(5.5);
				bankAccountRepository.save(savingAccount);

			});
			bankAccountRepository.findAll().forEach(acc -> {
				for (int i = 0; i < 10; i++) {
					AccountOperation accountOperation = new AccountOperation();
					accountOperation.setOperationDate(new Date());
					accountOperation.setAmount(Math.random() * 12000);
					accountOperation.setType(Math.random() > 0.5 ? OperationType.DEBIT : OperationType.CREDIT);
					accountOperation.setBankAccount(acc);
					accountOperationRepository.save(accountOperation);
				}

			});
		};

	}

}

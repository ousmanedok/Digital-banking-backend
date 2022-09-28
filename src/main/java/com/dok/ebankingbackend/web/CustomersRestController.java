package com.dok.ebankingbackend.web;

import com.dok.ebankingbackend.dtos.CustomerDTO;
import com.dok.ebankingbackend.exceptions.CustomerNotFoundException;
import com.dok.ebankingbackend.services.BankAccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@CrossOrigin("*")
public class CustomersRestController {

    private BankAccountService bankAccountService;

    @GetMapping("/customers")
    public List<CustomerDTO> customers(){
        return bankAccountService.getAllCustomer();
    }

    @GetMapping("/customers/search")
    public List<CustomerDTO> searchCustomers(@RequestParam(name="keyWord", defaultValue="") String keyWord){
        return bankAccountService.searchCustomers("%"+keyWord+"%");
    }

    @GetMapping("/customers/{id}")
    public CustomerDTO customer(@PathVariable(name = "id") Long customerId) throws CustomerNotFoundException {
        return bankAccountService.getCustomer(customerId);
    }

    @PostMapping("/customers")
    public CustomerDTO createCustomer(@RequestBody CustomerDTO customerDTO){
        return bankAccountService.createCustomer( customerDTO);
    }

    @PutMapping("/customers/{customerId}")
    public CustomerDTO updateCustomer(@PathVariable Long customerId, @RequestBody CustomerDTO customerDTO ){
        customerDTO.setId(customerId);
        return bankAccountService.updateCustomer(customerDTO);
    }

    @DeleteMapping("/customers/{id}")
    public  void deleteCustomer(@PathVariable Long id){
        bankAccountService.deleteCustomer(id);
    }

}

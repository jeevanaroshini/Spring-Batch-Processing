package com.roshini.spring.batchprocessing.importcsv.config;

import com.roshini.spring.batchprocessing.importcsv.entity.Customer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class CustomerProcessor implements ItemProcessor<Customer, Customer> {
    @Override
    public Customer process(Customer customer) throws Exception {
//        if (customer.getCountry().equals("United States")) {
//            return customer;
//        }
        return customer;
    }
}

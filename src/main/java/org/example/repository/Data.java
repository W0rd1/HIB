package org.example.repository;

import org.example.model.Customer;
import org.example.model.PaymentRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Data {
    public final Map<String, Customer> customers = new HashMap<>();
    public final Map<String, List<PaymentRequest>> inbox = new HashMap<>();
}

package org.example.payment;

import org.example.model.PaymentResult;

public class MobilePayment extends PaymentMethod {
    private final String phoneNumber;

    public MobilePayment(String phoneNumber) {
        super("Mobile");
        this.phoneNumber = phoneNumber;
    }

    @Override
    public PaymentResult processPayment(double amount) {

        if (phoneNumber == null || !phoneNumber.matches("^\\+371\\d{8}$")){
            return new PaymentResult(false, "Invalid Latvian phone number");
        }
        return new PaymentResult(true, "Paid " + amount + " using Mobile Payment");
    }
}

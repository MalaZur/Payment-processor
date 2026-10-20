package org.example.menu;

import org.example.config.AppConfig;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.model.PaymentResult;
import org.example.payment.PaymentMethod;
import org.example.payment.PaymentMethodFactory;
import org.example.payment.PaymentProcessor;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class ConsoleMenu {
    private final Scanner scanner = new Scanner(System.in);
    private final PaymentProcessor paymentProcessor = new PaymentProcessor();

    private Order currentOrder;
    private final List<Order> completedOrders = new ArrayList<>();

    public void start(){
        AppConfig config = AppConfig.getInstance();
        System.out.println("Welcome to " + config.getApplicationName());

        boolean running = true;
        while(running){
            printMenu();

            int option = Integer.parseInt(scanner.nextLine());

            switch (option){
                case 1 -> createOrder();
                case 2 -> addItem();
                case 3 -> viewOrder();
                case 4 -> payOrder();
                case 5 -> viewCompletedOrders();
                case 0 -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    private void createOrder(){
        System.out.println("Customer name:");
        String customerName = scanner.nextLine();

        currentOrder = Order.builder()
                .customerName(customerName)
                .build();
        System.out.println("Order created for " + customerName);
    }

    private void addItem(){
        if (currentOrder == null){
            System.out.println("Please create an order first");
            return;
        }
        if (currentOrder.isPaid()){
            System.out.println("Please create a new order");
            return;
        }

        System.out.println("Item name:");
        String itemName = scanner.nextLine();

        System.out.println("Price:");
        double price = Double.parseDouble(scanner.nextLine());

        System.out.println("Quantity:");
        int quantity = Integer.parseInt(scanner.nextLine());

        currentOrder.addItem(new OrderItem(itemName, price, quantity));
        System.out.println("Item added to order");
    }

    private void viewOrder(){
        if (currentOrder == null){
            System.out.println("Please create an order first");
            return;
        }

        if (currentOrder.getItems().size() == 0){
            System.out.println("Please add at least one item to the order");
            return;
        }

        System.out.println("Customer: " + currentOrder.getCustomerName());
        System.out.println("Status: " +  currentOrder.getStatus());
        System.out.println("Items:");

        for (OrderItem item : currentOrder.getItems()){
            System.out.println("- " + item);
        }

        System.out.println("Total before tax: " +
                currentOrder.calculateTotalWithoutTax() +
                "\nTax: " +
                        (currentOrder.calculateTotalWithoutTax() * AppConfig.getInstance().getTaxRate()) +
                "\nTotal: " + currentOrder.calculateTotal());
    }

    private void payOrder(){
        if (currentOrder == null){
            System.out.println("Please create an order first");
            return;
        }

        System.out.println("Choose your currency (example - EUR, USD, RUB): ");
        String currency = scanner.nextLine();

        if (!currency.trim().toUpperCase().equals(AppConfig.getInstance().getCurrency())){
            System.out.println("Sorry, we only accept EUR");
            return;
        }

        System.out.println("""
                Select payment method:
                1. Credit Card
                2. PayPal
                3. Gift Card
                4. Mobile Payment
                """);
        int option = Integer.parseInt(scanner.nextLine());

        PaymentMethod paymentMethod = switch(option){
            case 1 -> createCreditCardPayment();
            case 2 -> createPaypalPayment();
            case 3 -> createGiftCardPayment();
            case 4 -> createMobilePayment();
            default -> throw new IllegalArgumentException("Invalid payment method");
        };

        PaymentResult result = paymentProcessor.process(currentOrder, paymentMethod);
        System.out.println(result.getMessage());

        if (result.isSuccessful()){
            completedOrders.add(currentOrder);
        }
    }

    private PaymentMethod createCreditCardPayment(){
        System.out.println("Card number:");
        String cardNumber =  scanner.nextLine();

        System.out.println("Card holder name:");
        String cardHolderName =  scanner.nextLine();

        return PaymentMethodFactory.createCreditCardPayment(cardNumber,cardHolderName);
    }


    private PaymentMethod createPaypalPayment(){
        System.out.println("PayPal email: ");
        String email = scanner.nextLine();

        return PaymentMethodFactory.createPaypalPayment(email);
    }

    private PaymentMethod createGiftCardPayment(){
        System.out.println("Gift Card code: ");
        String code = scanner.nextLine();

        System.out.println("Available balance: ");
        double balance = Double.parseDouble(scanner.nextLine());

        return PaymentMethodFactory.createGiftCardPayment(code, balance);
    }

    private PaymentMethod createMobilePayment(){
        System.out.println("Latvian phone number:");
        String phoneNumber = scanner.nextLine();

        return PaymentMethodFactory.createMobilePayment(phoneNumber);
    }

    private void viewCompletedOrders(){
        if (completedOrders.isEmpty()){
            System.out.println("No completed orders yet");
            return;
        }

        System.out.println("Completed orders: " + completedOrders.size());

        for (int i = 0; i < completedOrders.size(); i++){
            Order order = completedOrders.get(i);

            System.out.println("\nOrder #" + (i + 1));
            System.out.println("Customer: " + order.getCustomerName());
            System.out.println("Status: " + order.getStatus());

            for (OrderItem item : order.getItems()){
                System.out.println("- " + item);
            }

            System.out.println("Tax: " +
                    (currentOrder.calculateTotalWithoutTax() * AppConfig.getInstance().getTaxRate()));
            System.out.println("Total: " + order.calculateTotal() + "\n");
        }
    }

    private void printMenu(){
        System.out.println("""
                1. Create order
                2. Add item to order
                3. View order
                4. Pay order
                5. View completed orders
                0. Exit
                """);
    }
}

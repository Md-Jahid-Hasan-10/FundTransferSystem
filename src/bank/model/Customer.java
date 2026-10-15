package bank.model;

public class Customer {
    private final String customerId;
    private final String name;
    private final String email;
    private final String phone;
    private final String pin;

    public Customer(String customerId, String name, String email, String phone, String pin) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.pin = pin;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    public boolean verifyPin(String input) {
        return pin.equals(input);
    }
}
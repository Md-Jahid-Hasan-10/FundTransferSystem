package bank;

import bank.config.AppConfig;
import bank.core.Bank;
import bank.model.Account;
import bank.model.Customer;
import bank.notification.NotificationChannel;
import bank.notification.NotificationFactory;
import bank.otp.FixedOtpGenerator;
import bank.otp.OtpGenerator;
import bank.otp.RandomOtpGenerator;
import bank.policy.FlatTransferFeePolicy;
import bank.policy.StandardTransferFeePolicy;
import bank.policy.TransferFeePolicy;
import bank.service.FundTransferService;
import bank.service.NotificationService;
import bank.service.OtpService;
import bank.ui.ConsoleUI;

import java.math.BigDecimal;
import java.time.Clock;

public class Main {
    public static void main(String[] args) {
        // 1. Shared settings
        AppConfig config = AppConfig.getInstance();
        Clock clock = Clock.systemDefaultZone();

        // 2. Create the concrete objects
        Bank bank = new Bank();
        TransferFeePolicy feePolicy = new StandardTransferFeePolicy();
        TransferFeePolicy ownAccountFeePolicy = new FlatTransferFeePolicy(new BigDecimal("0.00")); // free
        OtpGenerator otpGenerator = config.isFixedOtp()
                ? new FixedOtpGenerator(config.getFixedOtpValue())
                : new RandomOtpGenerator();
        OtpService otpService = new OtpService(otpGenerator, clock,
                config.getOtpExpirySeconds(), config.getMaxOtpAttempts());
        NotificationChannel channel = NotificationFactory.create(config.getNotificationChannel());
        NotificationService notificationService = new NotificationService(channel, config);

        // 3. Connect them (constructor injection)
        FundTransferService service = new FundTransferService(bank, feePolicy, ownAccountFeePolicy,
                otpService, notificationService, clock, config.getOtpExpirySeconds(), config.getRequestExpirySeconds());

        // 4. Sample data so you can test immediately
        seedData(bank, service);

        // 5. Start the CLI
        new ConsoleUI(bank, service, config).start();
    }

    private static void seedData(Bank bank, FundTransferService service) {
        Customer alice = bank.registerCustomer("Alice", "alice@example.com", "01700000001", "1234");
        Customer bob = bank.registerCustomer("Bob", "bob@example.com", "01700000002", "5678");
        Account aliceAccount = bank.openAccount(alice);
        Account aliceSecondAccount = bank.openAccount(alice);
        Account bobAccount = bank.openAccount(bob);
        service.deposit(alice, aliceAccount.getAccountNumber(), new BigDecimal("5000.00"));
        service.deposit(bob, bobAccount.getAccountNumber(), new BigDecimal("1000.00"));

        System.out.println("Sample customers (for testing):");
        System.out.println("  " + alice.getCustomerId() + " / PIN 1234 / accounts " + aliceAccount.getAccountNumber()
                + ", " + aliceSecondAccount.getAccountNumber());
        System.out.println("  " + bob.getCustomerId() + " / PIN 5678 / account " + bobAccount.getAccountNumber());
        System.out.println();
    }
}
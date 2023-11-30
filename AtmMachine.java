// AtmMachine.java

package ATM_MACHINE_DATABASE;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import gov.in.oupp.training.java.corejava.jdbc.AccountDAO;

public class AtmMachine {

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ouppdb?useSSL=false", "root", "12345678");
            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.println("\t\tWelcome to XYZ bank.");
                System.out.print("Enter UserID: ");
                String userID = sc.next();
                System.out.print("Enter Password: ");
                String password = sc.next();

                AccountDAO accountDAO = new AccountDAO(con);

                // Check user authentication
                if (accountDAO.authenticateUser(userID, password)) {
                    System.out.println("User authenticated successfully.");

                    // Get the account associated with the user
                    int accountNumber = accountDAO.getAccountNumber(userID);

                    while (true) {
                        System.out.println("\nATM Menu:");
                        System.out.println("1. Display Account Information");
                        System.out.println("2. Deposit");
                        System.out.println("3. Withdraw");
                        System.out.println("4. Display Transaction History");
                        System.out.println("5. Check Balance");
                        System.out.println("6. Transfer Money");
                        System.out.println("7. Logout");
                        System.out.print("Select an option: ");
                        int option = sc.nextInt();

                        try {
                            switch (option) {
                                case 1:
                                    // Display Account Information
                                    accountDAO.displayAccountInfo(accountNumber);
                                    break;
                                case 2:
                                    // Deposit
                                    System.out.print("Enter Deposit Amount: $");
                                    double depositAmount = sc.nextDouble();
                                    accountDAO.deposit(accountNumber, depositAmount);
                                    System.out.println("Deposit successful.");
                                    break;
                                case 3:
                                    // Withdraw
                                    System.out.print("Enter Withdrawal Amount: $");
                                    double withdrawAmount = sc.nextDouble();
                                    if (accountDAO.withdraw(accountNumber, withdrawAmount)) {
                                        System.out.println("Withdrawal successful.");
                                    } else {
                                        System.out.println("Insufficient funds!");
                                    }
                                    break;
                                case 4:
                                    // Display Transaction History
                                    accountDAO.displayTransactionHistory(accountNumber);
                                    break;
                                case 5:
                                    // Check Balance
                                    accountDAO.checkBalance(accountNumber);
                                    break;
                                case 6:
                                    // Transfer Money
                                    System.out.print("Enter Receiver's Account Number: ");
                                    int receiverAccountNumber = sc.nextInt();
                                    System.out.print("Enter Transfer Amount: $");
                                    double transferAmount = sc.nextDouble();
                                    if (accountDAO.transferMoney(accountNumber, receiverAccountNumber, transferAmount)) {
                                        System.out.println("Money transfer successful.");
                                    } else {
                                        System.out.println("Money transfer failed. Check the account numbers and available balance.");
                                    }
                                    break;
                                case 7:
                                    System.out.println("Logging out. Thank you!");
                                    break;
                                default:
                                    System.out.println("Invalid option. Please try again.");
                            }
                        } catch (Exception e) {
                            System.out.println("An error occurred: " + e.getMessage());
                        }

                        if (option == 7) {
                            break; // Break out of the inner loop (logout)
                        }
                    }
                } else {
                    System.out.println("User authentication failed. Please try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package gov.in.oupp.training.java.corejava.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AccountDAO {
    private Connection connection;

    public AccountDAO(Connection connection) {
        this.connection = connection;
        createTables(); // Ensure that tables exist
    }

    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            // Create users table if not exists
            statement.execute("CREATE TABLE IF NOT EXISTS users (userID VARCHAR(50) PRIMARY KEY, password VARCHAR(50) NOT NULL)");

            // Create accounts table if not exists
            statement.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                    "accountNumber INT PRIMARY KEY AUTO_INCREMENT, " +
                    "userID VARCHAR(50) NOT NULL, " +
                    "balance DOUBLE NOT NULL, " +
                    "type VARCHAR(50) NOT NULL, " +
                    "ownerName VARCHAR(50) NOT NULL, " +
                    "address VARCHAR(255), " +
                    "phoneNumber VARCHAR(15), " +
                    "cardNumber VARCHAR(16), " +
                    "expirationDate VARCHAR(10), " +
                    "pin VARCHAR(4))"
            );
            // Create transactions table if not exists
            statement.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "transactionID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "accountNumber INT, " +
                    "transactionType VARCHAR(20) NOT NULL, " +
                    "amount DOUBLE NOT NULL, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (accountNumber) REFERENCES accounts(accountNumber))"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean transferMoney(int senderAccountNumber, int receiverAccountNumber, double amount) throws SQLException {
        String updateSenderQuery = "UPDATE accounts SET balance = balance - ? WHERE accountNumber = ?";
        String updateReceiverQuery = "UPDATE accounts SET balance = balance + ? WHERE accountNumber = ?";
        String insertTransactionQuery = "INSERT INTO transactions (accountNumber, transactionType, amount) VALUES (?, 'TRANSFER', ?)";

        try (PreparedStatement updateSenderStatement = connection.prepareStatement(updateSenderQuery);
             PreparedStatement updateReceiverStatement = connection.prepareStatement(updateReceiverQuery);
             PreparedStatement insertTransactionStatement = connection.prepareStatement(insertTransactionQuery)) {

            connection.setAutoCommit(false);

            double senderBalance = getCurrentBalance(senderAccountNumber);

            if (amount <= senderBalance) {
                // Update sender's balance
                updateSenderStatement.setDouble(1, amount);
                updateSenderStatement.setInt(2, senderAccountNumber);
                int senderUpdatedRows = updateSenderStatement.executeUpdate();

                // Update receiver's balance
                updateReceiverStatement.setDouble(1, amount);
                updateReceiverStatement.setInt(2, receiverAccountNumber);
                int receiverUpdatedRows = updateReceiverStatement.executeUpdate();

                if (senderUpdatedRows > 0 && receiverUpdatedRows > 0) {
                    // Insert transaction record
                    insertTransactionStatement.setInt(1, senderAccountNumber);
                    insertTransactionStatement.setDouble(2, amount);
                    insertTransactionStatement.executeUpdate();

                    connection.commit();
                    return true;
                } else {
                    throw new SQLException("Money transfer failed. Check account numbers and available balance.");
                }
            } else {
                throw new SQLException("Money transfer failed. Insufficient funds.");
            }
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean authenticateUser(String userID, String password) {
        String query = "SELECT * FROM users WHERE userID = ? AND password = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userID);
            preparedStatement.setString(2, password);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next(); // true if user is authenticated
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void displayAccountInfo(int accountNumber) {
        String query = "SELECT * FROM accounts WHERE accountNumber = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, accountNumber);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("Account Information:");
                    System.out.println("Account Number: " + resultSet.getInt("accountNumber"));
                    System.out.println("Balance: $" + resultSet.getDouble("balance"));
                    System.out.println("Account Type: " + resultSet.getString("type"));
                    System.out.println("Owner Name: " + resultSet.getString("ownerName"));
                    
                } else {
                    System.out.println("Account not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void deposit(int accountNumber, double amount) throws SQLException {
        String updateQuery = "UPDATE accounts SET balance = balance + ? WHERE accountNumber = ?";
        String insertTransactionQuery = "INSERT INTO transactions (accountNumber, transactionType, amount) VALUES (?, 'DEPOSIT', ?)";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
             PreparedStatement insertTransactionStatement = connection.prepareStatement(insertTransactionQuery)) {
            connection.setAutoCommit(false);

            updateStatement.setDouble(1, amount);
            updateStatement.setInt(2, accountNumber);
            int updatedRows = updateStatement.executeUpdate();

            if (updatedRows > 0) {
                insertTransactionStatement.setInt(1, accountNumber);
                insertTransactionStatement.setDouble(2, amount);
                insertTransactionStatement.executeUpdate();

                connection.commit();
            } else {
                throw new SQLException("Deposit failed. Account not found.");
            }
        } catch (SQLException e) {
            connection.rollback();
            throw e;  
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean withdraw(int accountNumber, double amount) throws SQLException {
        String updateQuery = "UPDATE accounts SET balance = balance - ? WHERE accountNumber = ?";
        String insertTransactionQuery = "INSERT INTO transactions (accountNumber, transactionType, amount) VALUES (?, 'WITHDRAWAL', ?)";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
             PreparedStatement insertTransactionStatement = connection.prepareStatement(insertTransactionQuery)) {
            connection.setAutoCommit(false);

            double currentBalance = getCurrentBalance(accountNumber);

            if (amount <= currentBalance) {
                updateStatement.setDouble(1, amount);
                updateStatement.setInt(2, accountNumber);
                int updatedRows = updateStatement.executeUpdate();

                if (updatedRows > 0) {
                    insertTransactionStatement.setInt(1, accountNumber);
                    insertTransactionStatement.setDouble(2, amount);
                    insertTransactionStatement.executeUpdate();

                    connection.commit();
                    return true;
                } else {
                    throw new SQLException("Withdrawal failed. Account not found.");
                }
            } else {
                throw new SQLException("Withdrawal failed. Insufficient funds.");
            }
        } catch (SQLException e) {
            connection.rollback();
            throw e; // Re-throw the exception after rollback
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void displayTransactionHistory(int accountNumber) {
        String query = "SELECT * FROM transactions WHERE accountNumber = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, accountNumber);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    System.out.println("Transaction Type: " + resultSet.getString("transactionType"));
                    System.out.println("Amount: $" + resultSet.getDouble("amount"));
                    System.out.println("Date: " + resultSet.getTimestamp("timestamp"));
                    System.out.println("--------------");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public int getAccountNumber(String userID) {
        String query = "SELECT accountNumber FROM accounts WHERE userID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("accountNumber");
                } else {
                    throw new SQLException("Account not found for the given user ID.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // Return a special value or handle the error appropriately
        }
    }
    public void checkBalance(int accountNumber) throws SQLException {
        double balance = getCurrentBalance(accountNumber);
        System.out.println("Current Balance: $" + balance);
    }
    private double getCurrentBalance(int accountNumber) throws SQLException {
        String query = "SELECT balance FROM accounts WHERE accountNumber = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, accountNumber);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("balance");
                } else {
                    throw new SQLException("Account not found.");
                }
            }
        }
    }
}

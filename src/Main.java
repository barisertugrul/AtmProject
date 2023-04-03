import java.util.*;

public class Main {
    private static boolean userAuthenticated; // whether user is authenticated
    private static int currentAccountNumber; //current user's current account number
    private static final int BALANCE_INQUIRY = 1;
    private static final int WITHDRAWAL = 2;
    private static final int DEPOSIT = 3;
    private static final int EXIT = 4;
    private final static int CANCELED = 6;
    private static int count = 500;
    private static Scanner input;

    private static Map<Integer,Object> accounts = new HashMap<Integer,Object>();

    public static void main(String[] args) {
        run();
    }

    private static void run(){
        userAuthenticated = false;
        currentAccountNumber = 0;
        input = new Scanner(System.in);

        accounts.put(1,new ArrayList<>(Arrays.asList(12345, 54321, 1000.0, 1200.0)));
        accounts.put(2,new ArrayList<>(Arrays.asList(98765, 56789, 200.0, 200.0)));


        while(true)
        {
            // loop while user is not yet authenticated
            while(!userAuthenticated)
            {
                displayMessageLine("\nWelcome!");
                authenticateUser(); // authenticate user
            }// end while

            performTransactions(); // user is now authenticated
            userAuthenticated = false; // reset before next ATM session
            currentAccountNumber = 0; // reset before next ATM session
            displayMessageLine("\nThank you! Goodbye!");
        }// end while
    }

    private static void authenticateUser()
    {
        displayMessage("\nPlease enter your account number: ");
        int accountNumber = getInput(); // input account number
        displayMessage("\nEnter your PIN: "); // prompt for PIN
        int pin = getInput(); // input PIN

        // set userAuthenticated to boolean value returned by database
        userAuthenticated = authenticateUser(accountNumber, pin);

        // check whether authentication succeeded
        if(userAuthenticated)
        {
            currentAccountNumber = accountNumber; // save user's account number
        }// end if
        else
        {
            displayMessageLine("Invalid account number or PIN. Please try again.");
        }// end else
    }// end method authenticateUser

    private static void performTransactions()
    {

        // user has not chosen to exit
        boolean userExited = false;

        // loop while user has not chosen option to exit system
        while(!userExited)
        {
            // show main menu and get user selection
            int mainMenuSelection = displayMainMenu();

            // decide how to proceed based on user's menu selection
            switch(mainMenuSelection)
            {
                // user chosen to perform one of the three transactions types
                case BALANCE_INQUIRY:
                case WITHDRAWAL:
                case DEPOSIT:
                    createTransaction(mainMenuSelection);
                    break;

                case EXIT: // user chose to terminate session
                    displayMessageLine("\nExiting the system...");
                    userExited = true; // this ATM session should end
                    break;

                default: // user did not enter an integer from 1-4
                    displayMessageLine("\nYou did not enter a valid selection. Try again.");
                    break;
            }// end switch
        }// end while
    }// end method performTransactions

    private static void createTransaction(int type)
    {

        // determine which type of Transaction to create
        switch (type)
        {
            case BALANCE_INQUIRY: // create new BalanceInquiry transaction
                balanceInquiryExecute(currentAccountNumber);
                break;
            case WITHDRAWAL: // create new Withdrawal transaction
                withDrawalExecute(currentAccountNumber);
                break;
            case DEPOSIT: // create new Deposit transaction
                depositExecute(currentAccountNumber);
                break;
        }// end switch

    }// end method createTransaction

    private static void balanceInquiryExecute(int currentAccountNumber)
    {
        // get available balance for the account involved
        double availableBalance = getAvailableBalance(currentAccountNumber);

        // get total balance for the account involved
        double totalBalance = getTotalBalance(currentAccountNumber);

        // display the balance information on the screen
        displayMessageLine("\nBalanceInformation:");
        displayMessage(" - Available Balance: ");
        displayDollarAmount(availableBalance);
        displayMessageLine("\nTotal Balance: ");
        displayDollarAmount(totalBalance);
        displayMessageLine("");
    }// end method execute

    private static void withDrawalExecute(int currentAccountNumber)
    {
        boolean cashDispensed = false; // cash was not dispensed yet
        double availableBalance; // amount available for withdrawal


        // loop until cash is dispensed or the user cancels
        do
        {
            // obtain a chosen withdrawal amount from the user
            int amount = displayMenuOfAmounts();

            // check whether user chose a withdrawal amount or canceled
            if(amount != CANCELED)
            {
                // get available balance of account involved
                availableBalance = getAvailableBalance(currentAccountNumber);
            }
            else // user chose cancel menu option
            {
                displayMessageLine("\nCanceling transaction...");
                return; // return to main menu because user canceled
            }// end else
            // check whether the user has enough money in the account
            if(amount <= availableBalance)
            {
                // check if the cash dispenser has enough money
                if(isSufficientCashAvailable(amount))
                {
                    // update the account involved to reflect the withdrawal
                    debit(currentAccountNumber, amount);

                    dispenseCash(amount); // dispense cash
                    cashDispensed = true; // cash was dispensed

                    // instruct user to take cash
                    displayMessageLine("\n Your cash has been dispensed. Please take your cash now.");
                }// end if
                else // cash dispenser does not have enough cash
                {
                    displayMessageLine("\nInsufficient cash available in the ATM." +
                            "\n Please choose a smaller amount.");
                }// end else
            }// end if
            else // not enough money available in user's account
            {
                displayMessageLine("\nInsufficient funds in your account." +
                        "\n Please choose a smaller amount.");
            }// end else
        }// end if
        while(!cashDispensed);
    }// end method execute

    // display menu of withdrawal amounts and the option to cancel;
    // return the chosen amount or 0 if the user chooses to cancel
    private static int displayMenuOfAmounts()
    {
        int userChoice = 0; // local variable to store return value

        // array of amounts to correspond to menu numbers
        int[] amounts = {0, 20, 40, 60, 100, 200};

        // loop while no valid choice has been made
        while(userChoice == 0)
        {
            displayMessageLine("\nWithdrawal Menu:");
            displayMessageLine("1 - $20");
            displayMessageLine("2 - $40");
            displayMessageLine("3 - $60");
            displayMessageLine("4 - $100");
            displayMessageLine("5 - $200");
            displayMessageLine("6 - Cancel transaction");
            displayMessage("\nChoose a withdrawal amount: ");

            int input = getInput(); // get user iput through keypad

            //determine how to proceed based on the input value
            switch(input)
            {
                case 1: // if the user chose a withdrawal amount
                case 2: // (i.e., chose option 1, 2, 3, 4 or 5) return the
                case 3: // corresponding amount from array
                case 4:
                case 5:
                    userChoice = amounts[input]; // save user's choice
                    break;
                case CANCELED: // the user chose to cancel

                    userChoice = CANCELED;
                    break;
                default: // the user did not enter a value from 1-6
                    displayMessageLine("\nInvalid selection. Try again.");
            }// end switch
            displayMessageLine(String.valueOf(userChoice));
        }// end while

        return userChoice; // return withdrawal amount or canceled
    }// end method displayMenuOfAmounts

    public static void dispenseCash(int amount)
    {
        int billsRequired = amount / 20; // number of $20 bills required

        count -= billsRequired; // update count attribute (bills)
    }// end method dispenseCash

    // indicates whether cash dispenser can dispense desired amount
    public static boolean isSufficientCashAvailable(int amount)
    {
        int billsRequired = amount / 20; // number of $20 bills required

        if(count >= billsRequired)
        {
            return true; // enough bills available
        }
        else
        {
            return false; //not enough bills available
        }
    }// end method isSufficientCashAvailable

    public static void depositExecute(int currentAccountNumber)
    {
        // get deposit amount from user
        double amount = promptForDepositAmount();

        if(amount != CANCELED)
        {
            // request deposit envelope containing the specified amount
            displayMessage("\nPlease insert a deposit envelope containing ");
            displayDollarAmount(amount);
            displayMessageLine(".");

            // receive deposit envelope
            boolean envelopeReceived = isEnvelopeReceived();

            // check whether deposit envelope was received
            if(envelopeReceived)
            {
                displayMessageLine("\nYour envelope has been " +
                        "received. \nNOTE: The money just deposited will not " +
                        "be available until we verify the amount of any " +
                        "enclosed cash and your checks clear.");

                // credit account to reflect the deposit
                credit(currentAccountNumber, amount);
            }// end if
            else // deposit envelope not received
            {
                displayMessageLine("\nYou did not insert an " +
                        "envelope, so the ATM has canceled your transaction.");
            }// end else
        }// end if
        else // user canceled instead of entering amount
        {
            displayMessageLine("\nCanceling transaction...");
        }//end else
    }// end method execute

    // prompt user to enter a deposit amount in cents
    private static double promptForDepositAmount()
    {
        displayMessage("\nPlease enter a deposit amount in " +
                "CENTS (or 0 to cancel): ");

        int input = getInput(); // receive input of deposit amount

        // check whether the user canceled or entered a valid amount
        if(input == CANCELED)
        {
            return CANCELED;
        }// end if
        else
        {
            return (double) input / 100; // return dollar amount
        }// end else
    }// end method promptForDepositAmount

    public static boolean isEnvelopeReceived()
    {
        return true;
    }

    // display the main menu and return an input selection
    private static int displayMainMenu()
    {
        displayMessageLine("\nMain menu:");
        displayMessageLine("1 - View my balance");
        displayMessageLine("2 - Withdraw cash");
        displayMessageLine("3 - Deposit funds");
        displayMessageLine("4 - Exit\n");

        return getInput(); // return user's selection
    }// end method displayMainMenu

    private static void displayMessage(String message)
    {
        System.out.print(message);
    }// end method displayMessage

    // display a message with a carriage return
    private static void displayMessageLine(String message)
    {
        System.out.println(message);
    }// end method displayMessageLine

    // display a dollar amount
    private static void displayDollarAmount(double amount)
    {
        System.out.printf("$%,.2f", amount);
    }

    private static int getInput()
    {
        return input.nextInt(); // we assume that the user enters an integer
    }

    private static List getAccount(int accountNumber)
    {
        // loop through accounts searching for matching account number
        for (Integer key : accounts.keySet())
        {
            Object account = accounts.get(key);
            //return the current account if a match is found
            if ((int) ((List) account).get(0) == accountNumber)
            {
                return (List) account;
            }
        }// end for loop

        return null; // if no matching account is found, return null
    }// end method getAccount

    // determine whether the user specified account number and PIN matches
    // those of an account in the database
    private static boolean authenticateUser(int userAccountNumber, int userPIN)
    {
        // attempt to retrieve the account with the specified account number
        List userAccount = getAccount(userAccountNumber);

        // if account exists, return result of Account method validatePIN
        if(userAccount != null)
        {
            Integer pin = (int) userAccount.get(1);
            return pin == userPIN ? true : false;
        }
        else
        {
            return false; // account number not found, so return false
        }
    }// end method authenticateUser

    //return available balance of Account with specified account number
    public static double getAvailableBalance(int userAccountNumber)
    {
        List userAccount = getAccount(userAccountNumber);
        return (double) userAccount.get(2);
    }// end method getAvailableBalance

    // return total balance of Account with specified account number
    public static double getTotalBalance(int userAccountNumber)
    {
        List userAccount = getAccount(userAccountNumber);
        return (double) userAccount.get(3);
    }// end method getTotalBalance

    // credit an amount to Account with specified account number
    public static void credit(int userAccountNumber, double amount)
    {
        List userAccount = getAccount(userAccountNumber);
        double totalBalance = (double) userAccount.get(3);
        totalBalance += amount;
        userAccount.set(3, totalBalance);
    }// end method credit

    //debit an amount to Account with specified account number
    public static void debit(int userAccountNumber, double amount)
    {
        List userAccount = getAccount(userAccountNumber);
        double totalBalance = (double) userAccount.get(3);
        double availableBalance = (double) userAccount.get(3);
        totalBalance -= amount;
        availableBalance -= amount;
        userAccount.set(3, totalBalance);
        userAccount.set(2, availableBalance);
    }
}
package com.example.service.transaction;

import java.util.ArrayList;
import java.util.List;

import com.example.model.BalanceCommand;
import com.example.model.Command;
import com.example.model.CommandType;
import com.example.model.LoanCommand;
import com.example.model.PaymentCommand;
import com.example.model.RepaymentDetails;
import com.example.util.CalculationUtil;

public class RepaymentCalculationService
{
    private static final RepaymentCalculationService instance = new RepaymentCalculationService();

    private RepaymentCalculationService()
    {
    }

    public static RepaymentCalculationService getInstance()
    {
        return instance;
    }

    public List<RepaymentDetails> doCalculateRepayment(List<Command> commands)
    {
        LoanCommand loanCommand = (LoanCommand) commands.get(0);

        List<RepaymentDetails> loanDetailList = new ArrayList();

        RepaymentDetails loanDetails;

        int principleAmount = loanCommand.getPrincipal();

        int totalLoan = CalculationUtil.calculateTotalLoan(principleAmount, loanCommand.getNumberOfYears(), loanCommand.getInterestRate());
        int emi = CalculationUtil.calculateEmi(totalLoan, loanCommand.getNumberOfYears());

        int lumpSumPayment = 0;
        List<Command> transactions = commands.subList(1, commands.size());

        for (Command transaction : transactions)
        {
            if (CommandType.BALANCE.equals(transaction.getActionType()))
            {
                loanDetails = handleBalance(loanCommand, totalLoan, lumpSumPayment, emi, ((BalanceCommand) transaction).getEmiNo());
                loanDetailList.add(loanDetails);
            }
            else if (CommandType.PAYMENT.equals(transaction.getActionType()))
            {
                PaymentCommand paymentCommand = ((PaymentCommand) transaction);
                lumpSumPayment = paymentCommand.getLumpSumAmount();
            }
        }

        return loanDetailList;
    }

    private RepaymentDetails handleBalance(LoanCommand loanCommand, int totalLoan, int lumpSumPayment, int emiAmount, int noOfEmiPayed)
    {
        int totalAmountPayed = CalculationUtil.calculateTotalAmountPayed(emiAmount, noOfEmiPayed) + lumpSumPayment;

        int remainingEmis = CalculationUtil.calculateRemainingEmis(totalLoan - totalAmountPayed, emiAmount);

        return new RepaymentDetails(loanCommand.getBankName(), loanCommand.getBorrowerName(), totalAmountPayed, remainingEmis);
    }
}

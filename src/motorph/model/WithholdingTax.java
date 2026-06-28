package motorph.model;

// Withholding Tax calculation using BIR monthly bracket table.
// Input is taxableIncome (grossPay - SSS - PhilHealth - PagIbig).
// Overrides calculate() from GovernmentContribution — Polymorphism.
public class WithholdingTax extends GovernmentContribution {

    public WithholdingTax() {
        super("Withholding Tax");
    }

    @Override
    public double calculate(double taxableIncome) {
        if (taxableIncome <= 20833) {
            return 0;
        } else if (taxableIncome <= 33332) {
            return (taxableIncome - 20833) * 0.15;
        } else if (taxableIncome <= 66666) {
            return 1875.00 + (taxableIncome - 33332) * 0.20;
        } else if (taxableIncome <= 166666) {
            return 8541.80 + (taxableIncome - 66666) * 0.25;
        } else if (taxableIncome <= 666666) {
            return 33541.80 + (taxableIncome - 166666) * 0.30;
        } else {
            return 183541.80 + (taxableIncome - 666666) * 0.35;
        }
    }
}

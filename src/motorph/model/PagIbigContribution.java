package motorph.model;

// Pag-IBIG contribution calculation using tiered rate table.
// Overrides calculate() from GovernmentContribution — Polymorphism.
public class PagIbigContribution extends GovernmentContribution {

    private static final double MAX_CONTRIBUTION = 200.00;

    public PagIbigContribution() {
        super("Pag-IBIG");
    }

    @Override
    public double calculate(double grossPay) {
        double contribution;
        if (grossPay <= 1500) {
            contribution = grossPay * 0.01; // 1% for salary <= 1500
        } else {
            contribution = grossPay * 0.02; // 2% for salary > 1500
        }
        // Cap at maximum contribution
        if (contribution > MAX_CONTRIBUTION) {
            contribution = MAX_CONTRIBUTION;
        }
        return contribution;
    }
}

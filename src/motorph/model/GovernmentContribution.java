package motorph.model;

// Abstract parent class for all government-mandated deductions.
// Implements Calculable interface.
// Demonstrates Abstraction and serves as the base for Inheritance.
public abstract class GovernmentContribution implements Calculable {

    private String contributionName;

    public GovernmentContribution(String contributionName) {
        this.contributionName = contributionName;
    }

    public String getContributionName() {
        return contributionName;
    }

    // Abstract method — each subclass implements its own deduction formula.
    // Called polymorphically through List<GovernmentContribution>.
    @Override
    public abstract double calculate(double amount);
}

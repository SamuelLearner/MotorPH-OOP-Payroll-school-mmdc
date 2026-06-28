package motorph.model;

// PhilHealth contribution calculation using percentage-based formula.
// Overrides calculate() from GovernmentContribution — Polymorphism.
public class PhilHealthContribution extends GovernmentContribution {

    private static final double RATE = 0.05; // 5% total, split 50/50
    private static final double MIN_SALARY = 10000.00;
    private static final double MAX_SALARY = 100000.00;

    public PhilHealthContribution() {
        super("PhilHealth");
    }

    @Override
    public double calculate(double grossPay) {
        double salary = grossPay;
        if (salary < MIN_SALARY) {
            salary = MIN_SALARY;
        } else if (salary > MAX_SALARY) {
            salary = MAX_SALARY;
        }
        // Employee share is half of total premium
        return (salary * RATE) / 2.0;
    }
}

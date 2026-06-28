package motorph.model;

// SSS contribution calculation using bracket-based lookup table.
// Overrides calculate() from GovernmentContribution — Polymorphism.
public class SSSContribution extends GovernmentContribution {

    // SSS bracket table: {salaryFloor, contribution}
    private static final double[][] SSS_TABLE = {
        {0,        135.00},
        {3250,     157.50},
        {3750,     180.00},
        {4250,     202.50},
        {4750,     225.00},
        {5250,     247.50},
        {5750,     270.00},
        {6250,     292.50},
        {6750,     315.00},
        {7250,     337.50},
        {7750,     360.00},
        {8250,     382.50},
        {8750,     405.00},
        {9250,     427.50},
        {9750,     450.00},
        {10250,    472.50},
        {10750,    495.00},
        {11250,    517.50},
        {11750,    540.00},
        {12250,    562.50},
        {12750,    585.00},
        {13250,    607.50},
        {13750,    630.00},
        {14250,    652.50},
        {14750,    675.00},
        {15250,    697.50},
        {15750,    720.00},
        {16250,    742.50},
        {16750,    765.00},
        {17250,    787.50},
        {17750,    810.00},
        {18250,    832.50},
        {18750,    855.00},
        {19250,    877.50},
        {19750,    900.00},
        {20250,    922.50},
        {20750,    945.00},
        {21250,    967.50},
        {21750,    990.00},
        {22250,    1012.50},
        {22750,    1035.00},
        {23250,    1057.50},
        {23750,    1080.00},
        {24250,    1102.50},
        {24750,    1125.00}
    };

    public SSSContribution() {
        super("SSS");
    }

    @Override
    public double calculate(double grossPay) {
        double contribution = SSS_TABLE[0][1]; // default to lowest
        for (int i = SSS_TABLE.length - 1; i >= 0; i--) {
            if (grossPay >= SSS_TABLE[i][0]) {
                contribution = SSS_TABLE[i][1];
                break;
            }
        }
        return contribution;
    }
}

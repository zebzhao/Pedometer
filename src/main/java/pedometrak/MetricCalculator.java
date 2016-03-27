package pedometrak;

public class MetricCalculator {
    public static float calculateDistance() {
        return 0;
    }

    public static float calculateCalories() {
        return 1;
    }

    public static int calculateSteps() {
        return PedometerManager.steps.get(PedometerManager.steps.size() - 1) - PedometerManager.steps.get(0);
    }
}

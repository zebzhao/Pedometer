package com.pedometrak.util;

import com.pedometrak.PedometerManager;

public class MetricCalculator {

    private static float mStrideLength;

    public static void changeStrideLength(float stride) {
        mStrideLength = stride;
    }

    public static float calculateDistance() {
        return calculateSteps()*mStrideLength;
    }

    public static float calculateCalories() {
        float result = 0;
        Integer lastSteps = PedometerManager.steps.get(0);
        for (Integer currentSteps: PedometerManager.steps) {
            float stepsDelta = currentSteps - lastSteps;
            float distance = stepsDelta*mStrideLength;
            float speed = distance/2;
            result += 4.25*speed;
            lastSteps = currentSteps;
        }
        return result;
    }

    public static int calculateSteps() {
        return PedometerManager.steps.get(PedometerManager.steps.size() - 1) - PedometerManager.steps.get(0);
    }
}

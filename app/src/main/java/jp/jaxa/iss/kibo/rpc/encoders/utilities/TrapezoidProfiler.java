package jp.jaxa.iss.kibo.rpc.encoders.utilities;

/**
 * This is used to mirror the Kibo RPC API's Trapezoid profile to determine movement time.
 * <p></p>
 * This does not consider angular velocity so sufficient buffer time must be provided
 */
public class TrapezoidProfiler {
    private final double maxVelocity;
    private final double maxAcceleration;

    /**
     * Construct a TrapezoidProfiler
     *
     * @param maxVelocity The maximum velocity
     * @param maxAcceleration The maximum acceleration
     */
    public TrapezoidProfiler(double maxVelocity, double maxAcceleration) {
        this.maxVelocity = maxVelocity;
        this.maxAcceleration = maxAcceleration;
    }

    /**
     * Calculate the time it takes to travel a distance using a trapezoidal motion profile
     * <p></p>
     * This assumes the object will start and stop at rest.
     *
     * @param distance The distance to travel
     * @return The travel time in seconds
     */
    public double calculateTravelTime(double distance) {
        double accelerationTime = maxVelocity / maxAcceleration;

        double maxVelocityDist = distance - accelerationTime * accelerationTime * maxAcceleration;

        // Handle the case where the profile never reaches full speed
        if (maxVelocityDist < 0) {
            accelerationTime = Math.sqrt(distance / maxAcceleration);
            maxVelocityDist = 0;
        }

        double endFullSpeed = accelerationTime + maxVelocityDist / maxVelocity;
        double endDeccel = endFullSpeed + accelerationTime;

        return endDeccel;

    }
}
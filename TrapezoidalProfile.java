public class TrapezoidalProfile {
    static double maxVelocity = 0.5;
    static double maxAcceleration = 0.06;

    //public static double calculateTravelTime(Node startNode, Node endNode) {
    public static double calculateTravelTime() {
        double distance = distance();
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

    public static double distance(){
        return 5;
    }

    
    public static void main(String[] args) {
        
        double travelTime = calculateTravelTime();
        System.out.println(travelTime);
    }
}
import java.util.*;
import java.io.FileInputStream;
import java.io.IOException;

class Elevator implements Iterable<Passenger> {

    private int curr_floor;
    private List<Passenger> passengers;
    private List<Passenger> unload_passengers;
    private int capacity;
    protected int curr_tick;

    private static final Comparator<Elevator> elevator_compare = Comparator.comparingInt(Elevator::getCurrentFloor);
    private PriorityQueue<Elevator> elevator_order = new PriorityQueue<>(elevator_compare);

    public Elevator(int capacity) {
        this.curr_floor = 0;
        this.passengers = new ArrayList<>(capacity);
        this.unload_passengers= new ArrayList<>(capacity);
        this.capacity = capacity;
        this.elevator_order.add(this); // Add the elevator to the queue when created
        this.curr_tick=0;
    }

    public int getCurrentFloor() {
        return curr_floor;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public List<Passenger> getUnloadPassengers() {
        return unload_passengers;
    }

    public void move(int floor) {
        this.curr_floor = floor;
    }

    public int getCurrentTick() {
        return curr_tick;
    }

    public void loadPassenger(Passenger passenger) {
        if (passengers.size() < capacity) {
            passengers.add(passenger);
            passenger.setConveyanceTick(curr_tick); // Set the conveyance tick when loading
        }

        if (unload_passengers.size() < capacity) {
            unload_passengers.add(passenger);
            passenger.setConveyanceTick(curr_tick); // Set the conveyance tick when loading
        }


    }

    public void unloadPassengers(int targetFloor) {
        unload_passengers.removeIf(p -> p.getDestinationFloor() == targetFloor);
    }

    public void reportResults() {

        int total_passengers = 0;
        int total_time = 0;
        int longest_time = Integer.MIN_VALUE;
        int shortest_time = Integer.MAX_VALUE;

        for (Passenger passenger : passengers ) {
            int arrival_time = passenger.getArrivalTick();
            int conveyance_time = passenger.getConveyanceTick();
            int time_diff = conveyance_time - arrival_time;

            total_passengers++;
            total_time += time_diff;

            if (time_diff > longest_time) {
                longest_time = time_diff;
            }
            if (time_diff < shortest_time) {
                shortest_time = time_diff;
            }
        }

        double average_time;
        if (total_passengers > 0) {
            average_time = (double) total_time / total_passengers;
        } else {
            average_time = 0;
        }

        System.out.println("Average time: " + average_time);
        System.out.println("Longest time: " + longest_time);
        System.out.println("Shortest time: " + shortest_time);
    }

    public void move() {
        if (!elevator_order.isEmpty()) {
            Elevator nextElevator = elevator_order.poll();
            int targetFloor = chooseTargetFloor(nextElevator);
            nextElevator.move(targetFloor);
            elevator_order.add(nextElevator); // Add back to the queue after moving
        }
    }

    private int chooseTargetFloor(Elevator elevator) {
        int currentFloor = elevator.getCurrentFloor();
        List<Passenger> passengers = elevator.getPassengers();

        if (passengers.isEmpty()) {
            // If there are no passengers, stay on the current floor
            return currentFloor;
        }

        // Find the closest destination floor among the passengers
        int closestDestination = findClosestDestination(currentFloor, passengers);

        // Determine the direction to the closest destination
        int direction = Integer.compare(closestDestination, currentFloor);

        // Choose the next target floor based on the direction
        int targetFloor = currentFloor + direction;

        return targetFloor;
    }

    private int findClosestDestination(int currentFloor, List<Passenger> passengers) {
        int closestDestination = Integer.MAX_VALUE;

        for (Passenger passenger : passengers) {
            int destination = passenger.getDestinationFloor();
            int distance = Math.abs(destination - currentFloor);

            if (distance < closestDestination) {
                closestDestination = destination;
            }
        }

        return closestDestination;
    }

    // Implement Iterator for the Elevator class
    protected class ElevatorIterator implements Iterator<Passenger> {
        private Iterator<Passenger> passengerIterator;

        public ElevatorIterator() {
            this.passengerIterator = passengers.iterator();
        }

        public boolean hasNext() {
            return passengerIterator.hasNext();
        }

        public Passenger next() {
            return passengerIterator.next();
        }
    }

    public Iterator<Passenger> iterator() {
        return new ElevatorIterator();
    }
}

class Passenger {
    private int arrivalTick;
    private int destinationFloor;
    private int conveyanceTick;

    public Passenger(int arrivalTick, int destinationFloor) {
        this.arrivalTick = arrivalTick;
        this.destinationFloor = destinationFloor;
        this.conveyanceTick = -1; // Initialize with an invalid value
    }

    public int getArrivalTick() {
        return arrivalTick;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public int getConveyanceTick() {
        return conveyanceTick;
    }

    public void setConveyanceTick(int conveyanceTick) {
        this.conveyanceTick = conveyanceTick;
    }
}

class Floor {
    private int floorNumber;
    private List<Passenger> passengers;

    public Floor() {
        this.passengers = new ArrayList<>();
    }

    public void addPassenger(Passenger passenger) {
        passengers.add(passenger);
    }

    public void loadPassengers(Elevator elevator) {
        List<Passenger> toLoad = new ArrayList<>();
        for (Passenger passenger : passengers) {
            if (passenger.getDestinationFloor() == elevator.getCurrentFloor()) {
                toLoad.add(passenger);
            }
        }
        for (Passenger passenger : toLoad) {
            passengers.remove(passenger);
            elevator.loadPassenger(passenger);
        }
    }

    public int getFloorNumber() {
        return floorNumber;
    }
}

class SimulationConfiguration {


    public int duration;
    public int numFloors;
    public int numElevators;
    public int elevatorCapacity;
    public String structures ="array" ;

    public void readPropertyFile(String filePath) {

        Properties properties = new Properties();


        try (FileInputStream fis = new FileInputStream(filePath)) {

            properties.load(fis);

            this.structures = properties.getProperty("structures", "list");
            this.numFloors = Integer.parseInt(properties.getProperty("floors", "10"));
            this.numElevators = Integer.parseInt(properties.getProperty("elevators", "1"));
            this.elevatorCapacity = Integer.parseInt(properties.getProperty("elevatorCapacity", "10"));
            this.duration = Integer.parseInt(properties.getProperty("duration", "500"));

        }
        catch (IOException e) {
            e.printStackTrace();
            // Use default values if there is an error reading the property file
            this.structures ="list";
            this.numFloors = 10;
            this.numElevators = 1;
            this.elevatorCapacity = 10;
            this.duration = 500;
        }
    }

}

class ElevatorSimulationUsingArray {

    private List<Floor> floors;
    private List<Elevator> elevators;

    int totalPassengers = 0;
    int totalTime = 0;
    int longestTime = 0;
    int shortestTime = 0;

    public ElevatorSimulationUsingArray() {

        this.floors = new ArrayList<>();
        this.elevators = new ArrayList<>();
    }

    public void createFloors(int numFloors) {
        for (int i = 0; i < numFloors; i++) {
            floors.add(new Floor());
        }
    }

    public void createElevators(int numElevators, int elevatorCapacity) {
        for (int i = 0; i < numElevators; i++) {
            elevators.add(new Elevator(elevatorCapacity));
        }
    }

    public void generatePassengers(double probability, SimulationConfiguration simConf) {

        Random rand = new Random();
        for (Floor floor : floors) {

            if (rand.nextDouble() < probability) {
                int destinationFloor = rand.nextInt(simConf.numFloors);
                int arrivalTime = rand.nextInt(simConf.duration); // Random arrival time within the duration
                Passenger passenger = new Passenger(arrivalTime, destinationFloor);
                floor.addPassenger(passenger);
            }
        }
    }

    public void runSimulation(SimulationConfiguration simConf) {

        int currentTick = 0;

        for (int tick = 0; tick < simConf.duration; tick++) {

            currentTick = tick;

            for (Elevator elevator : elevators) {
                Floor currentFloor = floors.get(elevator.getCurrentFloor());
                elevator.unloadPassengers(currentFloor.getFloorNumber());
                currentFloor.loadPassengers(elevator);
            }

            for (Elevator elevator : elevators) {
                elevator.move();
            }

            // New passengers
            generatePassengers(0.2, simConf); // Example
        }


        for (Elevator elevator : elevators) {
            for (Passenger passenger : elevator) {
                passenger.setConveyanceTick(currentTick);

            }

            for (Passenger passenger : elevator.getPassengers()) {
                passenger.setConveyanceTick(currentTick); // Set to the end of the simulation for passengers not yet conveyed


            }
        }
    }

    public int chooseTargetFloor() {
        return 1; // Placeholder value
    }

    public void reportResults(SimulationConfiguration simConf) {

        int totalPassengers = 0;
        int totalTime = 0;
        int longestTime = 0;
        int shortestTime = simConf.duration;  // Initialize to the maximum possible duration

        for (Elevator elevator : elevators) {

            for (Passenger passenger : elevator.getPassengers()) {

                int arrivalTime = passenger.getArrivalTick();
                int conveyanceTime = passenger.getConveyanceTick();
                int timeDifference = conveyanceTime - arrivalTime;

                totalPassengers++;
                totalTime = timeDifference;

                if (timeDifference > longestTime) {
                    longestTime = timeDifference;
                }

                if (timeDifference < shortestTime) {
                    shortestTime = timeDifference;
                }
            }
        }

        double averageTime;

        if (totalPassengers > 0) {
            averageTime = (double) totalTime / totalPassengers;
        }
        else {
            averageTime = 0;
        }

        System.out.println("Average time: " + averageTime);
        System.out.println("Longest time: " + longestTime);
        System.out.println("Shortest time: " + shortestTime);
    }

}

class ElevatorSimulationUsingLinkedList {
    private LinkedList<Floor> floors;
    private LinkedList<Elevator> elevators;
    public ElevatorSimulationUsingLinkedList() {
        this.floors = new LinkedList<>();
        this.elevators = new LinkedList<>();
    }

    public void createFloors(int numFloors) {
        for (int i = 0; i < numFloors; i++) {
            floors.add(new Floor());
        }
    }
    public void createElevators(int numElevators, int elevatorCapacity) {
        for (int i = 0; i < numElevators; i++) {
            elevators.add(new Elevator(elevatorCapacity));
        }
    }

    public void generatePassengers(double probability, SimulationConfiguration simConf) {

        Random rand = new Random();
        for (Floor floor : floors) {
            if (rand.nextDouble() < probability) {
                int destinationFloor = rand.nextInt(simConf.numFloors);
                int arrivalTime = rand.nextInt(simConf.duration); // Random arrival time within the duration
                Passenger passenger = new Passenger(arrivalTime, destinationFloor);
                floor.addPassenger(passenger);
            }
        }
    }

    public void runSimulation(SimulationConfiguration simConf) {

        int currentTick = 0;

        for (int tick = 0; tick < simConf.duration; tick++) {
            currentTick = tick;

            for (Elevator elevator : elevators) {

                Floor currentFloor = floors.get(elevator.getCurrentFloor());

                elevator.unloadPassengers(currentFloor.getFloorNumber());

                currentFloor.loadPassengers(elevator);
            }

            for (Elevator elevator : elevators) {
                elevator.move();
            }

            // New passengers
            generatePassengers(0.2, simConf); // Example
        }


        for (Elevator elevator : elevators) {
            for (Passenger passenger : elevator) {
                passenger.setConveyanceTick(currentTick);

            }

            for (Passenger passenger : elevator.getPassengers()) {
                passenger.setConveyanceTick(currentTick); // Set to the end of the simulation for passengers not yet conveyed


            }
        }
    }

    public int chooseTargetFloor() {
        return 1; // Placeholder value
    }

    public void reportResults(SimulationConfiguration simConf) {

        int totalPassengers = 0;
        int totalTime = 0;
        int longestTime = 0;
        int shortestTime = simConf.duration;  // Initialize to the maximum possible duration

        for (Elevator elevator : elevators) {

            for (Passenger passenger : elevator.getPassengers()) {

                int arrivalTime = passenger.getArrivalTick();
                int conveyanceTime = passenger.getConveyanceTick();
                int timeDifference = conveyanceTime - arrivalTime;

                System.out.println("Passenger:" + passenger.getDestinationFloor());
                totalPassengers++;
                totalTime = timeDifference;

                if (timeDifference > longestTime) {
                    longestTime = timeDifference;
                }

                if (timeDifference < shortestTime) {
                    shortestTime = timeDifference;
                }
            }
        }

        double averageTime;

        if (totalPassengers > 0) {
            averageTime = (double) totalTime / totalPassengers;
        } else {
            averageTime = 0;
        }

        System.out.println("Average time: " + averageTime);
        System.out.println("Longest time: " + longestTime);
        System.out.println("Shortest time: " + shortestTime);
    }
}

public class ElevatorSimulation {

    public static void main(String[] args) {

        SimulationConfiguration simConf = new SimulationConfiguration() ;

        if (args.length > 0) {
            simConf.readPropertyFile(args[0]);
        }

        if (simConf.numFloors <= 0) {
            simConf.numFloors = 32;
        }
        if (simConf.numElevators <= 0) {
            simConf.numElevators = 1;
        }
        if (simConf.elevatorCapacity <= 0) {
            simConf.elevatorCapacity = 10;
        }
        if (simConf.duration <= 0) {
            simConf.duration = 500;
        }

        System.out.println("structures : " + simConf.structures );
        System.out.println("Num Floors : " + simConf.numFloors );
        System.out.println("Num Elevators : " + simConf.numElevators );
        System.out.println("Num elevatorCapacity :" + simConf.elevatorCapacity );
        System.out.println("Num duration : " + simConf.duration );
        System.out.println();

        if (simConf.structures.equals("array")) {

            ElevatorSimulationUsingArray simulation = new ElevatorSimulationUsingArray();

            simulation.createFloors(simConf.numFloors);
            simulation.createElevators(simConf.numElevators, simConf.elevatorCapacity);
            simulation.runSimulation(simConf);
            simulation.reportResults(simConf);

        }
        else if ( simConf.structures.equals("linked")) {

            ElevatorSimulationUsingLinkedList simulation = new ElevatorSimulationUsingLinkedList();

            simulation.createFloors(simConf.numFloors);
            simulation.createElevators(simConf.numElevators, simConf.elevatorCapacity);
            simulation.runSimulation(simConf);
            simulation.reportResults(simConf);

        }

    }

}


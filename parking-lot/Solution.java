/*
 * Click `Run` to execute the snippet below!
 */

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
/*
 * To execute Java, please define "static void main" on a class
 * named Solution.
 *
 * If you need more classes, simply define them inline.
 */

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;
import java.util.*;

class ParkingLot {
    private Map<Integer, ParkingSpot> parkingSpots;
    private int capacity;
    private Map<String, ParkingTicket> ticketMap; // Map to store active tickets
    private List<Payment> payments; // List to store payment records

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        this.parkingSpots = new HashMap<>(capacity);
        this.ticketMap = new HashMap<>();
        this.payments = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            parkingSpots.put(i + 1, new ParkingSpot(i + 1));
        }
    }

    public ParkingTicket parkVehicle(Vehicle vehicle) {
        // Check if there is an available parking spot
        for (ParkingSpot spot : parkingSpots.values()) {
            if (spot.isAvailable()) {
                ParkingTicket ticket = new ParkingTicket(vehicle, spot);
                spot.parkVehicle();
                ticketMap.put(ticket.getTicketId(), ticket);
                return ticket;
            }
        }
        return null; // No available spots
    }

    public Vehicle retrieveVehicle(String ticketId, double paymentAmount) {
        ParkingTicket ticket = ticketMap.get(ticketId);
        if (ticket != null && ticket.isTicketValid()) {
            ParkingSpot spot = ticket.getParkingSpot();
            if (spot != null) {
                spot.removeVehicle();
                ticketMap.remove(ticketId);

                // Create a payment and associate it with the ticket
                Payment payment = new Payment(generatePaymentId(), ticket, paymentAmount);
                ticket.setPayment(payment);
                payments.add(payment);

                return ticket.getVehicle();
            }
        }
        return null; // Invalid ticket or spot
    }

    public boolean isParkingAvailable() {
        return parkingSpots.values().stream().anyMatch(ParkingSpot::isAvailable);
    }

    private String generatePaymentId() {
        // Generate a unique payment ID (e.g., using UUID)
        return "PAY-" + Math.random();
    }

    public List<Payment> getPayments() {
        return payments;
    }

    // Other methods for checking available spots, vehicle retrieval, etc.
}

class ParkingSpot {
    private int spotNumber;
    private boolean available;

    public ParkingSpot(int spotNumber) {
        this.spotNumber = spotNumber;
        this.available = true;
    }

    public boolean isAvailable() {
        return available;
    }

    public void parkVehicle() {
        this.available = false;
    }

    public void removeVehicle() {
        this.available = true;
    }

    public int getSpotNumber() {
        return spotNumber;
    }
}

class Vehicle {
    private String registrationNumber;

    public Vehicle(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }
}

class ParkingTicket {
    private String ticketId;
    private Vehicle vehicle;
    private ParkingSpot parkingSpot;
    private LocalDateTime expirationTime;
    private Payment payment; // Reference to the associated payment

    public ParkingTicket(Vehicle vehicle, ParkingSpot parkingSpot) {
        this.ticketId = generateTicketId();
        this.vehicle = vehicle;
        this.parkingSpot = parkingSpot;
        this.expirationTime = LocalDateTime.now().plusHours(2); // Example: Ticket valid for 2 hours
    }

    private String generateTicketId() {
        // Generate a unique ticket ID (e.g., using UUID)
        return "TICKET-" + Math.random();
    }

    public boolean isTicketValid() {
        return LocalDateTime.now().isBefore(expirationTime);
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Payment getPayment() {
        return payment;
    }
}

class Payment {
    private String paymentId;
    private ParkingTicket parkingTicket;
    private double amount;
    private LocalDateTime paymentTime;

    public Payment(String paymentId, ParkingTicket parkingTicket, double amount) {
        this.paymentId = paymentId;
        this.parkingTicket = parkingTicket;
        this.amount = amount;
        this.paymentTime = LocalDateTime.now(); // Payment timestamp
    }

    public String getPaymentId() {
        return paymentId;
    }

    public ParkingTicket getParkingTicket() {
        return parkingTicket;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }
}

class Solution {
    public static void main(String[] args) {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("Hello, World!");
        strings.add("Welcome to CoderPad.");
        strings.add("This pad is running Java " + Runtime.version().feature());

        // var parkingLot = new ParkingLot(5);
        // var ticketList = new ArrayList<ParkingTicket>();
        // for (int i = 0; i < 7 ; i++) {
        // var vehicle = new Vehicle("HR1234" + i);
        // var ticket = parkingLot.parkVehicle(vehicle);
        // if(ticket != null){
        // ticketList.add(ticket);
        // System.out.println("vehicle " + vehicle.getRegistrationNumber() +" parked at
        // spot " + ticket.getParkingSpot().getSpotNumber());
        // }
        // else
        // System.out.println("parking is full so vehicle " +
        // vehicle.getRegistrationNumber() +" can not be parked");
        // }
        // parkingLot.retrieveVehicle(ticketList.get(0), 10.0);
        // var ticket = parkingLot.parkVehicle(new Vehicle("abc"));
        // System.out.println(ticket.getTicketId());
        ParkingLot parkingLot = new ParkingLot(10);

        // Create vehicles
        Vehicle vehicle1 = new Vehicle("ABC123");
        Vehicle vehicle2 = new Vehicle("XYZ789");

        // Park vehicles
        ParkingTicket ticket1 = parkingLot.parkVehicle(vehicle1);
        ParkingTicket ticket2 = parkingLot.parkVehicle(vehicle2);

        if (ticket1 != null) {
            System.out.println("Vehicle 1 parked successfully. Ticket ID: " + ticket1.getTicketId());
        } else {
            System.out.println("No available parking spots for Vehicle 1.");
        }

        if (ticket2 != null) {
            System.out.println("Vehicle 2 parked successfully. Ticket ID: " + ticket2.getTicketId());
        } else {
            System.out.println("No available parking spots for Vehicle 2.");
        }

        // Check parking availability
        boolean isParkingAvailable = parkingLot.isParkingAvailable();
        System.out.println("Parking is available: " + isParkingAvailable);

        // Retrieve vehicles with payment
        double paymentAmount1 = 10.0; // Example payment amount
        double paymentAmount2 = 12.0; // Example payment amount
        Vehicle retrievedVehicle1 = parkingLot.retrieveVehicle(ticket1.getTicketId(), paymentAmount1);
        if (retrievedVehicle1 != null) {
            Payment payment1 = ticket1.getPayment();
            System.out.println("Vehicle 1 retrieved successfully. Registration Number: "
                    + retrievedVehicle1.getRegistrationNumber());
            System.out.println("Payment ID: " + payment1.getPaymentId());
            System.out.println("Payment Amount: " + payment1.getAmount());
            System.out.println("Payment Time: " + payment1.getPaymentTime());
        } else {
            System.out.println("Failed to retrieve Vehicle 1.");
        }

        // Check parking availability again
        isParkingAvailable = parkingLot.isParkingAvailable();
        System.out.println("Parking is available: " + isParkingAvailable);

        // Attempt to retrieve Vehicle 2 with an insufficient payment
        Vehicle retrievedVehicle2 = parkingLot.retrieveVehicle(ticket2.getTicketId(), paymentAmount2);
        if (retrievedVehicle2 != null) {
            Payment payment2 = ticket2.getPayment();
            System.out.println("Vehicle 2 retrieved successfully. Registration Number: "
                    + retrievedVehicle2.getRegistrationNumber());
            System.out.println("Payment ID: " + payment2.getPaymentId());
            System.out.println("Payment Amount: " + payment2.getAmount());
            System.out.println("Payment Time: " + payment2.getPaymentTime());
        } else {
            System.out.println("Failed to retrieve Vehicle 2 due to insufficient payment.");
        }

        // Display payment records
        List<Payment> payments = parkingLot.getPayments();
        System.out.println("Payment Records:");
        for (Payment payment : payments) {
            System.out.println("Payment ID: " + payment.getPaymentId());
            System.out.println("Ticket ID: " + payment.getParkingTicket().getTicketId());
            System.out.println("Payment Amount: " + payment.getAmount());
            System.out.println("Payment Time: " + payment.getPaymentTime());
            System.out.println("--------------------");
        }

    }
}

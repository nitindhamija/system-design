# Problem Statement: Parking Management System

You are tasked with designing an API and a database schema for a parking management system. The system should allow users to perform the following operations:

Park a Vehicle: Users should be able to park their vehicles in available parking spots. Each parking spot can accommodate one vehicle. When a vehicle is parked, it should be associated with a unique parking ticket.

Retrieve a Vehicle: Users should be able to retrieve their parked vehicles by providing a valid parking ticket. Upon retrieval, the parking spot should become available for other vehicles to use.

Check Parking Availability: Users should be able to check if there are any available parking spots.

Your design should consider the following requirements:

The parking lot has a limited number of parking spots.
Each vehicle has a unique registration number.
Parking tickets should be generated and associated with parked vehicles.
Users should be able to pay for parking (a basic payment feature is sufficient for this simulation).
Parking tickets should have an expiration time, after which they are considered invalid.
The system should prevent multiple vehicles from occupying the same spot.
The system should handle concurrent requests without data inconsistencies.

# API Design:

Your API should expose endpoints for the operations mentioned above. Here's an example of how you can design the API:

POST /parking: Park a vehicle and generate a parking ticket.
GET /parking/{ticketId}: Retrieve a vehicle using a valid parking ticket.
GET /parking/available: Check parking availability.
POST /payment: Simulate a payment process.

# Database Schema:

Your database should store information about parking spots, parked vehicles, parking tickets, and payments. Here's a simplified schema:

ParkingSpot table: Stores information about parking spots, including their unique IDs and availability status.
Vehicle table: Stores information about vehicles, including their registration numbers.
ParkingTicket table: Stores information about parking tickets, including ticket IDs, associated vehicle IDs, expiration times, and payment status.
Payment table: Stores payment-related information.

## Database Tables:

- ParkingSpot Table:

spot_id (Primary Key): Unique identifier for each parking spot.
is_available: Boolean flag to indicate whether the spot is available for parking.

- Vehicle Table:

vehicle_id (Primary Key): Unique identifier for each vehicle.
registration_number: A unique identifier for each vehicle (e.g., license plate number).

- ParkingTicket Table:

ticket_id (Primary Key): Unique identifier for each parking ticket.
vehicle_id (Foreign Key): References the vehicle that was parked.
spot_id (Foreign Key): References the parking spot where the vehicle is parked.
entry_time: Timestamp indicating when the vehicle was parked.
exit_time: Timestamp indicating when the vehicle was retrieved.
is_paid: Boolean flag indicating whether the parking fee has been paid.

- Payment Table:

payment_id (Primary Key): Unique identifier for each payment.
ticket_id (Foreign Key): References the parking ticket associated with the payment.
amount: The amount paid for parking.
payment_time: Timestamp indicating when the payment was made.

## Entity Relationships:

Each parking spot (ParkingSpot) is associated with zero or one parking ticket (ParkingTicket) when a vehicle is parked.
Each vehicle (Vehicle) can be associated with zero or one parking tickets when parked and retrieved.
Each parking ticket (ParkingTicket) is associated with one parking spot, one vehicle, and zero or one payment records.
Each payment (Payment) is associated with one parking ticket.
## Database Constraints:

Ensure that parking spots are uniquely identified and marked as available or unavailable.
Vehicle registration numbers should be unique.
Parking tickets should have a start time (entry_time) when the vehicle is parked and an optional exit time (exit_time) when the vehicle is retrieved.
Payments should be associated with valid parking tickets.
Implement foreign key constraints to maintain referential integrity.
## Indexing:

Create appropriate indexes on frequently queried columns, such as vehicle_id and spot_id, to optimize query performance.
## Considerations:

Implement data retention policies to remove old or expired data (e.g., parking tickets with exit times in the past).
Ensure proper handling of concurrency issues to avoid data inconsistencies when multiple users access the system concurrently.
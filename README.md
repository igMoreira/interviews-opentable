# Private Dining Management System

A comprehensive RESTful API for managing private dining reservations at restaurants. This system provides robust reservation management with intelligent time slot optimization, flexible capacity management, and detailed occupancy analytics.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Key Features](#key-features)
  - [Operating Windows Enforcement](#operating-windows-enforcement)
  - [Slot Optimization](#slot-optimization)
  - [Flexible Capacity Management](#flexible-capacity-management)
  - [Occupancy Analytics Reporting](#occupancy-analytics-reporting)
- [API Reference](#api-reference)
  - [Restaurant API](#restaurant-api)
  - [Reservation API](#reservation-api)
  - [Analytics API](#analytics-api)
- [Technical Design](#technical-design)
  - [Data Model](#data-model)
  - [Reservation Validation Pipeline](#reservation-validation-pipeline)
  - [Caching](#caching)
- [Prerequisites](#prerequisites)
- [Running the Project](#running-the-project)
- [Testing & Coverage](#testing--coverage)

---

## Overview

The Private Dining Management System is a Spring Boot application designed to handle the complexities of private dining space reservations. It enables restaurants to:

- Manage multiple private dining spaces with individual configurations
- Accept and validate reservations with automatic time slot alignment
- Allow concurrent reservations while respecting capacity limits
- Generate detailed occupancy analytics reports

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    PRIVATE DINING SYSTEM OVERVIEW                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌──────────────┐    ┌──────────────┐    ┌──────────────────────────────┐  │
│   │  Restaurant  │───▶│    Spaces    │───▶│       Reservations           │  │
│   │  Management  │    │  Management  │    │        Management            │  │
│   └──────────────┘    └──────────────┘    └──────────────────────────────┘  │
│                                                        │                    │
│                                                        ▼                    │
│                                           ┌──────────────────────────────┐  │
│                                           │   Occupancy Analytics        │  │
│                                           │      Reporting               │  │
│                                           └──────────────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Architecture

The system follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              API LAYER                                      │
│  ┌─────────────────────────┐  ┌─────────────────────────────────────────┐   │
│  │  RestaurantController   │  │        ReservationController            │   │
│  │  • CRUD operations      │  │        • Create/Get/Delete reservations │   │
│  │  • Space management     │  │        • Validation pipeline            │   │
│  │  • Analytics endpoint   │  └─────────────────────────────────────────┘   │
│  └─────────────────────────┘                                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                            SERVICE LAYER                                    │
│  ┌──────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐   │
│  │ RestaurantService│  │  ReservationService  │  │OccupancyAnalytics    │   │
│  │                  │  │  • Time alignment    │  │Service               │   │
│  │                  │  │  • Validation chain  │  │  • Report generation │   │
│  │                  │  │  • Hours enforcement │  │  • Hourly breakdown  │   │
│  └──────────────────┘  └──────────────────────┘  └──────────────────────┘   │
│                              │                                              │
│                              ▼                                              │
│                   ┌──────────────────────┐                                  │
│                   │CapacityValidation    │                                  │
│                   │Service               │                                  │
│                   │• Concurrent checking │                                  │
│                   │• Overlap detection   │                                  │
│                   └──────────────────────┘                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                          REPOSITORY LAYER                                   │
│  ┌─────────────────────────┐  ┌─────────────────────────────────────────┐   │
│  │  RestaurantRepository   │  │        ReservationRepository            │   │
│  │                         │  │        • Overlap queries                │   │
│  │                         │  │        • Time range queries             │   │
│  └─────────────────────────┘  └─────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────────────────┤
│                          DATA LAYER (MongoDB)                               │
│  ┌─────────────────────────┐  ┌─────────────────────────────────────────┐   │
│  │     restaurants         │  │           reservations                  │   │
│  │     collection          │  │           collection                    │   │
│  └─────────────────────────┘  └─────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Technology Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.3.11 |
| Language | Java 17 |
| Database | MongoDB (Embedded for development) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito |
| Coverage | JaCoCo (80% minimum) |
| Build | Maven |

---

## Key Features

### Operating Windows Enforcement

**Description**: The system enforces that reservations can only be made within defined operating hours for each space (e.g., 9:00 AM - 10:00 PM).

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    OPERATING HOURS VALIDATION FLOW                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   Reservation Request                                                       │
│   Start: 08:30, End: 10:30                                                  │
│           │                                                                 │
│           ▼                                                                 │
│   ┌───────────────────┐                                                     │
│   │ Align to Time     │  Start: 08:30 → 09:00 (nearest slot)                │
│   │ Slot Boundaries   │  End: 10:30 → 11:00 (ceiling)                       │
│   └─────────┬─────────┘                                                     │
│             ▼                                                               │
│   ┌───────────────────────────────────────────────────────────────────────┐ │
│   │                    OPERATING HOURS CHECK                              │ │
│   │                                                                       │ │
│   │   Space Operating Hours: 09:00 ═══════════════════════════════ 22:00  │ │
│   │                          │                                       │    │ │
│   │   Aligned Reservation:   09:00 ══════ 11:00                           │ │
│   │                          │             │                              │ │
│   │                          ✓ Valid       ✓ Valid                        │ │
│   │                                                                       │ │
│   └───────────────────────────────────────────────────────────────────────┘ │
│             │                                                               │
│             ▼                                                               │
│   ✅ Reservation Accepted                                                   │
│                                                                             │
│   ─────────────────────────────────────────────────────────────────────     │
│                                                                             │
│   REJECTED EXAMPLE:                                                         │
│   Request: 21:00 → 23:30 (aligned: 21:00 → 00:00)                           │
│                                                                             │
│   Space Operating Hours: 09:00 ═══════════════════════════════ 22:00       │
│                                                                  │          │
│   Aligned Reservation:                              21:00 ═══════════ 00:00 │
│                                                              │          │   │
│                                                              ✓        ✗     │
│                                                                   Outside   │
│             │                                                               │
│             ▼                                                               │
│   ❌ OutsideOperatingHoursException                                         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Implementation Details**:

- Each space has configurable `operatingStartTime` and `operatingEndTime`
- Default operating hours: **09:00 - 22:00** (configurable in `application.yml`)
- Validation occurs **after** time slot alignment
- Throws `OutsideOperatingHoursException` with detailed message

**Pros**:
- ✅ Per-space configuration allows different operating hours for different spaces
- ✅ Centralized default configuration reduces repetition
- ✅ Clear error messages help users understand constraints
- ✅ Validation after alignment prevents edge cases

**Cons**:
- ⚠️ No support for multiple operating windows per day (e.g., closed for lunch)
- ⚠️ No day-of-week variation (e.g., different Sunday hours)
- ⚠️ Does not handle overnight reservations (crossing midnight)

---

### Slot Optimization

**Description**: The system uses configurable time-block intervals to maximize table turnover and capacity by automatically aligning reservation times to slot boundaries.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      TIME SLOT ALIGNMENT ALGORITHM                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   Configuration: 60-minute slots                                            │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │ START TIME ALIGNMENT (Round to Nearest)                             │   │
│   │                                                                     │   │
│   │   12:00          12:30          13:00                               │   │
│   │     │              │              │                                 │   │
│   │     ├──────────────┼──────────────┤                                 │   │
│   │     │   ◄──────────┼──────────►   │                                 │   │
│   │     │  Round Down  │  Round Up    │                                 │   │
│   │                                                                     │   │
│   │   Example: 12:17 → 12:00 (before halfway)                           │   │
│   │   Example: 12:45 → 13:00 (after halfway)                            │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │ END TIME ALIGNMENT (Round Up / Ceiling)                             │   │
│   │                                                                     │   │
│   │   14:00          14:30          15:00                               │   │
│   │     │              │              │                                 │   │
│   │     ├──────────────┴──────────────┤                                 │   │
│   │     │        Always Round Up  ───►│                                 │   │
│   │                                                                     │   │
│   │   Example: 14:10 → 15:00                                            │   │
│   │   Example: 14:00 → 14:00 (already aligned)                          │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │ PRACTICAL EXAMPLE                                                   │   │
│   │                                                                     │   │
│   │   Customer Request: 12:17 - 14:10                                   │   │
│   │                                                                     │   │
│   │   09:00   10:00   11:00   12:00   13:00   14:00   15:00   16:00     │   │
│   │     │       │       │       │       │       │       │       │       │   │
│   │     ├───────┼───────┼───────┼───────┼───────┼───────┼───────┤       │   │
│   │                             │               │                       │   │
│   │                             ├───────────────┤                       │   │
│   │                          12:00           14:00  (Original slots)    │   │
│   │                             │               │                       │   │
│   │                             ├───────────────┼───────┤               │   │
│   │                          12:00           14:00   15:00              │   │
│   │                             │                       │               │   │
│   │                             └───────────────────────┘               │   │
│   │                              Aligned: 12:00 - 15:00                 │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Implementation Details**:

- Default slot duration: **60 minutes** (configurable per space)
- Start time: Rounded to **nearest** slot boundary
- End time: Rounded **up** (ceiling) to ensure minimum coverage
- Minimum reservation duration: One complete time slot

**Pros**:
- ✅ Maximizes table utilization by creating predictable booking windows
- ✅ Simplifies overlap detection and capacity calculations
- ✅ Per-space configuration supports different dining experiences (quick lunch vs. fine dining)
- ✅ Transparent adjustment - original request preserved in response

**Cons**:
- ⚠️ May result in longer reservations than requested (extra time billed?)
- ⚠️ Fixed slot boundaries might not suit all business models
- ⚠️ No support for variable slot durations based on party size or meal type

---

### Flexible Capacity Management

**Description**: The system allows concurrent reservations in the same space as long as the combined headcount (sum of all party sizes) remains within the space's maximum capacity.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    FLEXIBLE CAPACITY MANAGEMENT                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   Space Configuration:                                                      │
│   • Min Capacity: 2                                                         │
│   • Max Capacity: 20                                                        │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │ TWO-LEVEL VALIDATION                                                │   │
│   │                                                                     │   │
│   │   Level 1: Per-Reservation Validation                               │   │
│   │   ────────────────────────────────────                              │   │
│   │   • Party size must be >= minCapacity (2)                           │   │
│   │   • Party size must be <= maxCapacity (20)                          │   │
│   │                                                                     │   │
│   │   Level 2: Concurrent Capacity Validation                           │   │
│   │   ────────────────────────────────────────                          │   │
│   │   • Sum of ALL overlapping parties <= maxCapacity                   │   │
│   │                                                                     │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │ EXAMPLE SCENARIO                                                    │   │
│   │                                                                     │   │
│   │   12:00   13:00   14:00   15:00   16:00   17:00                      │   │
│   │     │       │       │       │       │       │                       │   │
│   │     ├───────┼───────┼───────┼───────┼───────┤                       │   │
│   │     │                                                               │   │
│   │     │  ┌─────────────────────┐                                      │   │
│   │     │  │ Reservation A       │                                      │   │
│   │     │  │ Party: 8 guests     │                                      │   │
│   │     │  │ 12:00 - 14:00       │                                      │   │
│   │     │  └─────────────────────┘                                      │   │
│   │     │          ┌─────────────────────┐                              │   │
│   │     │          │ Reservation B       │                              │   │
│   │     │          │ Party: 6 guests     │                              │   │
│   │     │          │ 13:00 - 15:00       │                              │   │
│   │     │          └─────────────────────┘                              │   │
│   │     │                  ┌─────────────────────┐                      │   │
│   │     │                  │ Reservation C       │                      │   │
│   │     │                  │ Party: 5 guests     │                      │   │
│   │     │                  │ 14:00 - 16:00       │                      │   │
│   │     │                  └─────────────────────┘                      │   │
│   │                                                                     │   │
│   │   OCCUPANCY TIMELINE:                                               │   │
│   │                                                                     │   │
│   │   12:00-13:00:  A(8)                    = 8  guests  ✓              │   │
│   │   13:00-14:00:  A(8) + B(6)             = 14 guests  ✓              │   │
│   │   14:00-15:00:  B(6) + C(5)             = 11 guests  ✓              │   │
│   │   15:00-16:00:  C(5)                    = 5  guests  ✓              │   │
│   │                                                                     │   │
│   │   NEW REQUEST: Party of 8, 13:00-15:00                              │   │
│   │   13:00-14:00:  A(8) + B(6) + NEW(8)    = 22 guests  ❌ REJECTED    │   │
│   │                                         (exceeds max 20)            │   │
│   │                                                                     │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Overlap Detection Query**:

```
Two reservations overlap if:
  newStart < existingEnd AND newEnd > existingStart

MongoDB Query:
{
  'restaurantId': restaurantId,
  'spaceId': spaceId,
  'startTime': { $lt: newEndTime },
  'endTime': { $gt: newStartTime }
}
```

**Pros**:
- ✅ Maximizes space utilization for shared dining experiences
- ✅ Enables event-style bookings with multiple parties
- ✅ Separate min/max capacity validation prevents unsuitable party sizes
- ✅ Efficient database query using indexed time range

**Cons**:
- ⚠️ Peak occupancy calculation requires querying all overlapping reservations
- ⚠️ No consideration for physical table layouts or seating arrangements
- ⚠️ Concurrent modifications could cause race conditions (no distributed locking)
- ⚠️ Does not prevent "fragmentation" where available capacity is split across non-contiguous times

---

### Occupancy Analytics Reporting

**Description**: An analytical API that accepts a specific date/time range and returns a detailed breakdown of occupancy levels throughout that period, with hourly granularity and utilization metrics.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    OCCUPANCY ANALYTICS REPORT                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   REQUEST: GET /v1/restaurants/{id}/analytics/occupancy                     │
│           ?startTime=2026-01-20T09:00:00                                    │
│           &endTime=2026-01-20T18:00:00                                      │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │ RESPONSE STRUCTURE                                                  │   │
│   │                                                                     │   │
│   │   OccupancyReportResponse                                           │   │
│   │   ├── restaurantId                                                  │   │
│   │   ├── reportStartTime                                               │   │
│   │   ├── reportEndTime                                                 │   │
│   │   ├── summary: OccupancySummary                                     │   │
│   │   │   ├── totalReservations                                         │   │
│   │   │   ├── totalGuests                                               │   │
│   │   │   ├── peakOccupancy                                             │   │
│   │   │   ├── averageUtilization                                        │   │
│   │   │   └── overallUtilizationPercentage                              │   │
│   │   ├── spaceReports: List<SpaceOccupancyReport>  (paginated)         │   │
│   │   │   └── SpaceOccupancyReport                                      │   │
│   │   │       ├── spaceId                                               │   │
│   │   │       ├── spaceName                                             │   │
│   │   │       ├── maxCapacity                                           │   │
│   │   │       ├── totalReservations                                     │   │
│   │   │       ├── peakOccupancy                                         │   │
│   │   │       ├── averageUtilization                                    │   │
│   │   │       └── hourlyBreakdown: List<TimeSlotOccupancy>              │   │
│   │   │           └── TimeSlotOccupancy                                 │   │
│   │   │               ├── slotStart                                     │   │
│   │   │               ├── slotEnd                                       │   │
│   │   │               ├── reservationCount                              │   │
│   │   │               ├── occupancy                                     │   │
│   │   │               ├── maxCapacity                                   │   │
│   │   │               └── utilizationPercentage                         │   │
│   │   ├── page, size, totalElements, totalPages                         │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │ HOURLY BREAKDOWN VISUALIZATION                                      │   │
│   │                                                                     │   │
│   │   Space: "Garden Room" (Max Capacity: 20)                           │   │
│   │                                                                     │   │
│   │   100% ┤                                                            │   │
│   │    90% ┤              ████                                          │   │
│   │    80% ┤              ████                                          │   │
│   │    70% ┤         ████ ████ ████                                     │   │
│   │    60% ┤         ████ ████ ████                                     │   │
│   │    50% ┤    ████ ████ ████ ████ ████                                │   │
│   │    40% ┤    ████ ████ ████ ████ ████                                │   │
│   │    30% ┤    ████ ████ ████ ████ ████ ████                           │   │
│   │    20% ┤    ████ ████ ████ ████ ████ ████ ████                      │   │
│   │    10% ┤    ████ ████ ████ ████ ████ ████ ████ ████                 │   │
│   │     0% ┼────┬────┬────┬────┬────┬────┬────┬────┬────                │   │
│   │        09   10   11   12   13   14   15   16   17   (Hour)          │   │
│   │                                                                     │   │
│   │   Peak: 12:00-13:00 (18 guests, 90%)                                │   │
│   │   Average Utilization: 45.5%                                        │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Key Features**:

| Feature | Description |
|---------|-------------|
| Date Range Validation | Maximum 31 days, end must be after start |
| Space Filtering | Optional `spaceId` parameter for single-space reports |
| Pagination | Paginated space reports for restaurants with many spaces |
| Metrics | Reservation count, guest count, utilization percentage |

**Pros**:
- ✅ Provides actionable business intelligence for capacity planning
- ✅ Hourly granularity enables identification of peak/off-peak times
- ✅ Pagination prevents memory issues with large datasets
- ✅ Configurable maximum date range prevents expensive queries

**Cons**:
- ⚠️ Hourly breakdown can produce large response payloads
- ⚠️ No caching layer - each request recalculates from raw data
- ⚠️ Fixed hourly granularity may not suit all analysis needs
- ⚠️ No historical trending or comparison features

---

## API Reference

### Restaurant API

Base URL: `/v1/restaurants`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | List all restaurants |
| `GET` | `/{id}` | Get restaurant by ID |
| `POST` | `/` | Create new restaurant |
| `PUT` | `/{id}` | Update restaurant |
| `DELETE` | `/{id}` | Delete restaurant |
| `POST` | `/{id}/spaces` | Add space to restaurant |
| `DELETE` | `/{id}/spaces/{spaceId}` | Remove space from restaurant |

**Example: Create Restaurant with Space**

```bash
# Create restaurant
curl -X POST http://localhost:8081/v1/restaurants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "The Grand Dining",
    "address": "123 Main St",
    "cuisineType": "Fine Dining",
    "capacity": 100
  }'

# Add a space
curl -X POST http://localhost:8081/v1/restaurants/{restaurantId}/spaces \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Private Garden Room",
    "minCapacity": 4,
    "maxCapacity": 20,
    "operatingStartTime": "10:00",
    "operatingEndTime": "23:00",
    "timeSlotDurationMinutes": 60
  }'
```

### Reservation API

Base URL: `/v1/reservations`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | List all reservations |
| `GET` | `/{id}` | Get reservation by ID |
| `POST` | `/` | Create new reservation |
| `DELETE` | `/{id}` | Delete reservation |

**Example: Create Reservation**

```bash
curl -X POST http://localhost:8081/v1/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "507f1f77bcf86cd799439011",
    "spaceId": "123e4567-e89b-12d3-a456-426614174000",
    "customerEmail": "guest@example.com",
    "startTime": "20-01-2026 19:00",
    "endTime": "20-01-2026 21:30",
    "partySize": 6,
    "status": "CONFIRMED"
  }'
```

**Note**: Date/time format for reservations is `dd-MM-yyyy HH:mm`

### Analytics API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/v1/restaurants/{id}/analytics/occupancy` | Generate occupancy report |

**Parameters**:

| Parameter | Required | Description | Example |
|-----------|----------|-------------|---------|
| `startTime` | Yes | Report start (ISO format) | `2026-01-20T09:00:00` |
| `endTime` | Yes | Report end (ISO format) | `2026-01-20T18:00:00` |
| `spaceId` | No | Filter to specific space | UUID |
| `page` | No | Page number (0-based) | `0` |
| `size` | No | Page size | `10` |

**Example: Get Occupancy Report**

```bash
curl "http://localhost:8081/v1/restaurants/{restaurantId}/analytics/occupancy?\
startTime=2026-01-20T09:00:00&\
endTime=2026-01-20T18:00:00&\
page=0&\
size=10"
```

---

## Technical Design

### Data Model

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DATA MODEL                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────────────────┐         ┌─────────────────────────────────┐   │
│   │      Restaurant         │         │         Reservation             │   │
│   ├─────────────────────────┤         ├─────────────────────────────────┤   │
│   │ _id: ObjectId           │◄────────│ restaurantId: ObjectId          │   │
│   │ name: String            │         │ _id: ObjectId                   │   │
│   │ address: String         │         │ spaceId: UUID                   │   │
│   │ cuisineType: String     │         │ customerEmail: String           │   │
│   │ capacity: Integer       │         │ startTime: LocalDateTime        │   │
│   │ spaces: List<Space>     │         │ endTime: LocalDateTime          │   │
│   └──────────┬──────────────┘         │ partySize: Integer              │   │
│              │                        │ status: String                  │   │
│              │ embedded               └─────────────────────────────────┘   │
│              ▼                                                              │
│   ┌─────────────────────────┐                                               │
│   │        Space            │                                               │
│   ├─────────────────────────┤                                               │
│   │ id: UUID                │                                               │
│   │ name: String            │                                               │
│   │ minCapacity: Integer    │                                               │
│   │ maxCapacity: Integer    │                                               │
│   │ operatingStartTime      │  ← Nullable (uses default if not set)         │
│   │ operatingEndTime        │  ← Nullable (uses default if not set)         │
│   │ timeSlotDurationMinutes │  ← Nullable (uses default if not set)         │
│   └─────────────────────────┘                                               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Reservation Validation Pipeline

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    RESERVATION VALIDATION PIPELINE                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   Incoming Reservation Request                                              │
│              │                                                              │
│              ▼                                                              │
│   ┌─────────────────────┐                                                   │
│   │ 1. Restaurant       │──────► RestaurantNotFoundException                │
│   │    Exists?          │                                                   │
│   └─────────┬───────────┘                                                   │
│             ▼                                                               │
│   ┌─────────────────────┐                                                   │
│   │ 2. Space Exists in  │──────► SpaceNotFoundException                     │
│   │    Restaurant?      │                                                   │
│   └─────────┬───────────┘                                                   │
│             ▼                                                               │
│   ┌─────────────────────┐                                                   │
│   │ 3. Same Day         │──────► MultiDayReservationException               │
│   │    Reservation?     │                                                   │
│   └─────────┬───────────┘                                                   │
│             ▼                                                               │
│   ┌─────────────────────┐                                                   │
│   │ 4. Align Times to   │                                                   │
│   │    Slot Boundaries  │                                                   │
│   └─────────┬───────────┘                                                   │
│             ▼                                                               │
│   ┌─────────────────────┐                                                   │
│   │ 5. Duration >= One  │──────► InvalidReservationDurationException        │
│   │    Time Slot?       │                                                   │
│   └─────────┬───────────┘                                                   │
│             ▼                                                               │
│   ┌─────────────────────┐                                                   │
│   │ 6. Within Operating │──────► OutsideOperatingHoursException             │
│   │    Hours?           │                                                   │
│   └─────────┬───────────┘                                                   │
│             ▼                                                               │
│   ┌─────────────────────┐                                                   │
│   │ 7. Party Size in    │──────► InvalidPartySizeException                  │
│   │    [min, max]?      │                                                   │
│   └─────────┬───────────┘                                                   │
│             ▼                                                               │
│   ┌─────────────────────┐                                                   │
│   │ 8. Combined         │──────► CapacityExceededException                  │
│   │    Capacity OK?     │                                                   │
│   └─────────┬───────────┘                                                   │
│             ▼                                                               │
│   ┌─────────────────────┐                                                   │
│   │ 9. Save to MongoDB  │                                                   │
│   └─────────┬───────────┘                                                   │
│             ▼                                                               │
│   ✅ Return Created Reservation                                             │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Caching

The system implements a caching layer for occupancy analytics reports using **Caffeine**, a high-performance in-memory cache for Java.

**Cache Configuration**:

| Setting | Default | Description |
|---------|---------|-------------|
| `cache-ttl-minutes` | 10 | Time-to-live for cached reports |
| `cache-max-size` | 100 | Maximum number of cached entries |

**How It Works**:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         CACHING FLOW                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   Analytics Request                                                         │
│          │                                                                  │
│          ▼                                                                  │
│   ┌─────────────────┐     Cache Hit     ┌─────────────────────────────┐     │
│   │  Check Cache    │──────────────────►│  Return Cached Response     │     │
│   │  (Caffeine)     │                   └─────────────────────────────┘     │
│   └────────┬────────┘                                                       │
│            │ Cache Miss                                                     │
│            ▼                                                                │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │  Generate Report from Database                                      │   │
│   │  • Fetch reservations                                               │   │
│   │  • Calculate hourly breakdown                                       │   │
│   │  • Compute utilization metrics                                      │   │
│   └────────────────────────────┬────────────────────────────────────────┘   │
│                                │                                            │
│                                ▼                                            │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │  Store in Cache & Return                                            │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│   ═══════════════════════════════════════════════════════════════════════   │
│                                                                             │
│   CACHE INVALIDATION                                                        │
│                                                                             │
│   ┌─────────────────┐          ┌─────────────────────────────────────────┐  │
│   │ Create/Delete   │─────────►│  Evict ALL cached analytics reports    │  │
│   │ Reservation     │          │  (ensures data consistency)            │  │
│   └─────────────────┘          └─────────────────────────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Cache Key Structure**:

The cache key is a composite of all query parameters:
- `restaurantId`
- `startTime`
- `endTime`
- `spaceId` (optional)
- `page`
- `size`

**Eviction Strategy**:

When a reservation is created or deleted, **all** cached analytics reports are evicted (`allEntries=true`). This simple approach ensures data consistency while accepting a temporary performance cost after data modifications.

**Configuration** (`application.yml`):

```yaml
private-dining:
  analytics:
    cache-ttl-minutes: 10      # Cache entries expire after 10 minutes
    cache-max-size: 100        # Maximum 100 cached reports
```

> **Note for Multi-Instance Deployments**: The current implementation uses Caffeine, which is a local in-memory cache. For production deployments with multiple application instances, consider replacing Caffeine with a distributed cache such as **Redis** to ensure cache consistency across all nodes. This would require adding `spring-boot-starter-data-redis` and updating the `CacheConfig` to use `RedisCacheManager`.

---

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+**

No external database required - the application uses an embedded MongoDB instance.

---

## Running the Project

### Start the Application

```bash
# Clone and navigate to the project
cd private-dining

# Run with Maven
mvn spring-boot:run
```

The application starts on **http://localhost:8081**

### Access Swagger UI

Once running, access the interactive API documentation at:

**http://localhost:8081/swagger-ui.html**

---

## Testing & Coverage

### Run Tests

```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn verify
```

### JaCoCo Coverage Reports

After running `mvn verify`, coverage reports are generated at:

```
target/site/jacoco/index.html
```

Open this file in a browser to view detailed coverage metrics.

### Coverage Configuration

The project enforces **80% minimum instruction coverage** via JaCoCo Maven plugin:

```xml
<configuration>
  <rules>
    <rule>
      <element>BUNDLE</element>
      <limits>
        <limit>
          <counter>INSTRUCTION</counter>
          <value>COVEREDRATIO</value>
          <minimum>0.80</minimum>
        </limit>
      </limits>
    </rule>
  </rules>
</configuration>
```

**Excluded from coverage**:
- Model classes (`**/model/**`)
- DTO classes (`**/dto/**`)
- Configuration classes (`**/config/**`)
- Exception classes (`**/exception/*Exception.class`)
- One-time scripts (`**/onetime/**`)
- Application entry point (`**/*Application.class`)

### Generate Coverage Report

```bash
# Full build with tests and coverage
mvn clean verify

# Open coverage report (Windows)
start target/site/jacoco/index.html

# Open coverage report (Mac/Linux)
open target/site/jacoco/index.html
```

### Current Test Coverage

The project maintains high test coverage across all core components:

| Package | Coverage Focus |
|---------|---------------|
| `controller` | API endpoints, request/response mapping |
| `service` | Business logic, validation rules |
| `mapper` | Entity/DTO conversions |
| `validation` | Custom validators |
| `exception` | Global exception handling |

---

## Configuration Reference

### Application Properties (`application.yml`)

```yaml
# Private Dining Space Defaults
private-dining:
  space:
    defaults:
      operating-start-time: "09:00"      # Default opening time
      operating-end-time: "22:00"        # Default closing time
      time-slot-duration-minutes: 60     # Default slot duration
  analytics:
    time-slot-duration-minutes: 60       # Analytics report granularity
    max-range-days: 31                   # Max days for analytics query
    cache-ttl-minutes: 10                # Cache TTL for analytics reports
    cache-max-size: 100                  # Max cached analytics entries

# Server Configuration
server:
  port: 8081
```

---

## Future Improvements

1. **Multi-window operating hours** - Support for lunch/dinner breaks
2. **Day-of-week variations** - Different hours per day
3. **Table layout awareness** - Physical seating arrangements
4. **Distributed caching** - Redis for multi-instance deployments
5. **Distributed locking** - Prevent race conditions on concurrent bookings
6. **Event sourcing** - Full audit trail of reservation changes
7. **Notification system** - Email/SMS confirmations and reminders

## Note
GitHub Copilot was used to assist in the development of this project.

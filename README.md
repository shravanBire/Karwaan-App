# Karwaan-App 🚗

A group navigation and convoy travel app built with **Kotlin** and **Jetpack Compose**. Karwaan enables coordinated travel between multiple users with real-time location tracking and trip management.

## Features

- 📍 **Real-time Location Tracking** - Track all members of your convoy in real-time
- 🚀 **Group Navigation** - Organize and manage group trips effortlessly
- 🎨 **Member Markers** - Each member has a unique colored marker on the map
- 💬 **Trip Management** - Create, join, and manage trips with unique trip codes
- 📱 **Modern UI** - Built with Jetpack Compose for a smooth and responsive experience
- 🔄 **Real-time Updates** - Powered by Supabase for instant synchronization across all members

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Backend**: Supabase (PostgreSQL)
- **Real-time**: PostgreSQL Real-time Subscriptions
- **Build Tool**: Gradle

## Architecture

### Database Schema

**Trips Table**
- id (UUID, Primary Key)
- trip_code (Unique Integer) - Join code for the trip
- host_id (UUID) - Creator of the trip
- is_active (Boolean) - Trip status
- created_at (Timestamp) - Creation time
- ended_at (Timestamp) - Trip end time

**Members Table**
- id (UUID, Primary Key)
- trip_id (UUID) - Reference to trips table
- user_id (UUID) - User identifier
- display_name (VARCHAR) - Member's display name
- marker_color (VARCHAR) - Hex color for map marker
- latitude/longitude (Double) - Current location
- last_updated (Timestamp) - Last location update
- is_active (Boolean) - Member status

## Getting Started

### Prerequisites

- Android Studio (Arctic Fox or newer)
- Android SDK 21 or higher
- Kotlin 1.8+

### Installation

1. Clone the repository:
   git clone https://github.com/shravanBire/Karwaan-App.git
   cd Karwaan-App

2. Open in Android Studio:
   Android Studio will automatically detect the Gradle configuration

3. Build the project:
   ./gradlew build

4. Run the app:
   ./gradlew installDebug

### Environment Setup

Configure your Supabase credentials:
1. Create a project at https://supabase.com
2. Run the provided SQL schema in your Supabase SQL editor
3. Add your Supabase URL and API key to your app's configuration

## Usage

### Creating a Trip

1. Launch the app
2. Tap "Create Trip" to generate a new convoy
3. Share the unique trip code with friends

### Joining a Trip

1. Tap "Join Trip"
2. Enter the trip code shared by the host
3. Enter your display name and select a marker color
4. Start sharing your location with the group

### Real-time Tracking

- Map displays all active members with their selected marker colors
- Member positions update in real-time as they move
- View member details by tapping their marker

## Project Structure

Karwaan-App/
├── app/                 # Main application module
├── build.gradle.kts     # Root build configuration
├── gradle/              # Gradle wrapper files
├── gradle.properties    # Gradle properties
└── settings.gradle.kts  # Gradle settings

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (git checkout -b feature/amazing-feature)
3. Commit your changes (git commit -m 'Add amazing feature')
4. Push to the branch (git push origin feature/amazing-feature)
5. Open a Pull Request

Made with ❤️ by Shravan Bire (https://github.com/shravanBire) & Prince Jain (https://github.com/realprincejn)

##SQL SCHEMA SETUP

-- Create pgcrypto extension for UUID generation
create extension if not exists "pgcrypto";

-- Create trips table
-- Stores information about group trips/convoys
create table public.trips (
  id uuid default gen_random_uuid() primary key,
  trip_code int not null unique,
  host_id uuid not null,
  is_active boolean default true,
  created_at timestamptz default now(),
  ended_at timestamptz
);

-- Create members table
-- Stores information about trip participants and their locations
create table public.members (
  id uuid default gen_random_uuid() primary key,
  trip_id uuid references public.trips(id) on delete cascade,
  user_id uuid not null,
  display_name varchar(10) not null,
  marker_color varchar(7) not null,
  latitude double precision,
  longitude double precision,
  last_updated timestamptz default now(),
  is_active boolean default true,
  unique(trip_id, user_id)
);

-- Enable real-time subscriptions for both tables
alter publication supabase_realtime add table public.members;
alter publication supabase_realtime add table public.trips;

-- Create public access policies for trips table
create policy "Public access" on public.trips
for all using (true);

-- Create public access policies for members table
create policy "Public access" on public.members
for all using (true);

-- Enable Row Level Security on both tables
alter table public.trips enable row level security;
alter table public.members enable row level security;

##DATABASE SCHEMA DETAILS
TABLE: trips
================================================================================
Column Name    | Type       | Constraints/Default
===============|============|==================================================
id             | uuid       | PRIMARY KEY, DEFAULT gen_random_uuid()
trip_code      | int        | NOT NULL, UNIQUE
host_id        | uuid       | NOT NULL (User who created the trip)
is_active      | boolean    | DEFAULT true
created_at     | timestamptz| DEFAULT now()
ended_at       | timestamptz| NULL (Optional, set when trip ends)

DESCRIPTION:
The trips table stores information about each convoy/group trip. Each trip has
a unique trip_code that members use to join. The host_id identifies who created
the trip. The is_active flag indicates whether the trip is ongoing. Timestamps
track when the trip was created and ended.

TABLE: members
================================================================================
Column Name    | Type               | Constraints/Default
===============|====================|==================================================
id             | uuid               | PRIMARY KEY, DEFAULT gen_random_uuid()
trip_id        | uuid               | FOREIGN KEY -> trips(id) ON DELETE CASCADE
user_id        | uuid               | NOT NULL (Reference to user)
display_name   | varchar(10)        | NOT NULL (Max 10 characters)
marker_color   | varchar(7)         | NOT NULL (Hex color code, e.g., #FF5733)
latitude       | double precision   | NULL (Current latitude)
longitude      | double precision   | NULL (Current longitude)
last_updated   | timestamptz        | DEFAULT now()
is_active      | boolean            | DEFAULT true
UNIQUE         | (trip_id, user_id) | Ensures one user per trip

DESCRIPTION:
The members table stores information about users participating in trips. It
tracks their current location (latitude/longitude), display name shown on maps,
and a unique marker color. The last_updated timestamp records when their
location was last refreshed. The composite unique constraint ensures a user
can only be in a trip once.

RELATIONSHIPS:
- members.trip_id → trips.id (CASCADE DELETE)
  When a trip is deleted, all associated members are automatically deleted.

REAL-TIME FEATURES:
Both tables are added to the Supabase real-time publication, enabling:
- Live location updates as members move
- Instant trip status changes
- Automatic synchronization across all clients

ROW LEVEL SECURITY (RLS):
Both tables have RLS enabled with a simple "Public access" policy:
- All users can SELECT, INSERT, UPDATE, DELETE on both tables
- This is suitable for a public trip-sharing application
- Consider implementing more restrictive policies in production

================================================================================
                         SUPABASE INTEGRATION NOTES
================================================================================

1. REAL-TIME SUBSCRIPTIONS:
   - Members can subscribe to real-time updates on the members table
   - Updates to latitude/longitude trigger instant notifications to other users
   - Trip status changes are instantly reflected across the app

2. CASCADE DELETE:
   - When a trip is deleted, all associated members are automatically deleted
   - This maintains referential integrity and keeps the database clean

3. ROW LEVEL SECURITY:
   - Current policy allows public access
   - For production, implement user-based policies:
     * Users can only modify their own member records
     * Users can only read trips they're part of
     * Hosts can delete trips they created

4. RECOMMENDED PRODUCTION POLICIES:
   - Implement user_id validation
   - Add trip access control
   - Restrict location updates to prevent spoofing
   - Add audit logging for security-sensitive operations

================================================================================
                            PROJECT STRUCTURE
================================================================================

Karwaan-App/
│
├── .idea/                      # Android Studio configuration
├── .gitignore                  # Git ignore rules
├── app/                        # Main application module
│   ├── src/                    # Source code
│   │   ├── main/               # Main source set
│   │   │   ├── kotlin/         # Kotlin source files
│   │   │   ├── res/            # Android resources (layouts, drawables, etc.)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/               # Unit tests
│   │   └── androidTest/        # Instrumented tests
│   ├── build.gradle.kts        # App-level build configuration
│   └── proguard-rules.pro      # ProGuard/R8 rules
│
├── gradle/                     # Gradle wrapper distribution
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── build.gradle.kts            # Root build configuration
├── gradle.properties           # Gradle properties and versions
├── gradle.properties.kts       # Kotlin Gradle properties (if applicable)
├── settings.gradle.kts         # Gradle settings
├── gradlew                     # Gradle wrapper script (macOS/Linux)
├── gradlew.bat                 # Gradle wrapper script (Windows)
│
└── README.md                   # Project documentation

================================================================================
                          BUILD CONFIGURATION FILES
================================================================================

FILE: build.gradle.kts (Root)
Description: Root-level Gradle configuration for the entire project.
Contains plugin declarations, shared dependencies, and build settings.

FILE: app/build.gradle.kts
Description: Module-level Gradle configuration for the app.
Defines:
- compileSdk and targetSdk versions
- App-specific dependencies (Jetpack Compose, Kotlin, etc.)
- Signing configurations
- Build variants (debug/release)

FILE: gradle.properties
Description: Gradle system properties and version declarations.
Contains:
- org.gradle.jvmargs (JVM arguments)
- org.gradle.configureondemand (build optimization)
- Dependency versions
- Build tool versions

FILE: settings.gradle.kts
Description: Gradle settings file that defines project structure.
Includes module paths and plugin management.

FILE: gradlew and gradlew.bat
Description: Gradle wrapper scripts for Linux/macOS and Windows.
Automatically downloads and runs the correct Gradle version.

================================================================================
                            GETTING STARTED GUIDE
================================================================================

STEP 1: CLONE THE REPOSITORY
$ git clone https://github.com/shravanBire/Karwaan-App.git
$ cd Karwaan-App

STEP 2: OPEN IN ANDROID STUDIO
- Open Android Studio
- File > Open
- Select the Karwaan-App folder
- Wait for Gradle sync to complete

STEP 3: SETUP SUPABASE
1. Go to https://supabase.com and create a new project
2. Copy your Supabase URL and API key
3. In the Supabase SQL editor, run the SQL schema provided above
4. Add your credentials to your app's configuration

STEP 4: BUILD THE PROJECT
$ ./gradlew build

STEP 5: RUN ON EMULATOR OR DEVICE
$ ./gradlew installDebug

Or from Android Studio:
- Connect an Android device or start an emulator
- Click "Run" button (Shift + F10)

STEP 6: TEST THE APP
1. Create a trip on one device
2. Share the trip code with another device
3. Join the trip with the second device
4. See real-time location updates

================================================================================
                          FEATURE IMPLEMENTATION NOTES
================================================================================

REAL-TIME LOCATION TRACKING:
- Uses Supabase real-time subscriptions on the members table
- Location updates trigger instant changes on all connected clients
- Latitude and longitude are updated in the members table

TRIP MANAGEMENT:
- Trips are created with a unique trip_code for easy sharing
- Trip hosts are stored in host_id for future permission management
- is_active flag allows trips to be marked as completed without deletion

MEMBER MARKERS:
- Each member selects a marker_color (hex code like #FF5733)
- Display names are limited to 10 characters for UI consistency
- Colors are used to visually distinguish members on the map

CONVOY COORDINATION:
- Members can see all other active members in the same trip
- Real-time updates ensure everyone sees the current positions
- last_updated timestamp helps identify stale location data

================================================================================
                            SECURITY CONSIDERATIONS
================================================================================

CURRENT STATE:
- Public access policies enabled for easy testing
- No user authentication restrictions

PRODUCTION RECOMMENDATIONS:
1. Implement authentication with Supabase Auth
2. Update RLS policies to restrict access:
   - Users can only update their own member records
   - Users can only view trips they belong to
   - Hosts can manage trip settings
3. Add rate limiting for location updates
4. Implement input validation:
   - Validate trip_code format
   - Validate display_name and marker_color
   - Validate latitude/longitude ranges
5. Add audit logging for sensitive operations
6. Consider implementing trip expiration
7. Add reporting mechanism for inappropriate markers/names

================================================================================
                        FUTURE ENHANCEMENT IDEAS
================================================================================

1. ADVANCED NAVIGATION:
   - Route planning for the entire convoy
   - Stop suggestions along the route
   - Estimated arrival time calculations

2. COMMUNICATION:
   - In-app chat for group members
   - Voice/video call integration
   - Emergency alerts and messaging

3. TRIP ANALYTICS:
   - Distance traveled tracking
   - Average speed monitoring
   - Trip statistics and reports

4. SOCIAL FEATURES:
   - Trip history and past convoys
   - User profiles and ratings
   - Create recurring trips

5. SAFETY FEATURES:
   - SOS button for emergencies
   - Geofencing for trip boundaries
   - Driver behavior monitoring

6. PERFORMANCE:
   - Battery optimization for continuous tracking
   - Data usage optimization
   - Offline mode with sync when online

================================================================================
                              TROUBLESHOOTING
================================================================================

BUILD ISSUES:
- Gradle sync fails: 
  * File > Invalidate Caches > Restart
  * Delete .gradle and .idea folders
  * Re-open the project

- Missing dependencies:
  * Check internet connection
  * Verify gradle.properties versions
  * Run: ./gradlew --refresh-dependencies

RUNTIME ISSUES:
- App crashes on launch:
  * Check Supabase credentials
  * Ensure database schema is set up
  * Check AndroidManifest.xml permissions

- Real-time updates not working:
  * Verify Supabase connection
  * Check internet connectivity
  * Ensure real-time publication is enabled in Supabase

LOCATION TRACKING:
- Permissions not granted:
  * Check Android permissions in AndroidManifest.xml
  * Request runtime permissions for location
  * Verify device location services are enabled

================================================================================
                            CONTACT & SUPPORT
================================================================================

Project Owner: shravanBire
GitHub Profiles: https://github.com/shravanBire,https://github.com/realprincejn
Repository: https://github.com/shravanBire/Karwaan-App

For Issues or Feature Requests:
- Open an issue on GitHub: https://github.com/shravanBire/Karwaan-App/issues
- Include details about your problem or feature request
- Provide steps to reproduce for bugs

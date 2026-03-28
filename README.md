# Karwaan-App 🚗

A group navigation and convoy travel app built with **Kotlin** and **Jetpack Compose**. Karwaan enables coordinated travel between multiple users with real-time location tracking and trip management.

---

## Features

- 📍 **Real-time Location Tracking** — Track all members of your convoy in real-time
- 🚀 **Group Navigation** — Organize and manage group trips effortlessly
- 🎨 **Member Markers** — Each member has a unique colored marker on the map
- 💬 **Trip Management** — Create, join, and manage trips with unique trip codes
- 📱 **Modern UI** — Built with Jetpack Compose for a smooth and responsive experience
- 🔄 **Real-time Updates** — Powered by Supabase for instant synchronization across all members

---

## Tech Stack

| Layer       | Technology                        |
|-------------|-----------------------------------|
| Language    | Kotlin                            |
| UI          | Jetpack Compose                   |
| Backend     | Supabase (PostgreSQL)             |
| Real-time   | PostgreSQL Real-time Subscriptions|
| Build Tool  | Gradle                            |

---

## Architecture

### Database Schema

#### Trips Table

| Column       | Type        | Description                        |
|--------------|-------------|------------------------------------|
| `id`         | UUID (PK)   | Auto-generated primary key         |
| `trip_code`  | Integer     | Unique join code for the trip      |
| `host_id`    | UUID        | Creator of the trip                |
| `is_active`  | Boolean     | Trip status (default: `true`)      |
| `created_at` | Timestamp   | Creation time                      |
| `ended_at`   | Timestamp   | Trip end time (nullable)           |

#### Members Table

| Column         | Type          | Description                              |
|----------------|---------------|------------------------------------------|
| `id`           | UUID (PK)     | Auto-generated primary key               |
| `trip_id`      | UUID (FK)     | Reference to trips table                 |
| `user_id`      | UUID          | User identifier                          |
| `display_name` | VARCHAR(10)   | Member's display name (max 10 chars)     |
| `marker_color` | VARCHAR(7)    | Hex color for map marker (e.g. #FF5733)  |
| `latitude`     | Double        | Current latitude                         |
| `longitude`    | Double        | Current longitude                        |
| `last_updated` | Timestamp     | Last location update                     |
| `is_active`    | Boolean       | Member status (default: `true`)          |

> **Constraint:** `UNIQUE(trip_id, user_id)` — ensures one user per trip.

---

## Getting Started

### Prerequisites

- Android Studio (Arctic Fox or newer)
- Android SDK 21 or higher
- Kotlin 1.8+

### Installation

**1. Clone the repository:**
```bash
git clone https://github.com/shravanBire/Karwaan-App.git
cd Karwaan-App
```

**2. Open in Android Studio:**

Android Studio will automatically detect the Gradle configuration.

**3. Build the project:**
```bash
./gradlew build
```

**4. Run the app:**
```bash
./gradlew installDebug
```

### Environment Setup

1. Create a project at [supabase.com](https://supabase.com)
2. Run the SQL schema (see below) in your Supabase SQL editor
3. Add your Supabase URL and API key to your app's configuration

---

## Usage

### Creating a Trip

1. Launch the app
2. Tap **"Create Trip"** to generate a new convoy
3. Share the unique trip code with friends

### Joining a Trip

1. Tap **"Join Trip"**
2. Enter the trip code shared by the host
3. Enter your display name and select a marker color
4. Start sharing your location with the group

### Real-time Tracking

- Map displays all active members with their selected marker colors
- Member positions update in real-time as they move
- Tap a marker to view member details

---

## Project Structure

```
Karwaan-App/
├── .idea/                      # Android Studio configuration
├── .gitignore                  # Git ignore rules
├── app/                        # Main application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/         # Kotlin source files
│   │   │   ├── res/            # Android resources
│   │   │   └── AndroidManifest.xml
│   │   ├── test/               # Unit tests
│   │   └── androidTest/        # Instrumented tests
│   ├── build.gradle.kts        # App-level build config
│   └── proguard-rules.pro      # ProGuard/R8 rules
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle.kts            # Root build configuration
├── gradle.properties           # Gradle properties
├── settings.gradle.kts         # Gradle settings
├── gradlew                     # Gradle wrapper (macOS/Linux)
├── gradlew.bat                 # Gradle wrapper (Windows)
└── README.md                   # Project documentation
```

---

## SQL Schema Setup

```sql
-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Trips table
CREATE TABLE public.trips (
  id          UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
  trip_code   INT         NOT NULL UNIQUE,
  host_id     UUID        NOT NULL,
  is_active   BOOLEAN     DEFAULT true,
  created_at  TIMESTAMPTZ DEFAULT now(),
  ended_at    TIMESTAMPTZ
);

-- Members table
CREATE TABLE public.members (
  id            UUID        DEFAULT gen_random_uuid() PRIMARY KEY,
  trip_id       UUID        REFERENCES public.trips(id) ON DELETE CASCADE,
  user_id       UUID        NOT NULL,
  display_name  VARCHAR(10) NOT NULL,
  marker_color  VARCHAR(7)  NOT NULL,
  latitude      DOUBLE PRECISION,
  longitude     DOUBLE PRECISION,
  last_updated  TIMESTAMPTZ DEFAULT now(),
  is_active     BOOLEAN     DEFAULT true,
  UNIQUE(trip_id, user_id)
);

-- Enable real-time
ALTER PUBLICATION supabase_realtime ADD TABLE public.members;
ALTER PUBLICATION supabase_realtime ADD TABLE public.trips;

-- Row Level Security
ALTER TABLE public.trips   ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.members ENABLE ROW LEVEL SECURITY;

-- Public access policies (suitable for development)
CREATE POLICY "Public access" ON public.trips   FOR ALL USING (true);
CREATE POLICY "Public access" ON public.members FOR ALL USING (true);
```

---

## Supabase Integration Notes

### Real-time Subscriptions
- Members subscribe to live updates on the `members` table
- Location changes trigger instant notifications to other users
- Trip status changes sync across all clients immediately

### Cascade Delete
- Deleting a trip automatically removes all associated members
- Maintains referential integrity without manual cleanup

### Row Level Security (RLS)
- Current policy allows public access (suitable for testing)
- **For production**, implement user-based policies:
  - Users can only modify their own member records
  - Users can only read trips they belong to
  - Hosts can delete trips they created

---

## Security Considerations

| Area              | Current State         | Production Recommendation                   |
|-------------------|-----------------------|---------------------------------------------|
| Authentication    | None                  | Implement Supabase Auth                     |
| RLS Policies      | Public access         | Restrict by user_id and trip membership     |
| Location Updates  | Unrestricted          | Add rate limiting and range validation      |
| Input Validation  | Minimal               | Validate trip codes, names, coordinates     |
| Audit Logging     | None                  | Log sensitive operations                    |

---

## Future Enhancements

### 🗺️ Advanced Navigation
- Route planning for the entire convoy
- Stop suggestions along the route
- Estimated arrival time calculations

### 💬 Communication
- In-app group chat
- Voice/video call integration
- Emergency alerts and messaging

### 📊 Trip Analytics
- Distance traveled tracking
- Average speed monitoring
- Trip statistics and reports

### 👤 Social Features
- Trip history and past convoys
- User profiles and ratings
- Recurring trip support

### 🛡️ Safety Features
- SOS emergency button
- Geofencing for trip boundaries
- Driver behavior monitoring

### ⚡ Performance
- Battery optimization for continuous tracking
- Data usage reduction
- Offline mode with background sync

---

## Troubleshooting

### Build Issues

| Problem                  | Solution                                                               |
|--------------------------|------------------------------------------------------------------------|
| Gradle sync fails        | File → Invalidate Caches → Restart; delete `.gradle` and `.idea`      |
| Missing dependencies     | Check internet connection; run `./gradlew --refresh-dependencies`      |

### Runtime Issues

| Problem                       | Solution                                                          |
|-------------------------------|-------------------------------------------------------------------|
| App crashes on launch         | Verify Supabase credentials and database schema                   |
| Real-time updates not working | Check Supabase connection and real-time publication settings      |
| Location permissions denied   | Grant runtime permissions; ensure device location is enabled      |

---

## Contributing

Contributions are welcome! Follow these steps:

1. Fork the repository
2. Create a feature branch:
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. Commit your changes:
   ```bash
   git commit -m "Add amazing feature"
   ```
4. Push to the branch:
   ```bash
   git push origin feature/amazing-feature
   ```
5. Open a Pull Request

---

## Contact & Support

| | |
|---|---|
| **Project Owners** | [shravanBire](https://github.com/shravanBire) & [realprincejn](https://github.com/realprincejn) |
| **Repository** | [github.com/shravanBire/Karwaan-App](https://github.com/shravanBire/Karwaan-App) |
| **Issues** | [Open an issue](https://github.com/shravanBire/Karwaan-App/issues) |

---

*Made with ❤️ by Shravan Bire & Prince Jain*

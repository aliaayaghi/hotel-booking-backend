package com.HotelBook.HotelBooking;

import com.HotelBook.HotelBooking.Booking.*;
import com.HotelBook.HotelBooking.Cancellation.*;
import com.HotelBook.HotelBooking.Hotel.*;
import com.HotelBook.HotelBooking.HotelAccessibility.*;
import com.HotelBook.HotelBooking.HotelAmenity.*;
import com.HotelBook.HotelBooking.HotelAmenity.AmenityCategory;
import com.HotelBook.HotelBooking.HotelLocation.*;
import com.HotelBook.HotelBooking.HotelNearby.*;
import com.HotelBook.HotelBooking.HotelPhoto.*;
import com.HotelBook.HotelBooking.HotelPolicy.*;
import com.HotelBook.HotelBooking.Notification.*;
import com.HotelBook.HotelBooking.Payment.*;
import com.HotelBook.HotelBooking.Pricing.*;
import com.HotelBook.HotelBooking.Review.Entity.Review;
import com.HotelBook.HotelBooking.Review.repository.ReviewRepository;
import com.HotelBook.HotelBooking.Room.*;
import com.HotelBook.HotelBooking.RoomAccessibility.*;
import com.HotelBook.HotelBooking.RoomAmenity.*;
import com.HotelBook.HotelBooking.RoomAvailability.*;
import com.HotelBook.HotelBooking.RoomPhoto.*;
import com.HotelBook.HotelBooking.SavedHotel.*;
import com.HotelBook.HotelBooking.User.entity.Admin;
import com.HotelBook.HotelBooking.User.entity.Customer;
import com.HotelBook.HotelBooking.User.entity.HotelManager;
import com.HotelBook.HotelBooking.User.entity.User;
import com.HotelBook.HotelBooking.User.enums.UserRole;
import com.HotelBook.HotelBooking.User.repository.AdminRepository;
import com.HotelBook.HotelBooking.User.repository.CustomerRepository;
import com.HotelBook.HotelBooking.User.repository.HotelManagerRepository;
import com.HotelBook.HotelBooking.User.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DataSeeder — Full demo data for booking_glitchies
 *
 * All entity IDs are generated automatically by JPA (@GeneratedValue UUID).
 * No fixed UUIDs are set in this class — references between entities are
 * maintained through Java object references captured after each save().
 *
 * Admins   : Aliaa, Roz, Rama
 * Managers : 5 hotel managers
 * Customers: 15 customers
 * Hotels   : 9 hotels across 9 countries
 * Rooms    : 28 room types
 * Bookings : 40 bookings (18 COMPLETED, 4 CANCELLED, 18 CONFIRMED)
 * Reviews  : 18 reviews (for completed bookings only)
 * Payments : 40 payments
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    // ── Repositories ──────────────────────────────────────────────────────────
    private final UserRepository               userRepository;
    private final AdminRepository              adminRepository;
    private final CustomerRepository           customerRepository;
    private final HotelManagerRepository       hotelManagerRepository;
    private final HotelRepository              hotelRepository;
    private final LocationRepository           locationRepository;
    private final HotelAmenityRepository       hotelAmenityRepository;
    private final HotelAccessibilityRepository hotelAccessibilityRepository;
    private final HotelPhotoRepository         hotelPhotoRepository;
    private final NearbyPlaceRepository        nearbyPlaceRepository;
    private final BreakfastPolicyRepository    breakfastPolicyRepository;
    private final CheckInPolicyRepository      checkInPolicyRepository;
    private final PetPolicyRepository          petPolicyRepository;
    private final RoomRepository               roomRepository;
    private final RoomAmenityRepository        roomAmenityRepository;
    private final RoomAccessibilityRepository  roomAccessibilityRepository;
    private final RoomPhotoRepository          roomPhotoRepository;
    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final PricingRuleRepository        pricingRuleRepository;
    private final BookingRepository            bookingRepository;
    private final RoomAvailabilityRepository   roomAvailabilityRepository;
    private final PaymentRepository            paymentRepository;
    private final ReviewRepository             reviewRepository;
    private final SavedHotelRepository         savedHotelRepository;
    private final NotificationRepository       notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    // ── Entry point ───────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("DataSeeder: data already exists — skipping.");
            return;
        }
        // Clean up any orphaned room rows left by a previous failed run
        // (these would have hotel_id = null and cause the FK constraint migration to fail)
        entityManager.createNativeQuery("DELETE FROM room WHERE hotel_id IS NULL").executeUpdate();
        entityManager.flush();
        log.info("🌱 DataSeeder starting…");

        // Each seed method returns the saved entities so later steps can
        // reference them without hitting the database again.
        SeedContext ctx = new SeedContext();
        seedUsers(ctx);
        entityManager.flush();
        seedHotels(ctx);
        entityManager.flush();
        seedRooms(ctx);
        entityManager.flush();
        seedCancellationPolicies(ctx);
        seedPricingRules(ctx);
        seedBookings(ctx);
        seedRoomAvailability(ctx);
        seedPayments(ctx);
        seedReviews(ctx);
        seedSavedHotels(ctx);
        seedNotifications(ctx);

        log.info("✅ DataSeeder finished — booking_glitchies is ready.");
    }

    // =========================================================================
    // CONTEXT — holds all saved entities so steps can cross-reference them
    //           without extra DB queries and without any hard-coded UUIDs.
    // =========================================================================
    private static class SeedContext {
        // Users
        User uAdmin1, uAdmin2, uAdmin3;
        // Managers
        HotelManager mgr1, mgr2, mgr3, mgr4, mgr5;
        // Customers
        Customer c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15;
        // Hotels
        Hotel h1, h2, h3, h4, h5, h6, h7, h8, h9;
        // Rooms — Hotel 1
        Room r1Std, r1Dlx, r1Suite, r1Fam, r1Villa;
        // Rooms — Hotel 2
        Room r2Std, r2Dlx, r2Suite, r2Fam;
        // Rooms — Hotel 3
        Room r3Std, r3Dlx, r3Suite, r3Fam;
        // Rooms — Hotel 4
        Room r4Std, r4Dlx, r4Suite;
        // Rooms — Hotel 5
        Room r5Std, r5Dlx, r5Villa;
        // Rooms — Hotel 6
        Room r6Std, r6Dlx, r6Suite;
        // Rooms — Hotel 7
        Room r7Std, r7Dlx;
        // Rooms — Hotel 8
        Room r8Std, r8Dlx, r8Suite;
        // Rooms — Hotel 9
        Room r9Dorm, r9Pvt;
        // Cancellation policies
        CancellationPolicy cp1Flex, cp1Mod, cp1Strict;
        CancellationPolicy cp2Flex, cp2Mod;
        CancellationPolicy cp3Flex;
        CancellationPolicy cp4Mod;
        CancellationPolicy cp5Mod;
        CancellationPolicy cp6Flex;
        CancellationPolicy cp7Flex;
        CancellationPolicy cp8Mod;
        CancellationPolicy cp9Non;
        // Bookings
        Booking bk01, bk02, bk03, bk04, bk05, bk06, bk07, bk08, bk09, bk10;
        Booking bk11, bk12, bk13, bk14, bk15, bk16, bk17, bk18;
        Booking bk19, bk20, bk21, bk22;
        Booking bk23, bk24, bk25, bk26, bk27, bk28, bk29, bk30;
        Booking bk31, bk32, bk33, bk34, bk35, bk36, bk37, bk38, bk39, bk40;
    }

    // =========================================================================
    // 1. USERS  →  ADMINS / HOTEL_MANAGERS / CUSTOMERS
    // =========================================================================
    private void seedUsers(SeedContext ctx) {
        // ── Admins ────────────────────────────────────────────────────────────
        ctx.uAdmin1 = saveUser("Aliaa Hassan",   "aliaa@glitchies.com",   UserRole.ADMIN);
        ctx.uAdmin2 = saveUser("Roz Khalil",     "roz@glitchies.com",     UserRole.ADMIN);
        ctx.uAdmin3 = saveUser("Rama Nasser",    "rama@glitchies.com",    UserRole.ADMIN);

        adminRepository.save(Admin.builder().user(ctx.uAdmin1)
                .permissions("[\"MANAGE_USERS\",\"MANAGE_HOTELS\",\"VIEW_REPORTS\",\"MANAGE_BOOKINGS\",\"MANAGE_PAYMENTS\"]").build());
        adminRepository.save(Admin.builder().user(ctx.uAdmin2)
                .permissions("[\"MANAGE_HOTELS\",\"VIEW_REPORTS\",\"MANAGE_BOOKINGS\"]").build());
        adminRepository.save(Admin.builder().user(ctx.uAdmin3)
                .permissions("[\"MANAGE_USERS\",\"VIEW_REPORTS\",\"MANAGE_PAYMENTS\"]").build());

        // ── Hotel Managers ────────────────────────────────────────────────────
        User uMgr1 = saveUser("James Whitfield",  "james.w@grandazure.com",   UserRole.HOTEL_MANAGER);
        User uMgr2 = saveUser("Sofia Russo",      "sofia.r@bellavista.com",   UserRole.HOTEL_MANAGER);
        User uMgr3 = saveUser("Carlos Mendez",    "carlos.m@sunridge.com",    UserRole.HOTEL_MANAGER);
        User uMgr4 = saveUser("Fatima Al-Rashid", "fatima.r@desertoasis.com", UserRole.HOTEL_MANAGER);
        User uMgr5 = saveUser("Lena Bergström",   "lena.b@nordicinn.com",     UserRole.HOTEL_MANAGER);

        ctx.mgr1 = hotelManagerRepository.save(HotelManager.builder().user(uMgr1).phone("+44-7911-123456").build());
        ctx.mgr2 = hotelManagerRepository.save(HotelManager.builder().user(uMgr2).phone("+39-02-99887766").build());
        ctx.mgr3 = hotelManagerRepository.save(HotelManager.builder().user(uMgr3).phone("+1-305-555-0192").build());
        ctx.mgr4 = hotelManagerRepository.save(HotelManager.builder().user(uMgr4).phone("+971-50-123-4567").build());
        ctx.mgr5 = hotelManagerRepository.save(HotelManager.builder().user(uMgr5).phone("+46-8-555-0177").build());

        // ── Customers ─────────────────────────────────────────────────────────
        User uC1  = saveUser("Ahmed Mansour",  "ahmed.mansour@gmail.com",  UserRole.CUSTOMER);
        User uC2  = saveUser("Nour El-Din",    "nour.eldin@outlook.com",   UserRole.CUSTOMER);
        User uC3  = saveUser("Layla Ibrahim",  "layla.ibrahim@yahoo.com",  UserRole.CUSTOMER);
        User uC4  = saveUser("Omar Siddiqui",  "omar.siddiqui@gmail.com",  UserRole.CUSTOMER);
        User uC5  = saveUser("Yasmin Taha",    "yasmin.taha@hotmail.com",  UserRole.CUSTOMER);
        User uC6  = saveUser("Karim Farouk",   "karim.farouk@gmail.com",   UserRole.CUSTOMER);
        User uC7  = saveUser("Dina Mostafa",   "dina.mostafa@gmail.com",   UserRole.CUSTOMER);
        User uC8  = saveUser("Hassan Al-Amin", "hassan.alamin@yahoo.com",  UserRole.CUSTOMER);
        User uC9  = saveUser("Mariam Youssef", "mariam.youssef@gmail.com", UserRole.CUSTOMER);
        User uC10 = saveUser("Tariq Nabil",    "tariq.nabil@outlook.com",  UserRole.CUSTOMER);
        User uC11 = saveUser("Sara El-Sayed",  "sara.elsayed@gmail.com",   UserRole.CUSTOMER);
        User uC12 = saveUser("Bilal Haddad",   "bilal.haddad@hotmail.com", UserRole.CUSTOMER);
        User uC13 = saveUser("Rania Qasem",    "rania.qasem@yahoo.com",    UserRole.CUSTOMER);
        User uC14 = saveUser("Fady Gergis",    "fady.gergis@gmail.com",    UserRole.CUSTOMER);
        User uC15 = saveUser("Nada Sherif",    "nada.sherif@outlook.com",  UserRole.CUSTOMER);

        ctx.c1  = saveCustomer(uC1,  "1990-04-15", "EG", "+20-100-123-4567");
        ctx.c2  = saveCustomer(uC2,  "1988-08-22", "EG", "+20-101-234-5678");
        ctx.c3  = saveCustomer(uC3,  "1993-11-30", "JO", "+962-79-456-7890");
        ctx.c4  = saveCustomer(uC4,  "1985-03-07", "PK", "+92-300-123-4567");
        ctx.c5  = saveCustomer(uC5,  "1997-06-19", "EG", "+20-102-345-6789");
        ctx.c6  = saveCustomer(uC6,  "1991-12-25", "EG", "+20-103-456-7890");
        ctx.c7  = saveCustomer(uC7,  "1994-09-14", "LB", "+961-70-123-456");
        ctx.c8  = saveCustomer(uC8,  "1986-01-28", "SA", "+966-55-123-4567");
        ctx.c9  = saveCustomer(uC9,  "1999-05-03", "EG", "+20-104-567-8901");
        ctx.c10 = saveCustomer(uC10, "1989-07-11", "MA", "+212-61-234-5678");
        ctx.c11 = saveCustomer(uC11, "1995-10-18", "EG", "+20-105-678-9012");
        ctx.c12 = saveCustomer(uC12, "1987-02-14", "LB", "+961-71-234-567");
        ctx.c13 = saveCustomer(uC13, "1992-08-09", "JO", "+962-78-567-8901");
        ctx.c14 = saveCustomer(uC14, "1996-04-23", "EG", "+20-106-789-0123");
        ctx.c15 = saveCustomer(uC15, "1998-12-01", "EG", "+20-107-890-1234");

        log.info("  ✔ Users seeded");
    }

    // =========================================================================
    // 2. HOTELS + LOCATIONS + AMENITIES + ACCESSIBILITY + PHOTOS + NEARBY + POLICIES
    // =========================================================================
    private void seedHotels(SeedContext ctx) {
        ctx.h1 = saveHotel("Grand Azure Hotel",       "15 Corniche Road",       "Dubai",     "AE", 25.2048,  55.2708, 5, HotelType.HOTEL,      ctx.mgr4);
        ctx.h2 = saveHotel("Bella Vista Resort",      "22 Via Roma",            "Rome",      "IT", 41.9028,  12.4964, 5, HotelType.RESORT,     ctx.mgr2);
        ctx.h3 = saveHotel("Sunridge Boutique Inn",   "88 Ocean Drive",         "Miami",     "US", 25.7617, -80.1918, 4, HotelType.BOUTIQUE,   ctx.mgr3);
        ctx.h4 = saveHotel("Nordic Fjord Inn",        "5 Storgatan",            "Stockholm", "SE", 59.3293,  18.0686, 4, HotelType.INN,        ctx.mgr5);
        ctx.h5 = saveHotel("Desert Oasis Retreat",    "101 Sahara Boulevard",   "Marrakech", "MA", 31.6295,  -7.9811, 4, HotelType.VILLA,      ctx.mgr4);
        ctx.h6 = saveHotel("London Heights Hotel",    "44 Baker Street",        "London",    "GB", 51.5074,  -0.1278, 4, HotelType.HOTEL,      ctx.mgr1);
        ctx.h7 = saveHotel("Nile View Guesthouse",    "7 Tahrir Square",        "Cairo",     "EG", 30.0444,  31.2357, 3, HotelType.GUESTHOUSE, ctx.mgr4);
        ctx.h8 = saveHotel("Tokyo Zen Lodge",         "12-3 Shinjuku",          "Tokyo",     "JP", 35.6895, 139.6917, 4, HotelType.LODGE,      ctx.mgr1);
        ctx.h9 = saveHotel("Barcelona Hostel Central","Carrer de Pelai 32",     "Barcelona", "ES", 41.3851,   2.1734, 3, HotelType.HOSTEL,     ctx.mgr2);

        // Locations
        saveLocation(ctx.h1, "United Arab Emirates", "Dubai",     "Dubai",            "15 Corniche Road",      "00000",   25.2048,  55.2708);
        saveLocation(ctx.h2, "Italy",                "Rome",      "Lazio",            "22 Via Roma",           "00184",   41.9028,  12.4964);
        saveLocation(ctx.h3, "United States",        "Miami",     "Florida",          "88 Ocean Drive",        "33139",   25.7617, -80.1918);
        saveLocation(ctx.h4, "Sweden",               "Stockholm", "Stockholm County", "5 Storgatan",           "111 23",  59.3293,  18.0686);
        saveLocation(ctx.h5, "Morocco",              "Marrakech", "Marrakesh-Safi",   "101 Sahara Boulevard",  "40000",   31.6295,  -7.9811);
        saveLocation(ctx.h6, "United Kingdom",       "London",    "England",          "44 Baker Street",       "W1U6TY",  51.5074,  -0.1278);
        saveLocation(ctx.h7, "Egypt",                "Cairo",     "Cairo Governorate","7 Tahrir Square",       "11511",   30.0444,  31.2357);
        saveLocation(ctx.h8, "Japan",                "Tokyo",     "Tokyo",            "12-3 Shinjuku",         "160-0022",35.6895, 139.6917);
        saveLocation(ctx.h9, "Spain",                "Barcelona", "Catalonia",        "Carrer de Pelai 32",    "08001",   41.3851,   2.1734);

        seedHotelAmenities(ctx);
        seedHotelAccessibility(ctx);
        seedHotelPhotos(ctx);
        seedNearbyPlaces(ctx);
        seedPolicies(ctx);

        log.info("  ✔ Hotels seeded");
    }

    private void seedHotelAmenities(SeedContext ctx) {
        // Grand Azure (h1)
        saveHotelAmenity(ctx.h1, "Infinity Pool",           AmenityCategory.WELLNESS,     "pool");
        saveHotelAmenity(ctx.h1, "Full-Service Spa",         AmenityCategory.WELLNESS,     "spa");
        saveHotelAmenity(ctx.h1, "Rooftop Restaurant",       AmenityCategory.DINING,       "restaurant");
        saveHotelAmenity(ctx.h1, "Valet Parking",            AmenityCategory.PARKING,      "parking");
        saveHotelAmenity(ctx.h1, "Airport Shuttle",          AmenityCategory.TRANSPORT,    "shuttle");
        saveHotelAmenity(ctx.h1, "Free High-Speed WiFi",     AmenityCategory.CONNECTIVITY, "wifi");
        saveHotelAmenity(ctx.h1, "Business Center",          AmenityCategory.BUSINESS,     "business");
        saveHotelAmenity(ctx.h1, "Kids Club",                AmenityCategory.FAMILY,       "kids");
        saveHotelAmenity(ctx.h1, "Fitness Center",           AmenityCategory.WELLNESS,     "gym");
        saveHotelAmenity(ctx.h1, "Cocktail Bar",             AmenityCategory.DINING,       "bar");
        // Bella Vista (h2)
        saveHotelAmenity(ctx.h2, "Olympic Pool",             AmenityCategory.WELLNESS,     "pool");
        saveHotelAmenity(ctx.h2, "Rooftop Bar",              AmenityCategory.DINING,       "bar");
        saveHotelAmenity(ctx.h2, "Fine Dining Restaurant",   AmenityCategory.DINING,       "restaurant");
        saveHotelAmenity(ctx.h2, "Free Parking",             AmenityCategory.PARKING,      "parking");
        saveHotelAmenity(ctx.h2, "Spa & Wellness",           AmenityCategory.WELLNESS,     "spa");
        saveHotelAmenity(ctx.h2, "Free WiFi",                AmenityCategory.CONNECTIVITY, "wifi");
        saveHotelAmenity(ctx.h2, "Conference Room",          AmenityCategory.BUSINESS,     "conference");
        // Sunridge (h3)
        saveHotelAmenity(ctx.h3, "Beachfront Pool",          AmenityCategory.WELLNESS,     "pool");
        saveHotelAmenity(ctx.h3, "Beach Bar",                AmenityCategory.DINING,       "bar");
        saveHotelAmenity(ctx.h3, "Complimentary Breakfast",  AmenityCategory.DINING,       "restaurant");
        saveHotelAmenity(ctx.h3, "Free WiFi",                AmenityCategory.CONNECTIVITY, "wifi");
        saveHotelAmenity(ctx.h3, "Bicycle Rental",           AmenityCategory.TRANSPORT,    "bike");
        saveHotelAmenity(ctx.h3, "Gym",                      AmenityCategory.WELLNESS,     "gym");
        // Nordic Fjord (h4)
        saveHotelAmenity(ctx.h4, "Sauna",                    AmenityCategory.WELLNESS,     "sauna");
        saveHotelAmenity(ctx.h4, "Restaurant",               AmenityCategory.DINING,       "restaurant");
        saveHotelAmenity(ctx.h4, "Free WiFi",                AmenityCategory.CONNECTIVITY, "wifi");
        saveHotelAmenity(ctx.h4, "Parking Garage",           AmenityCategory.PARKING,      "parking");
        saveHotelAmenity(ctx.h4, "Ski Storage",              AmenityCategory.TRANSPORT,    "ski");
        // Desert Oasis (h5)
        saveHotelAmenity(ctx.h5, "Private Pool",             AmenityCategory.WELLNESS,     "pool");
        saveHotelAmenity(ctx.h5, "Traditional Riad Spa",     AmenityCategory.WELLNESS,     "spa");
        saveHotelAmenity(ctx.h5, "Moroccan Restaurant",      AmenityCategory.DINING,       "restaurant");
        saveHotelAmenity(ctx.h5, "Free WiFi",                AmenityCategory.CONNECTIVITY, "wifi");
        saveHotelAmenity(ctx.h5, "Camel Tour Desk",          AmenityCategory.TRANSPORT,    "tour");
        saveHotelAmenity(ctx.h5, "Rooftop Terrace",          AmenityCategory.DINING,       "terrace");
        // London Heights (h6)
        saveHotelAmenity(ctx.h6, "Fitness Room",             AmenityCategory.WELLNESS,     "gym");
        saveHotelAmenity(ctx.h6, "Restaurant & Bar",         AmenityCategory.DINING,       "restaurant");
        saveHotelAmenity(ctx.h6, "Free WiFi",                AmenityCategory.CONNECTIVITY, "wifi");
        saveHotelAmenity(ctx.h6, "Meeting Rooms",            AmenityCategory.BUSINESS,     "conference");
        saveHotelAmenity(ctx.h6, "Concierge Service",        AmenityCategory.TRANSPORT,    "concierge");
        // Nile View (h7)
        saveHotelAmenity(ctx.h7, "Rooftop Terrace",          AmenityCategory.DINING,       "terrace");
        saveHotelAmenity(ctx.h7, "Free WiFi",                AmenityCategory.CONNECTIVITY, "wifi");
        saveHotelAmenity(ctx.h7, "Traditional Egyptian Café", AmenityCategory.DINING,      "cafe");
        saveHotelAmenity(ctx.h7, "Airport Taxi Desk",        AmenityCategory.TRANSPORT,    "taxi");
        // Tokyo Zen (h8)
        saveHotelAmenity(ctx.h8, "Hot Spring Bath",          AmenityCategory.WELLNESS,     "spa");
        saveHotelAmenity(ctx.h8, "Japanese Restaurant",      AmenityCategory.DINING,       "restaurant");
        saveHotelAmenity(ctx.h8, "Free WiFi",                AmenityCategory.CONNECTIVITY, "wifi");
        saveHotelAmenity(ctx.h8, "Tea Ceremony Room",        AmenityCategory.DINING,       "tea");
        saveHotelAmenity(ctx.h8, "Bicycle Rental",           AmenityCategory.TRANSPORT,    "bike");
        // Barcelona Hostel (h9)
        saveHotelAmenity(ctx.h9, "Common Kitchen",           AmenityCategory.DINING,       "kitchen");
        saveHotelAmenity(ctx.h9, "Free WiFi",                AmenityCategory.CONNECTIVITY, "wifi");
        saveHotelAmenity(ctx.h9, "Luggage Storage",          AmenityCategory.TRANSPORT,    "storage");
        saveHotelAmenity(ctx.h9, "Social Lounge",            AmenityCategory.DINING,       "lounge");
        saveHotelAmenity(ctx.h9, "Bike Rental",              AmenityCategory.TRANSPORT,    "bike");
    }

    private void seedHotelAccessibility(SeedContext ctx) {
        saveHotelAccess(ctx.h1, "Wheelchair Access",  AccessibilityLevel.FULL,    "All public areas wheelchair accessible");
        saveHotelAccess(ctx.h1, "Elevator",           AccessibilityLevel.FULL,    "High-speed elevators on all floors");
        saveHotelAccess(ctx.h1, "Accessible Parking", AccessibilityLevel.FULL,    "Dedicated parking spaces near entrance");
        saveHotelAccess(ctx.h1, "Braille Signage",    AccessibilityLevel.PARTIAL, "Braille on main floor only");
        saveHotelAccess(ctx.h2, "Wheelchair Access",  AccessibilityLevel.FULL,    "Fully accessible entrance and rooms");
        saveHotelAccess(ctx.h2, "Elevator",           AccessibilityLevel.FULL,    "Accessible elevators on all floors");
        saveHotelAccess(ctx.h2, "Hearing Loop",       AccessibilityLevel.PARTIAL, "Available in lobby and conference rooms");
        saveHotelAccess(ctx.h3, "Wheelchair Access",  AccessibilityLevel.PARTIAL, "Ground floor and pool area accessible");
        saveHotelAccess(ctx.h3, "Elevator",           AccessibilityLevel.FULL,    "Elevator access to all floors");
        saveHotelAccess(ctx.h4, "Wheelchair Access",  AccessibilityLevel.PARTIAL, "Main building accessible, some areas steps");
        saveHotelAccess(ctx.h4, "Elevator",           AccessibilityLevel.FULL,    "Modern accessible elevator installed 2023");
        saveHotelAccess(ctx.h5, "Wheelchair Access",  AccessibilityLevel.PARTIAL, "Ground floor riad fully accessible");
        saveHotelAccess(ctx.h5, "Elevator",           AccessibilityLevel.NONE,    "Traditional riad with no elevator");
        saveHotelAccess(ctx.h6, "Wheelchair Access",  AccessibilityLevel.FULL,    "Step-free access throughout");
        saveHotelAccess(ctx.h6, "Elevator",           AccessibilityLevel.FULL,    "Accessible elevators");
        saveHotelAccess(ctx.h7, "Wheelchair Access",  AccessibilityLevel.PARTIAL, "Limited — main lobby accessible");
        saveHotelAccess(ctx.h7, "Elevator",           AccessibilityLevel.FULL,    "Elevator to all guestroom floors");
        saveHotelAccess(ctx.h8, "Wheelchair Access",  AccessibilityLevel.FULL,    "Step-free access across main areas");
        saveHotelAccess(ctx.h8, "Elevator",           AccessibilityLevel.FULL,    "Two modern elevators");
        saveHotelAccess(ctx.h9, "Wheelchair Access",  AccessibilityLevel.NONE,    "Hostel has stairs, no accessible rooms");
        saveHotelAccess(ctx.h9, "Elevator",           AccessibilityLevel.NONE,    "No elevator — stairs only");
    }

    private void seedHotelPhotos(SeedContext ctx) {
        saveHotelPhoto(ctx.h1, "https://images.unsplash.com/photo-1566073771259-6a8506099945", "Grand Azure Exterior",         true,  1);
        saveHotelPhoto(ctx.h1, "https://images.unsplash.com/photo-1582719508461-905c673771fd", "Infinity Pool at Night",       false, 2);
        saveHotelPhoto(ctx.h1, "https://images.unsplash.com/photo-1631049307264-da0ec9d70304", "Deluxe Room Interior",         false, 3);
        saveHotelPhoto(ctx.h1, "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa",   "Rooftop Restaurant",           false, 4);
        saveHotelPhoto(ctx.h1, "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4", "Spa Suite",                   false, 5);

        saveHotelPhoto(ctx.h2, "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb",   "Bella Vista Front",            true,  1);
        saveHotelPhoto(ctx.h2, "https://images.unsplash.com/photo-1571896349842-33c89424de2d", "Olympic Pool",                false, 2);
        saveHotelPhoto(ctx.h2, "https://images.unsplash.com/photo-1590490360182-c33d57733427", "Superior Room",               false, 3);
        saveHotelPhoto(ctx.h2, "https://images.unsplash.com/photo-1551776235-dde6d482980b",   "Fine Dining",                  false, 4);

        saveHotelPhoto(ctx.h3, "https://images.unsplash.com/photo-1445019980597-93fa8acb246c", "Sunridge Beach View",          true,  1);
        saveHotelPhoto(ctx.h3, "https://images.unsplash.com/photo-1602002418082-a4443e081dd1", "Beachfront Pool",              false, 2);
        saveHotelPhoto(ctx.h3, "https://images.unsplash.com/photo-1611892440504-42a792e24d32", "Ocean Suite",                  false, 3);

        saveHotelPhoto(ctx.h4, "https://images.unsplash.com/photo-1560185893-a55cbc8c57e8",   "Nordic Fjord Exterior",        true,  1);
        saveHotelPhoto(ctx.h4, "https://images.unsplash.com/photo-1584132967334-10e028bd69f7", "Swedish Sauna",               false, 2);
        saveHotelPhoto(ctx.h4, "https://images.unsplash.com/photo-1595576508898-0ad5c879a061", "Cozy Room",                   false, 3);

        saveHotelPhoto(ctx.h5, "https://images.unsplash.com/photo-1512100356356-de1b84283e18", "Desert Oasis Riad",            true,  1);
        saveHotelPhoto(ctx.h5, "https://images.unsplash.com/photo-1584132905271-512c958d674a", "Moroccan Pool",               false, 2);
        saveHotelPhoto(ctx.h5, "https://images.unsplash.com/photo-1606402179428-a57976d71fa4", "Traditional Suite",           false, 3);

        saveHotelPhoto(ctx.h6, "https://images.unsplash.com/photo-1455587734955-081b22074882", "London Heights Facade",        true,  1);
        saveHotelPhoto(ctx.h6, "https://images.unsplash.com/photo-1525596662741-e94ff9f26de1", "Classic Double Room",         false, 2);

        saveHotelPhoto(ctx.h7, "https://images.unsplash.com/photo-1496417263034-38ec4f0b665a", "Nile View Rooftop",            true,  1);
        saveHotelPhoto(ctx.h7, "https://images.unsplash.com/photo-1578683010236-d716f9a3f461", "Cozy Cairo Room",             false, 2);

        saveHotelPhoto(ctx.h8, "https://images.unsplash.com/photo-1551538827-9c037cb4f32a",   "Tokyo Zen Exterior",           true,  1);
        saveHotelPhoto(ctx.h8, "https://images.unsplash.com/photo-1611892440504-42a792e24d32", "Minimalist Room",             false, 2);

        saveHotelPhoto(ctx.h9, "https://images.unsplash.com/photo-1555854877-bab0e564b8d5",   "Barcelona Hostel Common Area", true,  1);
        saveHotelPhoto(ctx.h9, "https://images.unsplash.com/photo-1540555700478-4be289fbecef", "Hostel Dorm",                 false, 2);
    }

    private void seedNearbyPlaces(SeedContext ctx) {
        saveNearby(ctx.h1, "Dubai International Airport",    NearbyPlaceType.AIRPORT,     BigDecimal.valueOf(12.5));
        saveNearby(ctx.h1, "Dubai Mall",                     NearbyPlaceType.MALL,        BigDecimal.valueOf(3.2));
        saveNearby(ctx.h1, "Burj Al Arab",                   NearbyPlaceType.LANDMARK,    BigDecimal.valueOf(4.8));
        saveNearby(ctx.h1, "Wild Wadi Waterpark",             NearbyPlaceType.LANDMARK,    BigDecimal.valueOf(5.1));
        saveNearby(ctx.h1, "Rashid Hospital",                NearbyPlaceType.HOSPITAL,    BigDecimal.valueOf(6.3));

        saveNearby(ctx.h2, "Fiumicino Airport (FCO)",        NearbyPlaceType.AIRPORT,     BigDecimal.valueOf(32.0));
        saveNearby(ctx.h2, "Trevi Fountain",                 NearbyPlaceType.LANDMARK,    BigDecimal.valueOf(1.2));
        saveNearby(ctx.h2, "Campo de Fiori Market",          NearbyPlaceType.MALL,        BigDecimal.valueOf(0.8));
        saveNearby(ctx.h2, "Gemelli Hospital",               NearbyPlaceType.HOSPITAL,    BigDecimal.valueOf(4.5));
        saveNearby(ctx.h2, "Armando al Pantheon",            NearbyPlaceType.RESTAURANT,  BigDecimal.valueOf(1.0));

        saveNearby(ctx.h3, "Miami International Airport",    NearbyPlaceType.AIRPORT,     BigDecimal.valueOf(11.0));
        saveNearby(ctx.h3, "South Beach",                    NearbyPlaceType.BEACH,       BigDecimal.valueOf(0.4));
        saveNearby(ctx.h3, "Lincoln Road Mall",              NearbyPlaceType.MALL,        BigDecimal.valueOf(1.2));
        saveNearby(ctx.h3, "Mount Sinai Medical Center",     NearbyPlaceType.HOSPITAL,    BigDecimal.valueOf(4.0));

        saveNearby(ctx.h4, "Arlanda Airport",                NearbyPlaceType.AIRPORT,     BigDecimal.valueOf(42.0));
        saveNearby(ctx.h4, "Gamla Stan Old Town",            NearbyPlaceType.LANDMARK,    BigDecimal.valueOf(2.5));
        saveNearby(ctx.h4, "Åhléns City",                    NearbyPlaceType.MALL,        BigDecimal.valueOf(1.0));
        saveNearby(ctx.h4, "Karolinska Hospital",            NearbyPlaceType.HOSPITAL,    BigDecimal.valueOf(5.5));

        saveNearby(ctx.h5, "Menara Airport",                 NearbyPlaceType.AIRPORT,     BigDecimal.valueOf(8.0));
        saveNearby(ctx.h5, "Jemaa el-Fnaa Square",           NearbyPlaceType.LANDMARK,    BigDecimal.valueOf(2.0));
        saveNearby(ctx.h5, "Marrakech Medina Souk",          NearbyPlaceType.MALL,        BigDecimal.valueOf(2.5));
        saveNearby(ctx.h5, "Clinique Internationale",        NearbyPlaceType.HOSPITAL,    BigDecimal.valueOf(4.0));

        saveNearby(ctx.h6, "Heathrow Airport",               NearbyPlaceType.AIRPORT,     BigDecimal.valueOf(25.0));
        saveNearby(ctx.h6, "Regent's Park",                  NearbyPlaceType.LANDMARK,    BigDecimal.valueOf(0.5));
        saveNearby(ctx.h6, "Marylebone High Street",         NearbyPlaceType.MALL,        BigDecimal.valueOf(0.3));
        saveNearby(ctx.h6, "Great Portland Street Hospital", NearbyPlaceType.HOSPITAL,    BigDecimal.valueOf(1.8));

        saveNearby(ctx.h7, "Cairo International Airport",    NearbyPlaceType.AIRPORT,     BigDecimal.valueOf(23.0));
        saveNearby(ctx.h7, "Egyptian Museum",                NearbyPlaceType.LANDMARK,    BigDecimal.valueOf(0.8));
        saveNearby(ctx.h7, "Cairo Festival City Mall",       NearbyPlaceType.MALL,        BigDecimal.valueOf(12.0));
        saveNearby(ctx.h7, "Kasr El Ainy Hospital",          NearbyPlaceType.HOSPITAL,    BigDecimal.valueOf(3.5));

        saveNearby(ctx.h8, "Narita Airport",                 NearbyPlaceType.AIRPORT,     BigDecimal.valueOf(72.0));
        saveNearby(ctx.h8, "Shinjuku Gyoen Garden",          NearbyPlaceType.LANDMARK,    BigDecimal.valueOf(1.0));
        saveNearby(ctx.h8, "Shinjuku Station Shopping",      NearbyPlaceType.MALL,        BigDecimal.valueOf(0.5));
        saveNearby(ctx.h8, "Keio University Hospital",       NearbyPlaceType.HOSPITAL,    BigDecimal.valueOf(2.0));

        saveNearby(ctx.h9, "El Prat Airport",                NearbyPlaceType.AIRPORT,     BigDecimal.valueOf(18.0));
        saveNearby(ctx.h9, "La Rambla",                      NearbyPlaceType.LANDMARK,    BigDecimal.valueOf(0.3));
        saveNearby(ctx.h9, "El Corte Inglés",                NearbyPlaceType.MALL,        BigDecimal.valueOf(0.2));
        saveNearby(ctx.h9, "Hospital de la Santa Creu",      NearbyPlaceType.HOSPITAL,    BigDecimal.valueOf(1.5));
    }

    private void seedPolicies(SeedContext ctx) {
        // Breakfast
        breakfastPolicyRepository.save(BreakfastPolicy.builder().hotel(ctx.h1).breakfastOffered(true).includedInPrice(false).pricePerPerson(new BigDecimal("35.00")).type(BreakfastType.BUFFET).build());
        breakfastPolicyRepository.save(BreakfastPolicy.builder().hotel(ctx.h2).breakfastOffered(true).includedInPrice(true).type(BreakfastType.CONTINENTAL).build());
        breakfastPolicyRepository.save(BreakfastPolicy.builder().hotel(ctx.h3).breakfastOffered(true).includedInPrice(true).type(BreakfastType.AMERICAN).build());
        breakfastPolicyRepository.save(BreakfastPolicy.builder().hotel(ctx.h4).breakfastOffered(true).includedInPrice(false).pricePerPerson(new BigDecimal("18.00")).type(BreakfastType.CONTINENTAL).build());
        breakfastPolicyRepository.save(BreakfastPolicy.builder().hotel(ctx.h5).breakfastOffered(true).includedInPrice(true).type(BreakfastType.BUFFET).build());
        breakfastPolicyRepository.save(BreakfastPolicy.builder().hotel(ctx.h6).breakfastOffered(true).includedInPrice(false).pricePerPerson(new BigDecimal("22.00")).type(BreakfastType.FULL_ENGLISH).build());
        breakfastPolicyRepository.save(BreakfastPolicy.builder().hotel(ctx.h7).breakfastOffered(true).includedInPrice(false).pricePerPerson(new BigDecimal("12.00")).type(BreakfastType.CONTINENTAL).build());
        breakfastPolicyRepository.save(BreakfastPolicy.builder().hotel(ctx.h8).breakfastOffered(false).includedInPrice(false).build());
        breakfastPolicyRepository.save(BreakfastPolicy.builder().hotel(ctx.h9).breakfastOffered(false).includedInPrice(false).build());

        // Check-in
        checkInPolicyRepository.save(CheckInPolicy.builder().hotel(ctx.h1).description("Standard check-in from 3 PM, check-out by 12 PM").earliestTime("15:00").latestTime("23:59").earlyCheckIn(true).lateCheckOut(true).build());
        checkInPolicyRepository.save(CheckInPolicy.builder().hotel(ctx.h2).description("Check-in from 2 PM, check-out by 11 AM").earliestTime("14:00").latestTime("23:59").earlyCheckIn(true).lateCheckOut(true).build());
        checkInPolicyRepository.save(CheckInPolicy.builder().hotel(ctx.h3).description("Flexible check-in from 3 PM, check-out by noon").earliestTime("15:00").latestTime("23:59").earlyCheckIn(true).lateCheckOut(true).build());
        checkInPolicyRepository.save(CheckInPolicy.builder().hotel(ctx.h4).description("Check-in from 4 PM, early check-in on request").earliestTime("16:00").latestTime("22:00").earlyCheckIn(true).lateCheckOut(false).build());
        checkInPolicyRepository.save(CheckInPolicy.builder().hotel(ctx.h5).description("Riad check-in from 2 PM, check-out by 11 AM").earliestTime("14:00").latestTime("23:59").earlyCheckIn(false).lateCheckOut(false).build());
        checkInPolicyRepository.save(CheckInPolicy.builder().hotel(ctx.h6).description("Check-in from 3 PM, check-out by 12 PM").earliestTime("15:00").latestTime("23:59").earlyCheckIn(true).lateCheckOut(true).build());
        checkInPolicyRepository.save(CheckInPolicy.builder().hotel(ctx.h7).description("Check-in from 2 PM, check-out by 12 PM").earliestTime("14:00").latestTime("20:00").earlyCheckIn(false).lateCheckOut(false).build());
        checkInPolicyRepository.save(CheckInPolicy.builder().hotel(ctx.h8).description("Check-in from 3 PM, check-out by 11 AM").earliestTime("15:00").latestTime("22:00").earlyCheckIn(false).lateCheckOut(false).build());
        checkInPolicyRepository.save(CheckInPolicy.builder().hotel(ctx.h9).description("Self check-in 24 hours, check-out by 10 AM").earliestTime("00:00").latestTime("23:59").earlyCheckIn(false).lateCheckOut(false).build());

        // Pet
        petPolicyRepository.save(PetPolicy.builder().hotel(ctx.h1).petsAllowed(false).build());
        petPolicyRepository.save(PetPolicy.builder().hotel(ctx.h2).petsAllowed(true).petFee(new BigDecimal("50.00")).build());
        petPolicyRepository.save(PetPolicy.builder().hotel(ctx.h3).petsAllowed(true).petFee(new BigDecimal("30.00")).build());
        petPolicyRepository.save(PetPolicy.builder().hotel(ctx.h4).petsAllowed(true).petFee(new BigDecimal("25.00")).build());
        petPolicyRepository.save(PetPolicy.builder().hotel(ctx.h5).petsAllowed(false).build());
        petPolicyRepository.save(PetPolicy.builder().hotel(ctx.h6).petsAllowed(true).petFee(new BigDecimal("40.00")).build());
        petPolicyRepository.save(PetPolicy.builder().hotel(ctx.h7).petsAllowed(false).build());
        petPolicyRepository.save(PetPolicy.builder().hotel(ctx.h8).petsAllowed(false).build());
        petPolicyRepository.save(PetPolicy.builder().hotel(ctx.h9).petsAllowed(false).build());
    }

    // =========================================================================
    // 3. ROOMS + ROOM AMENITIES + ROOM ACCESSIBILITY + ROOM PHOTOS
    // =========================================================================
    private void seedRooms(SeedContext ctx) {
        // Hotel 1
        ctx.r1Std   = saveRoom(ctx.h1, "Standard King Room",      RoomType.STANDARD, BedType.KING,   149.00, 8,  32.0,  3, 2, 1, RoomView.CITY);
        ctx.r1Dlx   = saveRoom(ctx.h1, "Deluxe Sea View Room",    RoomType.DELUXE,   BedType.KING,   249.00, 6,  45.0,  6, 2, 2, RoomView.SEA);
        ctx.r1Suite = saveRoom(ctx.h1, "Presidential Suite",      RoomType.SUITE,    BedType.KING,   399.00, 2,  90.0, 18, 3, 2, RoomView.SEA);
        ctx.r1Fam   = saveRoom(ctx.h1, "Family Garden Room",      RoomType.FAMILY,   BedType.TWIN,   199.00, 5,  55.0,  2, 2, 3, RoomView.GARDEN);
        ctx.r1Villa = saveRoom(ctx.h1, "Private Pool Villa",      RoomType.VILLA,    BedType.KING,   599.00, 1, 150.0,  1, 4, 2, RoomView.POOL);
        // Hotel 2
        ctx.r2Std   = saveRoom(ctx.h2, "Classic Double Room",     RoomType.STANDARD, BedType.DOUBLE, 129.00,10,  28.0,  2, 2, 0, RoomView.CITY);
        ctx.r2Dlx   = saveRoom(ctx.h2, "Deluxe City View",        RoomType.DELUXE,   BedType.QUEEN,  189.00, 6,  40.0,  5, 2, 1, RoomView.CITY);
        ctx.r2Suite = saveRoom(ctx.h2, "Terrace Suite",           RoomType.SUITE,    BedType.KING,   320.00, 3,  75.0,  7, 3, 1, RoomView.CITY);
        ctx.r2Fam   = saveRoom(ctx.h2, "Family Room",             RoomType.FAMILY,   BedType.TWIN,   210.00, 4,  50.0,  3, 2, 2, RoomView.GARDEN);
        // Hotel 3
        ctx.r3Std   = saveRoom(ctx.h3, "Standard Ocean Room",     RoomType.STANDARD, BedType.QUEEN,  119.00, 8,  30.0,  2, 2, 0, RoomView.SEA);
        ctx.r3Dlx   = saveRoom(ctx.h3, "Deluxe Beachfront Suite", RoomType.DELUXE,   BedType.KING,   199.00, 5,  48.0,  4, 2, 1, RoomView.SEA);
        ctx.r3Suite = saveRoom(ctx.h3, "Penthouse Ocean Suite",   RoomType.SUITE,    BedType.KING,   350.00, 2,  80.0, 10, 3, 1, RoomView.SEA);
        ctx.r3Fam   = saveRoom(ctx.h3, "Family Cabana Room",      RoomType.FAMILY,   BedType.BUNK,   179.00, 4,  52.0,  1, 2, 3, RoomView.POOL);
        // Hotel 4
        ctx.r4Std   = saveRoom(ctx.h4, "Cozy Standard Room",      RoomType.STANDARD, BedType.DOUBLE,  99.00, 6,  22.0,  2, 2, 0, RoomView.CITY);
        ctx.r4Dlx   = saveRoom(ctx.h4, "Fjord View Deluxe",       RoomType.DELUXE,   BedType.QUEEN,  159.00, 4,  35.0,  4, 2, 1, RoomView.CITY);
        ctx.r4Suite = saveRoom(ctx.h4, "Nordic Suite",             RoomType.SUITE,    BedType.KING,   280.00, 2,  60.0,  6, 2, 1, RoomView.CITY);
        // Hotel 5
        ctx.r5Std   = saveRoom(ctx.h5, "Traditional Riad Room",   RoomType.STANDARD, BedType.DOUBLE, 110.00, 6,  28.0,  1, 2, 0, RoomView.GARDEN);
        ctx.r5Dlx   = saveRoom(ctx.h5, "Deluxe Riad Suite",       RoomType.DELUXE,   BedType.KING,   180.00, 4,  42.0,  2, 2, 1, RoomView.GARDEN);
        ctx.r5Villa = saveRoom(ctx.h5, "Private Desert Villa",    RoomType.VILLA,    BedType.KING,   390.00, 2, 100.0,  1, 4, 2, RoomView.GARDEN);
        // Hotel 6
        ctx.r6Std   = saveRoom(ctx.h6, "Classic Standard Room",   RoomType.STANDARD, BedType.SINGLE,  89.00, 8,  20.0,  2, 1, 0, RoomView.CITY);
        ctx.r6Dlx   = saveRoom(ctx.h6, "Executive Double",        RoomType.DELUXE,   BedType.DOUBLE, 149.00, 5,  32.0,  4, 2, 1, RoomView.CITY);
        ctx.r6Suite = saveRoom(ctx.h6, "Hyde Park Suite",         RoomType.SUITE,    BedType.KING,   260.00, 2,  55.0,  7, 3, 1, RoomView.CITY);
        // Hotel 7
        ctx.r7Std   = saveRoom(ctx.h7, "Economy Room",            RoomType.STANDARD, BedType.SINGLE,  65.00, 8,  18.0,  2, 1, 0, RoomView.CITY);
        ctx.r7Dlx   = saveRoom(ctx.h7, "Nile View Double",        RoomType.DELUXE,   BedType.DOUBLE,  95.00, 4,  28.0,  4, 2, 1, RoomView.CITY);
        // Hotel 8
        ctx.r8Std   = saveRoom(ctx.h8, "Tatami Standard Room",    RoomType.STANDARD, BedType.SINGLE,  90.00, 6,  18.0,  2, 1, 0, RoomView.CITY);
        ctx.r8Dlx   = saveRoom(ctx.h8, "Deluxe Zen Room",         RoomType.DELUXE,   BedType.QUEEN,  140.00, 4,  28.0,  4, 2, 0, RoomView.CITY);
        ctx.r8Suite = saveRoom(ctx.h8, "Onsen Suite",             RoomType.SUITE,    BedType.KING,   280.00, 2,  55.0,  5, 2, 0, RoomView.CITY);
        // Hotel 9
        ctx.r9Dorm  = saveRoom(ctx.h9, "8-Bed Mixed Dorm",        RoomType.STANDARD, BedType.BUNK,    28.00,10,  50.0,  1, 1, 0, RoomView.NONE);
        ctx.r9Pvt   = saveRoom(ctx.h9, "Private Twin Room",       RoomType.STANDARD, BedType.TWIN,    75.00, 4,  18.0,  2, 2, 0, RoomView.NONE);

        seedRoomAmenities(ctx);
        seedRoomAccessibility(ctx);
        seedRoomPhotos(ctx);

        log.info("  ✔ Rooms seeded");
    }

    private void seedRoomAmenities(SeedContext ctx) {
        com.HotelBook.HotelBooking.RoomAmenity.AmenityCategory TECH    = com.HotelBook.HotelBooking.RoomAmenity.AmenityCategory.TECH;
        com.HotelBook.HotelBooking.RoomAmenity.AmenityCategory COMFORT = com.HotelBook.HotelBooking.RoomAmenity.AmenityCategory.COMFORT;
        com.HotelBook.HotelBooking.RoomAmenity.AmenityCategory BATH    = com.HotelBook.HotelBooking.RoomAmenity.AmenityCategory.BATHROOM;
        com.HotelBook.HotelBooking.RoomAmenity.AmenityCategory KITCH   = com.HotelBook.HotelBooking.RoomAmenity.AmenityCategory.KITCHEN;

        saveRoomAmenity(ctx.r1Std,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r1Std,   "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r1Std,   "Smart TV",          TECH,    "tv");
        saveRoomAmenity(ctx.r1Std,   "Private Bathroom",  BATH,    "bath");

        saveRoomAmenity(ctx.r1Dlx,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r1Dlx,   "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r1Dlx,   "Smart TV",          TECH,    "tv");
        saveRoomAmenity(ctx.r1Dlx,   "Jacuzzi",           BATH,    "jacuzzi");
        saveRoomAmenity(ctx.r1Dlx,   "Minibar",           KITCH,   "minibar");

        saveRoomAmenity(ctx.r1Suite, "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r1Suite, "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r1Suite, "Smart TV",          TECH,    "tv");
        saveRoomAmenity(ctx.r1Suite, "Jacuzzi",           BATH,    "jacuzzi");
        saveRoomAmenity(ctx.r1Suite, "Full Kitchen",      KITCH,   "kitchen");
        saveRoomAmenity(ctx.r1Suite, "Butler Service",    COMFORT, "butler");

        saveRoomAmenity(ctx.r1Fam,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r1Fam,   "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r1Fam,   "Smart TV",          TECH,    "tv");
        saveRoomAmenity(ctx.r1Fam,   "Cribs Available",   COMFORT, "crib");

        saveRoomAmenity(ctx.r1Villa, "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r1Villa, "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r1Villa, "Private Pool",      COMFORT, "pool");
        saveRoomAmenity(ctx.r1Villa, "Full Kitchen",      KITCH,   "kitchen");
        saveRoomAmenity(ctx.r1Villa, "Private Gym",       COMFORT, "gym");

        saveRoomAmenity(ctx.r2Std,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r2Std,   "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r2Std,   "TV",                TECH,    "tv");
        saveRoomAmenity(ctx.r2Std,   "Shower",            BATH,    "shower");

        saveRoomAmenity(ctx.r2Dlx,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r2Dlx,   "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r2Dlx,   "Smart TV",          TECH,    "tv");
        saveRoomAmenity(ctx.r2Dlx,   "Bathtub",           BATH,    "bath");
        saveRoomAmenity(ctx.r2Dlx,   "Minibar",           KITCH,   "minibar");

        saveRoomAmenity(ctx.r2Suite, "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r2Suite, "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r2Suite, "Smart TV",          TECH,    "tv");
        saveRoomAmenity(ctx.r2Suite, "Jacuzzi",           BATH,    "jacuzzi");
        saveRoomAmenity(ctx.r2Suite, "Kitchenette",       KITCH,   "kitchen");

        saveRoomAmenity(ctx.r3Std,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r3Std,   "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r3Std,   "TV",                TECH,    "tv");
        saveRoomAmenity(ctx.r3Std,   "Shower",            BATH,    "shower");

        saveRoomAmenity(ctx.r3Dlx,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r3Dlx,   "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r3Dlx,   "Smart TV",          TECH,    "tv");
        saveRoomAmenity(ctx.r3Dlx,   "Balcony",           COMFORT, "balcony");
        saveRoomAmenity(ctx.r3Dlx,   "Minibar",           KITCH,   "minibar");

        saveRoomAmenity(ctx.r4Std,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r4Std,   "Heating",           COMFORT, "heat");
        saveRoomAmenity(ctx.r4Std,   "TV",                TECH,    "tv");

        saveRoomAmenity(ctx.r4Dlx,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r4Dlx,   "Heating",           COMFORT, "heat");
        saveRoomAmenity(ctx.r4Dlx,   "Smart TV",          TECH,    "tv");
        saveRoomAmenity(ctx.r4Dlx,   "Bathtub",           BATH,    "bath");

        saveRoomAmenity(ctx.r5Std,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r5Std,   "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r5Std,   "Traditional Decor", COMFORT, "decor");

        saveRoomAmenity(ctx.r5Dlx,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r5Dlx,   "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r5Dlx,   "Jacuzzi",           BATH,    "jacuzzi");
        saveRoomAmenity(ctx.r5Dlx,   "Private Terrace",   COMFORT, "terrace");

        saveRoomAmenity(ctx.r6Std,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r6Std,   "Heating",           COMFORT, "heat");
        saveRoomAmenity(ctx.r6Std,   "TV",                TECH,    "tv");

        saveRoomAmenity(ctx.r6Dlx,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r6Dlx,   "Heating",           COMFORT, "heat");
        saveRoomAmenity(ctx.r6Dlx,   "Smart TV",          TECH,    "tv");
        saveRoomAmenity(ctx.r6Dlx,   "Bathtub",           BATH,    "bath");

        saveRoomAmenity(ctx.r7Std,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r7Std,   "Fan",               COMFORT, "fan");
        saveRoomAmenity(ctx.r7Std,   "TV",                TECH,    "tv");

        saveRoomAmenity(ctx.r7Dlx,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r7Dlx,   "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r7Dlx,   "Smart TV",          TECH,    "tv");
        saveRoomAmenity(ctx.r7Dlx,   "Shower",            BATH,    "shower");

        saveRoomAmenity(ctx.r8Std,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r8Std,   "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r8Std,   "Smart TV",          TECH,    "tv");

        saveRoomAmenity(ctx.r8Dlx,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r8Dlx,   "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r8Dlx,   "Smart TV",          TECH,    "tv");
        saveRoomAmenity(ctx.r8Dlx,   "Soaking Tub",       BATH,    "bath");

        saveRoomAmenity(ctx.r9Dorm,  "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r9Dorm,  "Locker Storage",    COMFORT, "locker");
        saveRoomAmenity(ctx.r9Dorm,  "Reading Light",     TECH,    "light");

        saveRoomAmenity(ctx.r9Pvt,   "Free WiFi",         TECH,    "wifi");
        saveRoomAmenity(ctx.r9Pvt,   "Air Conditioning",  COMFORT, "ac");
        saveRoomAmenity(ctx.r9Pvt,   "TV",                TECH,    "tv");
    }

    private void seedRoomAccessibility(SeedContext ctx) {
        saveRoomAccess(ctx.r1Std,   "Roll-in Shower",      true);
        saveRoomAccess(ctx.r1Std,   "Grab Bars",           true);
        saveRoomAccess(ctx.r1Dlx,   "Roll-in Shower",      true);
        saveRoomAccess(ctx.r1Dlx,   "Grab Bars",           true);
        saveRoomAccess(ctx.r1Suite, "Lowered Fixtures",    true);
        saveRoomAccess(ctx.r1Suite, "Wide Doorway",        true);
        saveRoomAccess(ctx.r1Fam,   "Grab Bars",           true);
        saveRoomAccess(ctx.r1Villa, "Ground Floor Access", true);
        saveRoomAccess(ctx.r2Std,   "Roll-in Shower",      true);
        saveRoomAccess(ctx.r2Std,   "Grab Bars",           true);
        saveRoomAccess(ctx.r2Dlx,   "Roll-in Shower",      true);
        saveRoomAccess(ctx.r2Suite, "Grab Bars",           true);
        saveRoomAccess(ctx.r3Std,   "Roll-in Shower",      true);
        saveRoomAccess(ctx.r3Dlx,   "Grab Bars",           true);
        saveRoomAccess(ctx.r4Std,   "Grab Bars",           true);
        saveRoomAccess(ctx.r6Std,   "Grab Bars",           true);
        saveRoomAccess(ctx.r6Dlx,   "Roll-in Shower",      true);
        saveRoomAccess(ctx.r7Std,   "Roll-in Shower",      false);
        saveRoomAccess(ctx.r9Dorm,  "Grab Bars",           false);
    }

    private void seedRoomPhotos(SeedContext ctx) {
        saveRoomPhoto(ctx.r1Std,   "https://images.unsplash.com/photo-1631049307264-da0ec9d70304", "Standard Room Overview",    1);
        saveRoomPhoto(ctx.r1Std,   "https://images.unsplash.com/photo-1590490360182-c33d57733427", "Bathroom",                  2);
        saveRoomPhoto(ctx.r1Dlx,   "https://images.unsplash.com/photo-1582719508461-905c673771fd", "Sea View Bedroom",          1);
        saveRoomPhoto(ctx.r1Dlx,   "https://images.unsplash.com/photo-1571896349842-33c89424de2d", "Deluxe Bathroom",           2);
        saveRoomPhoto(ctx.r1Suite, "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af", "Presidential Suite Living", 1);
        saveRoomPhoto(ctx.r1Suite, "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4", "Suite Bathroom",            2);
        saveRoomPhoto(ctx.r1Fam,   "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6", "Family Room Overview",      1);
        saveRoomPhoto(ctx.r2Std,   "https://images.unsplash.com/photo-1611892440504-42a792e24d32", "Classic Double",            1);
        saveRoomPhoto(ctx.r2Std,   "https://images.unsplash.com/photo-1631049307264-da0ec9d70304", "Standard Bath",             2);
        saveRoomPhoto(ctx.r2Dlx,   "https://images.unsplash.com/photo-1617098474202-0d0d7f60c56a", "Deluxe Room",               1);
        saveRoomPhoto(ctx.r3Std,   "https://images.unsplash.com/photo-1445019980597-93fa8acb246c", "Ocean View Bed",            1);
        saveRoomPhoto(ctx.r3Dlx,   "https://images.unsplash.com/photo-1602002418082-a4443e081dd1", "Beachfront Suite",          1);
        saveRoomPhoto(ctx.r4Std,   "https://images.unsplash.com/photo-1560185893-a55cbc8c57e8",   "Cozy Nordic Room",           1);
        saveRoomPhoto(ctx.r4Dlx,   "https://images.unsplash.com/photo-1595576508898-0ad5c879a061", "Nordic Deluxe",             1);
        saveRoomPhoto(ctx.r5Std,   "https://images.unsplash.com/photo-1512100356356-de1b84283e18", "Traditional Riad Room",     1);
        saveRoomPhoto(ctx.r5Dlx,   "https://images.unsplash.com/photo-1606402179428-a57976d71fa4", "Deluxe Riad",               1);
        saveRoomPhoto(ctx.r6Std,   "https://images.unsplash.com/photo-1525596662741-e94ff9f26de1", "London Classic Room",       1);
        saveRoomPhoto(ctx.r6Dlx,   "https://images.unsplash.com/photo-1455587734955-081b22074882", "Executive Double",          1);
        saveRoomPhoto(ctx.r7Std,   "https://images.unsplash.com/photo-1496417263034-38ec4f0b665a", "Cairo Budget Room",         1);
        saveRoomPhoto(ctx.r7Dlx,   "https://images.unsplash.com/photo-1578683010236-d716f9a3f461", "Nile View Room",            1);
        saveRoomPhoto(ctx.r8Std,   "https://images.unsplash.com/photo-1551538827-9c037cb4f32a",   "Tatami Room",                1);
        saveRoomPhoto(ctx.r8Dlx,   "https://images.unsplash.com/photo-1611892440504-42a792e24d32", "Zen Deluxe",                1);
        saveRoomPhoto(ctx.r9Dorm,  "https://images.unsplash.com/photo-1555854877-bab0e564b8d5",   "Mixed Dorm",                 1);
        saveRoomPhoto(ctx.r9Pvt,   "https://images.unsplash.com/photo-1540555700478-4be289fbecef", "Private Room",              1);
    }

    // =========================================================================
    // 4. CANCELLATION POLICIES
    // =========================================================================
    private void seedCancellationPolicies(SeedContext ctx) {
        ctx.cp1Flex   = saveCp(ctx.h1.getId(), ctx.r1Std.getId(),   "Flexible",       48, 100, "1.00", true,  "Full refund if cancelled 48h before check-in");
        ctx.cp1Mod    = saveCp(ctx.h1.getId(), ctx.r1Dlx.getId(),   "Moderate",       72,  50, "1.10", false, "50% refund if cancelled 72h before check-in");
        ctx.cp1Strict = saveCp(ctx.h1.getId(), ctx.r1Suite.getId(), "Non-refundable",  0,   0, "0.90", false, "No refund on cancellation — discounted rate");
        ctx.cp2Flex   = saveCp(ctx.h2.getId(), ctx.r2Std.getId(),   "Flexible",       48, 100, "1.00", true,  "Full refund if cancelled 48h before check-in");
        ctx.cp2Mod    = saveCp(ctx.h2.getId(), ctx.r2Dlx.getId(),   "Moderate",       72,  50, "1.10", false, "50% refund if cancelled 72h before");
        ctx.cp3Flex   = saveCp(ctx.h3.getId(), ctx.r3Std.getId(),   "Flexible",       24, 100, "1.00", true,  "Full refund if cancelled 24h before check-in");
        ctx.cp4Mod    = saveCp(ctx.h4.getId(), ctx.r4Std.getId(),   "Moderate",       48,  50, "1.00", true,  "50% refund if cancelled 48h before check-in");
        ctx.cp5Mod    = saveCp(ctx.h5.getId(), ctx.r5Std.getId(),   "Moderate",       72,  50, "1.00", true,  "50% refund if cancelled 72h before check-in");
        ctx.cp6Flex   = saveCp(ctx.h6.getId(), ctx.r6Std.getId(),   "Flexible",       48, 100, "1.00", true,  "Full refund if cancelled 48h before check-in");
        ctx.cp7Flex   = saveCp(ctx.h7.getId(), ctx.r7Std.getId(),   "Flexible",       24, 100, "1.00", true,  "Full refund if cancelled 24h before check-in");
        ctx.cp8Mod    = saveCp(ctx.h8.getId(), ctx.r8Std.getId(),   "Moderate",       48,  50, "1.00", true,  "50% refund if cancelled 48h before check-in");
        ctx.cp9Non    = saveCp(ctx.h9.getId(), ctx.r9Dorm.getId(),  "Non-refundable",  0,   0, "0.85", true,  "No refund — discounted hostel rate");

        log.info("  ✔ Cancellation policies seeded");
    }

    // =========================================================================
    // 5. PRICING RULES
    // =========================================================================
    private void seedPricingRules(SeedContext ctx) {
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r1Std.getId())  .ruleType(PricingRule.RuleType.WEEKDAY_WEEKEND).multiplier(new BigDecimal("1.20")).priority(1).isActive(true).dayOfWeek("FRIDAY,SATURDAY").build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r1Dlx.getId())  .ruleType(PricingRule.RuleType.WEEKDAY_WEEKEND).multiplier(new BigDecimal("1.25")).priority(1).isActive(true).dayOfWeek("FRIDAY,SATURDAY").build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r1Suite.getId()).ruleType(PricingRule.RuleType.SEASONAL)       .multiplier(new BigDecimal("1.40")).priority(2).isActive(true).startDate(LocalDate.of(2025,6,1)) .endDate(LocalDate.of(2025,8,31)).build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r1Suite.getId()).ruleType(PricingRule.RuleType.SPECIAL_EVENT)  .multiplier(new BigDecimal("1.50")).priority(3).isActive(true).startDate(LocalDate.of(2025,12,24)).endDate(LocalDate.of(2026,1,2)).build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r2Std.getId())  .ruleType(PricingRule.RuleType.WEEKDAY_WEEKEND).multiplier(new BigDecimal("1.15")).priority(1).isActive(true).dayOfWeek("FRIDAY,SATURDAY,SUNDAY").build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r2Dlx.getId())  .ruleType(PricingRule.RuleType.SEASONAL)       .multiplier(new BigDecimal("1.30")).priority(2).isActive(true).startDate(LocalDate.of(2025,7,1)) .endDate(LocalDate.of(2025,9,30)).build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r3Std.getId())  .ruleType(PricingRule.RuleType.WEEKDAY_WEEKEND).multiplier(new BigDecimal("1.20")).priority(1).isActive(true).dayOfWeek("FRIDAY,SATURDAY").build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r3Dlx.getId())  .ruleType(PricingRule.RuleType.SEASONAL)       .multiplier(new BigDecimal("1.35")).priority(2).isActive(true).startDate(LocalDate.of(2025,6,15)).endDate(LocalDate.of(2025,9,15)).build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r3Dlx.getId())  .ruleType(PricingRule.RuleType.SPECIAL_EVENT)  .multiplier(new BigDecimal("1.45")).priority(3).isActive(true).startDate(LocalDate.of(2025,12,27)).endDate(LocalDate.of(2026,1,3)).build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r4Std.getId())  .ruleType(PricingRule.RuleType.SEASONAL)       .multiplier(new BigDecimal("1.50")).priority(2).isActive(true).startDate(LocalDate.of(2025,12,1)).endDate(LocalDate.of(2026,2,28)).build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r4Dlx.getId())  .ruleType(PricingRule.RuleType.WEEKDAY_WEEKEND).multiplier(new BigDecimal("1.20")).priority(1).isActive(true).dayOfWeek("FRIDAY,SATURDAY").build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r5Dlx.getId())  .ruleType(PricingRule.RuleType.SEASONAL)       .multiplier(new BigDecimal("1.40")).priority(2).isActive(true).startDate(LocalDate.of(2025,3,1)) .endDate(LocalDate.of(2025,5,31)).build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r6Std.getId())  .ruleType(PricingRule.RuleType.WEEKDAY_WEEKEND).multiplier(new BigDecimal("1.15")).priority(1).isActive(true).dayOfWeek("FRIDAY,SATURDAY").build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r6Dlx.getId())  .ruleType(PricingRule.RuleType.SPECIAL_EVENT)  .multiplier(new BigDecimal("1.30")).priority(3).isActive(true).startDate(LocalDate.of(2025,12,31)).endDate(LocalDate.of(2026,1,1)).build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r8Std.getId())  .ruleType(PricingRule.RuleType.SEASONAL)       .multiplier(new BigDecimal("1.25")).priority(2).isActive(true).startDate(LocalDate.of(2025,3,20)).endDate(LocalDate.of(2025,4,5)).build());
        pricingRuleRepository.save(PricingRule.builder().roomId(ctx.r9Dorm.getId()) .ruleType(PricingRule.RuleType.WEEKDAY_WEEKEND).multiplier(new BigDecimal("1.10")).priority(1).isActive(true).dayOfWeek("FRIDAY,SATURDAY").build());

        log.info("  ✔ Pricing rules seeded");
    }

    // =========================================================================
    // 6. BOOKINGS
    // =========================================================================
    private void seedBookings(SeedContext ctx) {
        LocalDate today = LocalDate.now();

        // ── COMPLETED ─────────────────────────────────────────────────────────
        ctx.bk01 = saveBooking(ctx.c1.getId(),  ctx.h1.getId(), ctx.r1Std.getId(),    2,0, ld("2024-03-10"), ld("2024-03-14"), BookingStatus.COMPLETED,   596.00, 149.00, 1, ctx.cp1Flex.getId(),   null, null);
        ctx.bk02 = saveBooking(ctx.c2.getId(),  ctx.h2.getId(), ctx.r2Dlx.getId(),    2,1, ld("2024-03-15"), ld("2024-03-19"), BookingStatus.COMPLETED,   756.00, 189.00, 1, ctx.cp2Mod.getId(),    null, null);
        ctx.bk03 = saveBooking(ctx.c3.getId(),  ctx.h3.getId(), ctx.r3Dlx.getId(),    2,0, ld("2024-04-01"), ld("2024-04-05"), BookingStatus.COMPLETED,   796.00, 199.00, 1, ctx.cp3Flex.getId(),   null, null);
        ctx.bk04 = saveBooking(ctx.c4.getId(),  ctx.h4.getId(), ctx.r4Dlx.getId(),    1,0, ld("2024-04-10"), ld("2024-04-14"), BookingStatus.COMPLETED,   636.00, 159.00, 1, ctx.cp4Mod.getId(),    null, null);
        ctx.bk05 = saveBooking(ctx.c5.getId(),  ctx.h5.getId(), ctx.r5Dlx.getId(),    2,0, ld("2024-04-20"), ld("2024-04-24"), BookingStatus.COMPLETED,   720.00, 180.00, 1, ctx.cp5Mod.getId(),    null, null);
        ctx.bk06 = saveBooking(ctx.c6.getId(),  ctx.h6.getId(), ctx.r6Dlx.getId(),    2,0, ld("2024-05-05"), ld("2024-05-08"), BookingStatus.COMPLETED,   447.00, 149.00, 1, ctx.cp6Flex.getId(),   null, null);
        ctx.bk07 = saveBooking(ctx.c7.getId(),  ctx.h7.getId(), ctx.r7Dlx.getId(),    2,0, ld("2024-05-15"), ld("2024-05-18"), BookingStatus.COMPLETED,   285.00,  95.00, 1, ctx.cp7Flex.getId(),   null, null);
        ctx.bk08 = saveBooking(ctx.c8.getId(),  ctx.h8.getId(), ctx.r8Dlx.getId(),    2,0, ld("2024-06-01"), ld("2024-06-05"), BookingStatus.COMPLETED,   560.00, 140.00, 1, ctx.cp8Mod.getId(),    null, null);
        ctx.bk09 = saveBooking(ctx.c9.getId(),  ctx.h9.getId(), ctx.r9Pvt.getId(),    2,0, ld("2024-06-10"), ld("2024-06-13"), BookingStatus.COMPLETED,   225.00,  75.00, 1, ctx.cp9Non.getId(),    null, null);
        ctx.bk10 = saveBooking(ctx.c10.getId(), ctx.h1.getId(), ctx.r1Dlx.getId(),    2,1, ld("2024-06-20"), ld("2024-06-25"), BookingStatus.COMPLETED,  1245.00, 249.00, 1, ctx.cp1Mod.getId(),    null, null);
        ctx.bk11 = saveBooking(ctx.c11.getId(), ctx.h2.getId(), ctx.r2Suite.getId(),  2,0, ld("2024-07-01"), ld("2024-07-06"), BookingStatus.COMPLETED,  1600.00, 320.00, 1, ctx.cp2Flex.getId(),   null, null);
        ctx.bk12 = saveBooking(ctx.c12.getId(), ctx.h3.getId(), ctx.r3Suite.getId(),  3,1, ld("2024-07-10"), ld("2024-07-16"), BookingStatus.COMPLETED,  2100.00, 350.00, 1, ctx.cp3Flex.getId(),   null, null);
        ctx.bk13 = saveBooking(ctx.c13.getId(), ctx.h4.getId(), ctx.r4Suite.getId(),  2,0, ld("2024-07-20"), ld("2024-07-24"), BookingStatus.COMPLETED,  1120.00, 280.00, 1, ctx.cp4Mod.getId(),    null, null);
        ctx.bk14 = saveBooking(ctx.c14.getId(), ctx.h5.getId(), ctx.r5Villa.getId(),  4,2, ld("2024-08-01"), ld("2024-08-08"), BookingStatus.COMPLETED,  2730.00, 390.00, 1, ctx.cp5Mod.getId(),    null, null);
        ctx.bk15 = saveBooking(ctx.c15.getId(), ctx.h6.getId(), ctx.r6Suite.getId(),  2,1, ld("2024-08-15"), ld("2024-08-20"), BookingStatus.COMPLETED,  1300.00, 260.00, 1, ctx.cp6Flex.getId(),   null, null);
        ctx.bk16 = saveBooking(ctx.c1.getId(),  ctx.h8.getId(), ctx.r8Suite.getId(),  2,0, ld("2024-09-05"), ld("2024-09-10"), BookingStatus.COMPLETED,  1400.00, 280.00, 1, ctx.cp8Mod.getId(),    null, null);
        ctx.bk17 = saveBooking(ctx.c2.getId(),  ctx.h7.getId(), ctx.r7Std.getId(),    1,0, ld("2024-09-15"), ld("2024-09-18"), BookingStatus.COMPLETED,   195.00,  65.00, 1, ctx.cp7Flex.getId(),   null, null);
        ctx.bk18 = saveBooking(ctx.c3.getId(),  ctx.h1.getId(), ctx.r1Fam.getId(),    2,2, ld("2024-10-01"), ld("2024-10-06"), BookingStatus.COMPLETED,   995.00, 199.00, 1, ctx.cp1Flex.getId(),   null, null);

        // ── CANCELLED ─────────────────────────────────────────────────────────────
        ctx.bk19 = saveBooking(ctx.c4.getId(),  ctx.h2.getId(), ctx.r2Std.getId(),   2,0, ld("2024-11-10"), ld("2024-11-14"), BookingStatus.CANCELLED,   516.00, 129.00, 1, ctx.cp2Flex.getId(), LocalDateTime.of(2024,11,5,14,0),  "CUSTOMER");
        ctx.bk20 = saveBooking(ctx.c5.getId(),  ctx.h3.getId(), ctx.r3Std.getId(),   1,0, ld("2024-11-20"), ld("2024-11-23"), BookingStatus.CANCELLED,   357.00, 119.00, 1, ctx.cp3Flex.getId(), LocalDateTime.of(2024,11,18,10,0), "CUSTOMER");
        ctx.bk21 = saveBooking(ctx.c6.getId(),  ctx.h4.getId(), ctx.r4Std.getId(),   2,0, ld("2024-12-05"), ld("2024-12-08"), BookingStatus.CANCELLED,   297.00,  99.00, 1, ctx.cp4Mod.getId(),  LocalDateTime.of(2024,12,3,9,0),   "CUSTOMER");
        ctx.bk22 = saveBooking(ctx.c7.getId(),  ctx.h9.getId(), ctx.r9Dorm.getId(),  1,0, ld("2024-12-20"), ld("2024-12-22"), BookingStatus.CANCELLED,    56.00,  28.00, 1, ctx.cp9Non.getId(),  LocalDateTime.of(2024,12,18,8,0),  "CUSTOMER");
        // ── CONFIRMED (current / upcoming) ────────────────────────────────────
        ctx.bk23 = saveBooking(ctx.c8.getId(),  ctx.h1.getId(), ctx.r1Std.getId(),   2,0, today,                today.plusDays(3),  BookingStatus.CONFIRMED,   447.00, 149.00, 1, ctx.cp1Flex.getId(),   null, null);
        ctx.bk24 = saveBooking(ctx.c9.getId(),  ctx.h2.getId(), ctx.r2Dlx.getId(),   2,1, today,                today.plusDays(4),  BookingStatus.CONFIRMED,   756.00, 189.00, 1, ctx.cp2Mod.getId(),    null, null);
        ctx.bk25 = saveBooking(ctx.c10.getId(), ctx.h3.getId(), ctx.r3Std.getId(),   1,0, today.plusDays(7),    today.plusDays(10), BookingStatus.CONFIRMED,   357.00, 119.00, 1, ctx.cp3Flex.getId(),   null, null);
        ctx.bk26 = saveBooking(ctx.c11.getId(), ctx.h4.getId(), ctx.r4Dlx.getId(),   2,0, today.plusDays(14),   today.plusDays(17), BookingStatus.CONFIRMED,   477.00, 159.00, 1, ctx.cp4Mod.getId(),    null, null);
        ctx.bk27 = saveBooking(ctx.c12.getId(), ctx.h5.getId(), ctx.r5Std.getId(),   2,0, today.plusDays(21),   today.plusDays(25), BookingStatus.CONFIRMED,   440.00, 110.00, 1, ctx.cp5Mod.getId(),    null, null);
        ctx.bk28 = saveBooking(ctx.c13.getId(), ctx.h6.getId(), ctx.r6Dlx.getId(),   2,0, today.plusDays(30),   today.plusDays(33), BookingStatus.CONFIRMED,   447.00, 149.00, 1, ctx.cp6Flex.getId(),   null, null);
        ctx.bk29 = saveBooking(ctx.c14.getId(), ctx.h7.getId(), ctx.r7Dlx.getId(),   2,0, today.plusDays(10),   today.plusDays(13), BookingStatus.CONFIRMED,   285.00,  95.00, 1, ctx.cp7Flex.getId(),   null, null);
        ctx.bk30 = saveBooking(ctx.c15.getId(), ctx.h8.getId(), ctx.r8Std.getId(),   1,0, today.plusDays(5),    today.plusDays(8),  BookingStatus.CONFIRMED,   270.00,  90.00, 1, ctx.cp8Mod.getId(),    null, null);
        ctx.bk31 = saveBooking(ctx.c1.getId(),  ctx.h9.getId(), ctx.r9Dorm.getId(),  1,0, today.plusDays(45),   today.plusDays(47), BookingStatus.CONFIRMED,    56.00,  28.00, 1, ctx.cp9Non.getId(),    null, null);
        ctx.bk32 = saveBooking(ctx.c2.getId(),  ctx.h1.getId(), ctx.r1Dlx.getId(),   2,0, today.plusDays(60),   today.plusDays(64), BookingStatus.CONFIRMED,   996.00, 249.00, 1, ctx.cp1Mod.getId(),    null, null);
        ctx.bk33 = saveBooking(ctx.c3.getId(),  ctx.h2.getId(), ctx.r2Std.getId(),   1,0, today.plusDays(35),   today.plusDays(37), BookingStatus.CONFIRMED,   258.00, 129.00, 1, ctx.cp2Flex.getId(),   null, null);
        ctx.bk34 = saveBooking(ctx.c4.getId(),  ctx.h3.getId(), ctx.r3Fam.getId(),   2,2, today.plusDays(40),   today.plusDays(44), BookingStatus.CONFIRMED,   716.00, 179.00, 1, ctx.cp3Flex.getId(),   null, null);
        ctx.bk35 = saveBooking(ctx.c5.getId(),  ctx.h5.getId(), ctx.r5Villa.getId(), 4,0, today.plusDays(90),   today.plusDays(95), BookingStatus.CONFIRMED,  1950.00, 390.00, 1, ctx.cp5Mod.getId(),    null, null);
        ctx.bk36 = saveBooking(ctx.c6.getId(),  ctx.h8.getId(), ctx.r8Dlx.getId(),   2,0, today.plusDays(55),   today.plusDays(58), BookingStatus.CONFIRMED,   420.00, 140.00, 1, ctx.cp8Mod.getId(),    null, null);
        ctx.bk37 = saveBooking(ctx.c7.getId(),  ctx.h6.getId(), ctx.r6Suite.getId(), 2,1, today.plusDays(25),   today.plusDays(29), BookingStatus.CONFIRMED,  1040.00, 260.00, 1, ctx.cp6Flex.getId(),   null, null);
        ctx.bk38 = saveBooking(ctx.c8.getId(),  ctx.h4.getId(), ctx.r4Suite.getId(), 2,0, today.plusDays(20),   today.plusDays(24), BookingStatus.CONFIRMED,  1120.00, 280.00, 1, ctx.cp4Mod.getId(),    null, null);
        ctx.bk39 = saveBooking(ctx.c9.getId(),  ctx.h1.getId(), ctx.r1Suite.getId(), 3,1, today.plusDays(75),   today.plusDays(80), BookingStatus.CONFIRMED,  1995.00, 399.00, 1, ctx.cp1Strict.getId(), null, null);
        ctx.bk40 = saveBooking(ctx.c10.getId(), ctx.h9.getId(), ctx.r9Pvt.getId(),   2,0, today.plusDays(15),   today.plusDays(18), BookingStatus.CONFIRMED,   225.00,  75.00, 1, ctx.cp9Non.getId(),    null, null);

        log.info("  ✔ Bookings seeded");
    }

    // =========================================================================
    // 7. ROOM AVAILABILITY
    // =========================================================================
    private void seedRoomAvailability(SeedContext ctx) {
        LocalDate today = LocalDate.now();

        // Past completed bookings
        saveAvailability(ctx.r1Std.getId(),  LocalDate.of(2024,3,10), 1, RoomAvailability.BlockedReason.BOOKING,       ctx.bk01.getId());
        saveAvailability(ctx.r1Std.getId(),  LocalDate.of(2024,3,11), 1, RoomAvailability.BlockedReason.BOOKING,       ctx.bk01.getId());
        saveAvailability(ctx.r1Std.getId(),  LocalDate.of(2024,3,12), 1, RoomAvailability.BlockedReason.BOOKING,       ctx.bk01.getId());
        saveAvailability(ctx.r1Std.getId(),  LocalDate.of(2024,3,13), 1, RoomAvailability.BlockedReason.BOOKING,       ctx.bk01.getId());

        saveAvailability(ctx.r8Dlx.getId(),  LocalDate.of(2024,6,1),  1, RoomAvailability.BlockedReason.BOOKING,       ctx.bk08.getId());
        saveAvailability(ctx.r8Dlx.getId(),  LocalDate.of(2024,6,2),  1, RoomAvailability.BlockedReason.BOOKING,       ctx.bk08.getId());
        saveAvailability(ctx.r8Dlx.getId(),  LocalDate.of(2024,6,3),  1, RoomAvailability.BlockedReason.BOOKING,       ctx.bk08.getId());
        saveAvailability(ctx.r8Dlx.getId(),  LocalDate.of(2024,6,4),  1, RoomAvailability.BlockedReason.BOOKING,       ctx.bk08.getId());

        // Current confirmed booking (bk23)
        saveAvailability(ctx.r1Std.getId(),  today,             1, RoomAvailability.BlockedReason.BOOKING,       ctx.bk23.getId());
        saveAvailability(ctx.r1Std.getId(),  today.plusDays(1), 1, RoomAvailability.BlockedReason.BOOKING,       ctx.bk23.getId());
        saveAvailability(ctx.r1Std.getId(),  today.plusDays(2), 1, RoomAvailability.BlockedReason.BOOKING,       ctx.bk23.getId());

        // Upcoming confirmed booking (bk25)
        saveAvailability(ctx.r3Std.getId(),  today.plusDays(7), 1, RoomAvailability.BlockedReason.BOOKING,       ctx.bk25.getId());
        saveAvailability(ctx.r3Std.getId(),  today.plusDays(8), 1, RoomAvailability.BlockedReason.BOOKING,       ctx.bk25.getId());
        saveAvailability(ctx.r3Std.getId(),  today.plusDays(9), 1, RoomAvailability.BlockedReason.BOOKING,       ctx.bk25.getId());

        // Maintenance / manager blocks
        saveAvailability(ctx.r1Villa.getId(), today.plusDays(3), 1, RoomAvailability.BlockedReason.MAINTENANCE,   null);
        saveAvailability(ctx.r5Villa.getId(), today.plusDays(2), 1, RoomAvailability.BlockedReason.MANAGER_BLOCK, null);

        log.info("  ✔ Room availability seeded");
    }

    // =========================================================================
    // 8. PAYMENTS
    // =========================================================================
    private void seedPayments(SeedContext ctx) {
        LocalDateTime now = LocalDateTime.now();

        // Completed bookings — PAID
        savePayment(ctx.bk01.getId(), ctx.c1.getId(),    596.00, "CREDIT_CARD",   PaymentStatus.PAID,     ldt("2024-03-01T09:05"), null,   null);
        savePayment(ctx.bk02.getId(), ctx.c2.getId(),    756.00, "CREDIT_CARD",   PaymentStatus.PAID,     ldt("2024-03-05T10:10"), null,   null);
        savePayment(ctx.bk03.getId(), ctx.c3.getId(),    796.00, "PAYPAL",        PaymentStatus.PAID,     ldt("2024-03-22T11:15"), null,   null);
        savePayment(ctx.bk04.getId(), ctx.c4.getId(),    636.00, "DEBIT_CARD",    PaymentStatus.PAID,     ldt("2024-04-01T08:20"), null,   null);
        savePayment(ctx.bk05.getId(), ctx.c5.getId(),    720.00, "CREDIT_CARD",   PaymentStatus.PAID,     ldt("2024-04-10T12:10"), null,   null);
        savePayment(ctx.bk06.getId(), ctx.c6.getId(),    447.00, "CREDIT_CARD",   PaymentStatus.PAID,     ldt("2024-04-25T09:20"), null,   null);
        savePayment(ctx.bk07.getId(), ctx.c7.getId(),    285.00, "CASH",          PaymentStatus.PAID,     ldt("2024-05-05T10:30"), null,   null);
        savePayment(ctx.bk08.getId(), ctx.c8.getId(),    560.00, "CREDIT_CARD",   PaymentStatus.PAID,     ldt("2024-05-20T11:10"), null,   null);
        savePayment(ctx.bk09.getId(), ctx.c9.getId(),    225.00, "DEBIT_CARD",    PaymentStatus.PAID,     ldt("2024-06-01T10:05"), null,   null);
        savePayment(ctx.bk10.getId(), ctx.c10.getId(),  1245.00, "CREDIT_CARD",   PaymentStatus.PAID,     ldt("2024-06-10T08:15"), null,   null);
        savePayment(ctx.bk11.getId(), ctx.c11.getId(),  1600.00, "BANK_TRANSFER", PaymentStatus.PAID,     ldt("2024-06-20T12:05"), null,   null);
        savePayment(ctx.bk12.getId(), ctx.c12.getId(),  2100.00, "CREDIT_CARD",   PaymentStatus.PAID,     ldt("2024-06-30T10:10"), null,   null);
        savePayment(ctx.bk13.getId(), ctx.c13.getId(),  1120.00, "PAYPAL",        PaymentStatus.PAID,     ldt("2024-07-10T09:15"), null,   null);
        savePayment(ctx.bk14.getId(), ctx.c14.getId(),  2730.00, "CREDIT_CARD",   PaymentStatus.PAID,     ldt("2024-07-20T11:10"), null,   null);
        savePayment(ctx.bk15.getId(), ctx.c15.getId(),  1300.00, "DEBIT_CARD",    PaymentStatus.PAID,     ldt("2024-08-05T10:15"), null,   null);
        savePayment(ctx.bk16.getId(), ctx.c1.getId(),   1400.00, "CREDIT_CARD",   PaymentStatus.PAID,     ldt("2024-08-25T12:20"), null,   null);
        savePayment(ctx.bk17.getId(), ctx.c2.getId(),    195.00, "CASH",          PaymentStatus.PAID,     ldt("2024-09-05T10:10"), null,   null);
        savePayment(ctx.bk18.getId(), ctx.c3.getId(),    995.00, "CREDIT_CARD",   PaymentStatus.PAID,     ldt("2024-09-20T09:15"), null,   null);
        // Cancelled — refunded or failed
        savePayment(ctx.bk19.getId(), ctx.c4.getId(),   516.00, "CREDIT_CARD",   PaymentStatus.REFUNDED, ldt("2024-10-25T11:05"), 516.00, ldt("2024-11-05T15:00"));
        savePayment(ctx.bk20.getId(), ctx.c5.getId(),   357.00, "PAYPAL",        PaymentStatus.REFUNDED, ldt("2024-11-10T09:05"), 357.00, ldt("2024-11-18T11:00"));
        savePayment(ctx.bk21.getId(), ctx.c6.getId(),   297.00, "CREDIT_CARD",   PaymentStatus.REFUNDED, ldt("2024-11-20T12:10"), 148.50, ldt("2024-12-04T10:00"));
        savePayment(ctx.bk22.getId(), ctx.c7.getId(),    56.00, "DEBIT_CARD",    PaymentStatus.FAILED,   null,                    null,   null);
        // Confirmed — PAID
        savePayment(ctx.bk23.getId(), ctx.c8.getId(),   447.00, "CREDIT_CARD",   PaymentStatus.PAID, now.minusDays(5), null, null);
        savePayment(ctx.bk24.getId(), ctx.c9.getId(),   756.00, "CREDIT_CARD",   PaymentStatus.PAID, now.minusDays(3), null, null);
        savePayment(ctx.bk25.getId(), ctx.c10.getId(),  357.00, "PAYPAL",        PaymentStatus.PAID, now.minusDays(2), null, null);
        savePayment(ctx.bk26.getId(), ctx.c11.getId(),  477.00, "DEBIT_CARD",    PaymentStatus.PAID, now.minusDays(1), null, null);
        savePayment(ctx.bk27.getId(), ctx.c12.getId(),  440.00, "CREDIT_CARD",   PaymentStatus.PAID, now,              null, null);
        savePayment(ctx.bk28.getId(), ctx.c13.getId(),  447.00, "BANK_TRANSFER", PaymentStatus.PAID, now,              null, null);
        savePayment(ctx.bk29.getId(), ctx.c14.getId(),  285.00, "CREDIT_CARD",   PaymentStatus.PAID, now.minusDays(4), null, null);
        savePayment(ctx.bk30.getId(), ctx.c15.getId(),  270.00, "DEBIT_CARD",    PaymentStatus.PAID, now.minusDays(2), null, null);
        savePayment(ctx.bk31.getId(), ctx.c1.getId(),    56.00, "CREDIT_CARD",   PaymentStatus.PAID, now,              null, null);
        savePayment(ctx.bk32.getId(), ctx.c2.getId(),   996.00, "CREDIT_CARD",   PaymentStatus.PAID, now,              null, null);
        savePayment(ctx.bk33.getId(), ctx.c3.getId(),   258.00, "PAYPAL",        PaymentStatus.PAID, now,              null, null);
        savePayment(ctx.bk34.getId(), ctx.c4.getId(),   716.00, "CREDIT_CARD",   PaymentStatus.PAID, now,              null, null);
        savePayment(ctx.bk35.getId(), ctx.c5.getId(),  1950.00, "BANK_TRANSFER", PaymentStatus.PAID, now,              null, null);
        savePayment(ctx.bk36.getId(), ctx.c6.getId(),   420.00, "CREDIT_CARD",   PaymentStatus.PAID, now,              null, null);
        savePayment(ctx.bk37.getId(), ctx.c7.getId(),  1040.00, "DEBIT_CARD",    PaymentStatus.PAID, now,              null, null);
        savePayment(ctx.bk38.getId(), ctx.c8.getId(),  1120.00, "CREDIT_CARD",   PaymentStatus.PAID, now,              null, null);
        savePayment(ctx.bk39.getId(), ctx.c9.getId(),  1995.00, "CREDIT_CARD",   PaymentStatus.PAID, now,              null, null);
        savePayment(ctx.bk40.getId(), ctx.c10.getId(),  225.00, "PAYPAL",        PaymentStatus.PAID, now,              null, null);

        log.info("  ✔ Payments seeded");
    }

    // =========================================================================
    // 9. REVIEWS — only for COMPLETED bookings (bk01–bk18)
    // =========================================================================
    private void seedReviews(SeedContext ctx) {
        saveReview(ctx.bk01, ctx.c1,  ctx.h1, "Absolutely stunning stay!",         "The Grand Azure exceeded every expectation. The infinity pool at night is magical and the staff were incredibly attentive.",             5,5,5,5,5,4, 4.8, "Thank you so much for your kind words! We hope to welcome you back soon.");
        saveReview(ctx.bk02, ctx.c2,  ctx.h2, "Romantic Roman escape",             "Bella Vista is the perfect base for exploring Rome. The rooftop bar with city views at sunset is unbeatable.",                          5,5,4,5,5,4, 4.6, "Grazie mille! We are delighted you enjoyed your stay.");
        saveReview(ctx.bk03, ctx.c3,  ctx.h3, "Paradise found on South Beach",     "Loved the beachfront location and the breakfast was included. The pool area was a bit crowded on weekends but overall fantastic.",       4,5,4,5,4,4, 4.3, null);
        saveReview(ctx.bk04, ctx.c4,  ctx.h4, "Cozy Scandinavian charm",           "The sauna after a long day exploring Stockholm was absolutely perfect. Clean, quiet, and the staff were helpful.",                       4,5,4,4,4,4, 4.2, null);
        saveReview(ctx.bk05, ctx.c5,  ctx.h5, "Magical Moroccan experience",       "Waking up to the smell of jasmine in a traditional riad is unforgettable. The private pool was a highlight.",                           5,5,5,4,5,4, 4.7, "We are so happy you enjoyed the riad experience!");
        saveReview(ctx.bk06, ctx.c6,  ctx.h6, "Great London base",                 "Perfectly located near Regent's Park. The rooms are compact but clean. Good value for central London.",                                 4,4,3,5,4,4, 4.0, null);
        saveReview(ctx.bk07, ctx.c7,  ctx.h7, "Authentic Cairo experience",        "You cannot beat the view from the rooftop terrace at sunset with the city spread below. Simple but comfortable.",                       4,3,3,5,4,5, 4.0, null);
        saveReview(ctx.bk08, ctx.c8,  ctx.h8, "Zen tranquility in Shinjuku",       "The hot spring bath in the evening was deeply relaxing. Staff were professional and the room was immaculate.",                          5,5,5,4,5,4, 4.7, "Thank you! We hope the onsen brought you peace and rest.");
        saveReview(ctx.bk09, ctx.c9,  ctx.h9, "Best hostel value in Barcelona",    "Super clean dorm, great location, friendly staff. Meet so many cool travelers. The social lounge is great for meeting people.",          4,4,3,5,4,5, 4.2, null);
        saveReview(ctx.bk10, ctx.c10, ctx.h1, "Luxury beyond expectations",        "The deluxe sea view room with private balcony was breathtaking. Service was top-notch from check-in to check-out.",                     5,5,5,5,5,4, 4.8, "It was an absolute pleasure hosting you. Please visit us again!");
        saveReview(ctx.bk11, ctx.c11, ctx.h2, "Suite life in Rome",                "The terrace suite with a private terrace overlooking Rome was just incredible. Worth every penny.",                                      5,5,5,5,5,4, 4.8, null);
        saveReview(ctx.bk12, ctx.c12, ctx.h3, "Amazing family beach holiday",      "The penthouse suite was epic for our family. Kids loved the pool and beach access. A truly memorable trip.",                             5,5,5,5,4,4, 4.7, "What a wonderful family trip! Hope to welcome you all back.");
        saveReview(ctx.bk13, ctx.c13, ctx.h4, "Perfect Nordic suite",              "The Nordic Suite was spacious and beautifully decorated. The sauna included access was a great touch.",                                  4,5,5,4,4,4, 4.4, null);
        saveReview(ctx.bk14, ctx.c14, ctx.h5, "An unforgettable desert escape",    "The private villa was our own little paradise. The Moroccan spa treatments were heavenly and the food was exceptional.",                 5,5,5,4,5,4, 4.8, "We are so glad you experienced the true spirit of Morocco.");
        saveReview(ctx.bk15, ctx.c15, ctx.h6, "Stylish London stay",               "The Hyde Park Suite is elegant and spacious. Great for families. Slightly pricey but good value for the quality.",                      4,5,5,5,4,3, 4.4, null);
        saveReview(ctx.bk16, ctx.c1,  ctx.h8, "Tokyo on another level",            "The Onsen Suite with a private hot spring was an unreal experience. Could not have asked for more in Tokyo.",                           5,5,5,5,5,4, 4.8, "Thank you for choosing our onsen suite! Arigato gozaimasu!");
        saveReview(ctx.bk17, ctx.c2,  ctx.h7, "Simple and central in Cairo",       "Clean room, great location near the Egyptian Museum. The breakfast was basic but the rooftop terrace was lovely.",                      3,3,3,5,4,4, 3.7, null);
        saveReview(ctx.bk18, ctx.c3,  ctx.h1, "Wonderful family holiday in Dubai", "The family room was spacious and the kids club was excellent. My children talked about it for weeks after.",                             5,5,5,5,5,4, 4.8, "We love welcoming families! Your children were a delight.");

        log.info("  ✔ Reviews seeded");
    }

    // =========================================================================
    // 10. SAVED HOTELS
    // =========================================================================
    private void seedSavedHotels(SeedContext ctx) {
        saveSaved(ctx.c1.getId(),  ctx.h2.getId(), "Want to try Rome next summer");
        saveSaved(ctx.c1.getId(),  ctx.h5.getId(), "Desert experience for anniversary trip");
        saveSaved(ctx.c2.getId(),  ctx.h1.getId(), "Dubai looks amazing — bucket list");
        saveSaved(ctx.c2.getId(),  ctx.h8.getId(), "Always wanted to visit Tokyo");
        saveSaved(ctx.c3.getId(),  ctx.h4.getId(), "Stockholm for New Year maybe?");
        saveSaved(ctx.c4.getId(),  ctx.h6.getId(), "London for a work trip");
        saveSaved(ctx.c5.getId(),  ctx.h9.getId(), "Barcelona with friends next spring");
        saveSaved(ctx.c6.getId(),  ctx.h3.getId(), "Miami Beach holiday sounds perfect");
        saveSaved(ctx.c7.getId(),  ctx.h2.getId(), "Rome for my honeymoon");
        saveSaved(ctx.c8.getId(),  ctx.h1.getId(), "Luxury Dubai stay someday");
        saveSaved(ctx.c9.getId(),  ctx.h5.getId(), "Marrakech escape for winter");
        saveSaved(ctx.c10.getId(), ctx.h8.getId(), "Tokyo zen retreat — need this");
        saveSaved(ctx.c11.getId(), ctx.h3.getId(), "Miami beach holiday");
        saveSaved(ctx.c12.getId(), ctx.h6.getId(), "London sightseeing trip");
        saveSaved(ctx.c13.getId(), ctx.h1.getId(), "Dubai shopping and beach");

        log.info("  ✔ Saved hotels seeded");
    }

    // =========================================================================
    // 11. NOTIFICATIONS
    // =========================================================================
    private void seedNotifications(SeedContext ctx) {
        LocalDateTime now = LocalDateTime.now();

        saveNotification(ctx.c1.getId(),     "Booking Confirmed — Grand Azure Hotel",
                "Dear Ahmed, your booking at Grand Azure Hotel (Dubai) from 10 Mar to 14 Mar 2024 has been confirmed. We look forward to welcoming you!",
                NotificationType.BOOKING_CONFIRMED, NotificationStatus.SENT);

        saveNotification(ctx.c2.getId(),     "Booking Confirmed — Bella Vista Resort",
                "Dear Nour, your booking at Bella Vista Resort (Rome) from 15 Mar to 19 Mar 2024 has been confirmed.",
                NotificationType.BOOKING_CONFIRMED, NotificationStatus.SENT);

        saveNotification(ctx.c8.getId(),     "Booking Confirmed — Grand Azure Hotel",
                "Dear Hassan, your booking at Grand Azure Hotel (Dubai) check-in today has been confirmed. We look forward to welcoming you!",
                NotificationType.BOOKING_CONFIRMED, NotificationStatus.SENT);

        saveNotification(ctx.c9.getId(),     "Booking Confirmed — Bella Vista Resort",
                "Dear Mariam, your booking at Bella Vista Resort (Rome) check-in today has been confirmed.",
                NotificationType.BOOKING_CONFIRMED, NotificationStatus.SENT);

        saveNotification(ctx.c10.getId(),    "Booking Confirmed — Sunridge Boutique Inn",
                "Dear Tariq, your upcoming booking at Sunridge Boutique Inn (Miami) has been confirmed.",
                NotificationType.BOOKING_CONFIRMED, NotificationStatus.SENT);

        saveNotification(ctx.c1.getId(),     "Payment Received — AED 596.00",
                "Dear Ahmed, we have received your payment of AED 596.00 for your upcoming stay at Grand Azure Hotel. Thank you!",
                NotificationType.PAYMENT_RECEIVED, NotificationStatus.SENT);

        saveNotification(ctx.c2.getId(),     "Payment Received — EUR 756.00",
                "Dear Nour, your payment of EUR 756.00 for Bella Vista Resort has been successfully processed.",
                NotificationType.PAYMENT_RECEIVED, NotificationStatus.SENT);

        saveNotification(ctx.c8.getId(),     "Payment Received — USD 447.00",
                "Dear Hassan, your payment of USD 447.00 has been successfully processed.",
                NotificationType.PAYMENT_RECEIVED, NotificationStatus.SENT);

        saveNotification(ctx.c4.getId(),     "Booking Cancelled — Bella Vista Resort",
                "Dear Omar, your booking at Bella Vista Resort (Rome) from 10 Nov to 14 Nov 2024 has been cancelled. Your refund of EUR 516.00 will be processed within 5-7 business days.",
                NotificationType.BOOKING_CANCELLED, NotificationStatus.SENT);

        saveNotification(ctx.c5.getId(),     "Booking Cancelled — Sunridge Boutique Inn",
                "Dear Yasmin, your booking at Sunridge Boutique Inn (Miami) has been cancelled. A full refund will be processed shortly.",
                NotificationType.BOOKING_CANCELLED, NotificationStatus.SENT);

        saveNotification(ctx.c7.getId(),     "Payment Failed — Barcelona Hostel Central",
                "Dear Dina, unfortunately your payment for Barcelona Hostel Central could not be processed. Please update your payment details and try again.",
                NotificationType.PAYMENT_FAILED, NotificationStatus.SENT);

        saveNotification(ctx.uAdmin1.getId(), "New Hotel Booking — Grand Azure",
                "A new booking has been made at Grand Azure Hotel. Customer: Hassan Al-Amin. Check-in: today.",
                NotificationType.BOOKING_CONFIRMED, NotificationStatus.SENT);

        saveNotification(ctx.uAdmin2.getId(), "New Booking Notification",
                "A new booking has been created at Bella Vista Resort by customer Mariam Youssef.",
                NotificationType.BOOKING_CONFIRMED, NotificationStatus.SENT);

        log.info("  ✔ Notifications seeded");
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    private User saveUser(String fullName, String email, UserRole role) {

        String firstName = fullName.split(" ")[0];
        String usernamePart = firstName.toLowerCase();
        String rawPassword = usernamePart + "123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .name(fullName)
                .email(email)
                .password(encodedPassword)
                .role(role)
                .build();

        return userRepository.save(user);
    }

    private Customer saveCustomer(User user, String dob, String nationality, String phone) {
        return customerRepository.save(Customer.builder()
                .user(user)
                .dateOfBirth(LocalDate.parse(dob))
                .nationality(nationality)
                .phone(phone)
                .build());
    }

    private Hotel saveHotel(String name, String address, String city, String countryCode,
                            double lat, double lng, int stars, HotelType type, HotelManager manager) {
        return hotelRepository.saveAndFlush(Hotel.builder()
                .name(name).address(address).city(city).countryCode(countryCode)
                .latitude(lat).longitude(lng).starRating(stars)
                .status(HotelStatus.ACTIVE).type(type).manager(manager)
                .build());
    }

    private void saveLocation(Hotel hotel, String country, String city, String state,
                              String address, String zip, double lat, double lng) {
        locationRepository.save(Location.builder()
                .hotel(hotel)
                .country(country).city(city).state(state)
                .address(address).zipCode(zip)
                .latitude(lat).longitude(lng)
                .build());
    }

    private void saveHotelAmenity(Hotel hotel, String name, AmenityCategory category, String icon) {
        hotelAmenityRepository.save(HotelAmenity.builder()
                .hotel(hotel).name(name).category(category).icon(icon).build());
    }

    private void saveHotelAccess(Hotel hotel, String feature, AccessibilityLevel level, String desc) {
        hotelAccessibilityRepository.save(HotelAccessibility.builder()
                .hotel(hotel).feature(feature).level(level).description(desc).build());
    }

    private void saveHotelPhoto(Hotel hotel, String url, String caption, boolean isCover, int order) {
        hotelPhotoRepository.save(HotelPhoto.builder()
                .hotel(hotel).url(url).caption(caption).isCover(isCover).order(order).build());
    }

    private void saveNearby(Hotel hotel, String name, NearbyPlaceType type, BigDecimal distanceKm) {
        nearbyPlaceRepository.save(NearbyPlace.builder()
                .hotelId(hotel.getId()).name(name).type(type).distanceKm(distanceKm).build());
    }

    private Room saveRoom(Hotel hotel, String name, RoomType type, BedType bedType,
                          double price, int qty, double size, int floor,
                          int maxAdults, int maxChildren, RoomView view) {
        return roomRepository.saveAndFlush(Room.builder()
                .hotelId(hotel.getId())   // ← set the UUID column directly
                .name(name)
                .type(type)
                .bedType(bedType)
                .price(BigDecimal.valueOf(price))
                .quantity(qty)
                .sizeSqm(BigDecimal.valueOf(size))
                .floor(floor)
                .maxAdults(maxAdults)
                .maxChildren(maxChildren)
                .view(view)
                .isActive(true)
                .build());
    }

    private void saveRoomAmenity(Room room, String name,
                                 com.HotelBook.HotelBooking.RoomAmenity.AmenityCategory category, String icon) {
        roomAmenityRepository.save(RoomAmenity.builder()
                .room(room).name(name).category(category).icon(icon).build());
    }

    private void saveRoomAccess(Room room, String feature, boolean isAvailable) {
        roomAccessibilityRepository.save(RoomAccessibility.builder()
                .room(room).feature(feature).isAvailable(isAvailable).build());
    }

    private void saveRoomPhoto(Room room, String url, String caption, int order) {
        roomPhotoRepository.save(RoomPhoto.builder()
                .room(room).url(url).caption(caption).displayOrder(order).build());
    }

    private CancellationPolicy saveCp(java.util.UUID hotelId, java.util.UUID roomId,
                                      String tierName, int deadlineHours, int refundPct,
                                      String multiplier, boolean isDefault, String description) {
        return cancellationPolicyRepository.save(CancellationPolicy.builder()
                .hotelId(hotelId).roomId(roomId)
                .tierName(tierName).deadlineHours(deadlineHours)
                .refundPercentage(refundPct)
                .priceMultiplier(new BigDecimal(multiplier))
                .isDefault(isDefault).description(description)
                .build());
    }

    private Booking saveBooking(java.util.UUID customerId, java.util.UUID hotelId, java.util.UUID roomId,
                                int adults, int children,
                                LocalDate checkIn, LocalDate checkOut,
                                BookingStatus status,
                                double totalPrice, double pricePerNight, int roomCount,
                                java.util.UUID cancellationPolicyId,
                                LocalDateTime cancelledAt, String cancelledBy) {
        return bookingRepository.save(Booking.builder()
                .customerId(customerId).hotelId(hotelId).roomId(roomId)
                .adults(adults).children(children)
                .checkInDate(checkIn).checkOutDate(checkOut)
                .status(status)
                .totalPrice(BigDecimal.valueOf(totalPrice))
                .pricePerNight(BigDecimal.valueOf(pricePerNight))
                .roomCount(roomCount)
                .cancellationPolicyId(cancellationPolicyId)
                .cancelledAt(cancelledAt)
                .cancelledBy(cancelledBy)
                .build());
    }

    private void saveAvailability(java.util.UUID roomId, LocalDate date, int count,
                                  RoomAvailability.BlockedReason reason, java.util.UUID bookingId) {
        roomAvailabilityRepository.save(RoomAvailability.builder()
                .roomId(roomId).date(date)
                .blockedCount(count).blockedReason(reason)
                .bookingId(bookingId)
                .build());
    }

    private void savePayment(java.util.UUID bookingId, java.util.UUID customerId,
                             double amount, String method,
                             PaymentStatus status, LocalDateTime paidAt,
                             Double refundAmount, LocalDateTime refundedAt) {
        paymentRepository.save(Payment.builder()
                .bookingId(bookingId).customerId(customerId)
                .amount(BigDecimal.valueOf(amount))
                .paymentMethod(method).status(status)
                .paidAt(paidAt)
                .refundAmount(refundAmount != null ? BigDecimal.valueOf(refundAmount) : null)
                .refundedAt(refundedAt)
                .build());
    }

    private void saveReview(Booking booking, Customer customer, Hotel hotel,
                            String title, String comment,
                            int overall, int cleanliness, int comfort, int location, int service, int value,
                            double calculated, String managerReply) {
        Review r = new Review();
        r.setBooking(booking);
        r.setCustomer(customer);
        r.setHotel(hotel);
        r.setTitle(title);
        r.setComment(comment);
        r.setCustomerOverallRating(overall);
        r.setCleanlinessScore(cleanliness);
        r.setComfortScore(comfort);
        r.setLocationScore(location);
        r.setServiceScore(service);
        r.setValueScore(value);
        r.setCalculatedOverallRating(calculated);
        r.setManagerReply(managerReply);
        r.setHidden(false);
        r.setFlagged(false);
        reviewRepository.save(r);
    }

    private void saveSaved(java.util.UUID customerId, java.util.UUID hotelId, String notes) {
        savedHotelRepository.save(SavedHotel.builder()
                .customerId(customerId).hotelId(hotelId).notes(notes).build());
    }

    private void saveNotification(java.util.UUID recipientId, String subject, String body,
                                  NotificationType type, NotificationStatus status) {
        notificationRepository.save(Notification.builder()
                .recipientId(recipientId).subject(subject).body(body)
                .type(type).status(status).channel(NotificationChannel.EMAIL)
                .build());
    }

    // ── Date helpers ──────────────────────────────────────────────────────────
    private static LocalDate     ld(String s)  { return LocalDate.parse(s); }
    private static LocalDateTime ldt(String s) { return LocalDateTime.parse(s); }

}

package com.adsphere.config;

import com.adsphere.domain.AdFormat;
import com.adsphere.domain.AdPosition;
import com.adsphere.domain.AppSettings;
import com.adsphere.domain.AppUser;
import com.adsphere.domain.Audience;
import com.adsphere.domain.AudienceStatus;
import com.adsphere.domain.AudienceType;
import com.adsphere.domain.Campaign;
import com.adsphere.domain.CampaignStatus;
import com.adsphere.domain.Country;
import com.adsphere.domain.Deal;
import com.adsphere.domain.DealStatus;
import com.adsphere.domain.DealType;
import com.adsphere.domain.DeviceTargeting;
import com.adsphere.domain.Objective;
import com.adsphere.domain.Placement;
import com.adsphere.domain.Role;
import com.adsphere.domain.TrafficType;
import com.adsphere.domain.VideoTargeting;
import com.adsphere.repository.AppSettingsRepository;
import com.adsphere.repository.AppUserRepository;
import com.adsphere.repository.AudienceRepository;
import com.adsphere.repository.CampaignRepository;
import com.adsphere.repository.DealRepository;
import com.adsphere.repository.PlacementRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Seeds demo data into an empty database. Disable with app.seed.enabled=false. */
@Component
public class DataSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

  private final AppUserRepository users;
  private final AudienceRepository audiences;
  private final PlacementRepository placements;
  private final CampaignRepository campaigns;
  private final DealRepository deals;
  private final AppSettingsRepository settings;
  private final PasswordEncoder encoder;
  private final boolean enabled;

  public DataSeeder(
      AppUserRepository users,
      AudienceRepository audiences,
      PlacementRepository placements,
      CampaignRepository campaigns,
      DealRepository deals,
      AppSettingsRepository settings,
      PasswordEncoder encoder,
      @Value("${app.seed.enabled:true}") boolean enabled) {
    this.users = users;
    this.audiences = audiences;
    this.placements = placements;
    this.campaigns = campaigns;
    this.deals = deals;
    this.settings = settings;
    this.encoder = encoder;
    this.enabled = enabled;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (!enabled || users.count() > 0) {
      return;
    }
    log.info("Seeding demo data");
    settings.save(new AppSettings());

    user("admin", "Admin@123", "Shweta Shende", "shweta.shende@example.com", Role.ADMIN, "Platform Administrator");
    user("manager", "Manager@123", "Alex Morgan", "alex.morgan@example.com", Role.MANAGER, "Campaign Manager");
    user("viewer", "Viewer@123", "Jordan Lee", "jordan.lee@example.com", Role.VIEWER, "Analyst");

    List<Audience> aud =
        audiences.saveAll(
            Arrays.asList(
                new Audience("18-25 Age Group", AudienceType.DEMOGRAPHIC, "Ages 18-25, All genders", AudienceStatus.ACTIVE, 4_200_000),
                new Audience("Tech Enthusiasts", AudienceType.INTEREST, "Technology, Gadgets, Gaming", AudienceStatus.ACTIVE, 2_750_000),
                new Audience("US - East Coast", AudienceType.LOCATION, "New York, New Jersey, Boston", AudienceStatus.ACTIVE, 9_800_000),
                new Audience("High Income", AudienceType.INCOME, "$100k+ annual income", AudienceStatus.ACTIVE, 1_300_000),
                new Audience("Mobile Users", AudienceType.DEVICE, "iOS and Android users", AudienceStatus.ACTIVE, 15_600_000),
                new Audience("Frequent Travelers", AudienceType.BEHAVIORAL, "3+ trips per year", AudienceStatus.ACTIVE, 870_000),
                new Audience("Parents of Toddlers", AudienceType.DEMOGRAPHIC, "Parents with children aged 1-4", AudienceStatus.INACTIVE, 640_000),
                new Audience("Sports Fans", AudienceType.INTEREST, "Football, Basketball, Cricket", AudienceStatus.ACTIVE, 5_100_000),
                new Audience("Europe - DACH", AudienceType.LOCATION, "Germany, Austria, Switzerland", AudienceStatus.ACTIVE, 3_400_000),
                new Audience("Connected TV Viewers", AudienceType.DEVICE, "Smart TV and streaming devices", AudienceStatus.ACTIVE, 2_050_000),
                new Audience("Online Shoppers", AudienceType.BEHAVIORAL, "Purchased online in the last 30 days", AudienceStatus.ACTIVE, 7_900_000)));

    Placement homepage = placement("Homepage Leaderboard", Country.US, aud.get(2), TrafficType.WEBSITE, AdPosition.ABOVE_THE_FOLD, AdFormat.DISPLAY_BANNER, DeviceTargeting.DESKTOP, VideoTargeting.NO_VIDEO, DealType.RTB, "3/day");
    Placement preRoll = placement("Streaming Pre-Roll", Country.US, aud.get(0), TrafficType.APP, AdPosition.PRE_ROLL, AdFormat.VIDEO, DeviceTargeting.MOBILE, VideoTargeting.IN_STREAM, DealType.PROGRAMMATIC, "5/day");
    Placement feed = placement("In-Feed Native", Country.GB, aud.get(1), TrafficType.APP, AdPosition.IN_CONTENT, AdFormat.NATIVE, DeviceTargeting.MOBILE, VideoTargeting.NO_VIDEO, DealType.PREFERRED, "2/day");
    Placement sidebar = placement("Tech Blog Sidebar", Country.CA, aud.get(1), TrafficType.WEBSITE, AdPosition.SIDEBAR, AdFormat.RICH_MEDIA, DeviceTargeting.ALL_DEVICES, VideoTargeting.NO_VIDEO, DealType.RTB, null);
    Placement ctv = placement("CTV Mid-Roll", Country.DE, aud.get(9), TrafficType.APP, AdPosition.MID_ROLL, AdFormat.VIDEO, DeviceTargeting.CONNECTED_TV, VideoTargeting.IN_STREAM, DealType.PRIVATE_MARKETPLACE, "4/week");

    campaign("Summer Sale 2025", Objective.BRAND_AWARENESS, CampaignStatus.ACTIVE, "2025-05-01", "2025-05-31", 50_000, homepage, feed);
    campaign("Product Launch", Objective.CONVERSIONS, CampaignStatus.ACTIVE, "2025-06-01", "2025-06-30", 75_000, preRoll);
    campaign("Holiday Campaign", Objective.TRAFFIC, CampaignStatus.PAUSED, "2025-11-01", "2025-11-30", 40_000, homepage, sidebar);
    campaign("Brand Awareness Q3", Objective.BRAND_AWARENESS, CampaignStatus.DRAFT, "2025-07-01", "2025-07-31", 60_000);
    campaign("Retargeting Campaign", Objective.CONVERSIONS, CampaignStatus.ACTIVE, "2025-08-01", "2025-08-31", 80_000, feed);
    campaign("Back to School", Objective.REACH, CampaignStatus.COMPLETED, "2025-08-15", "2025-09-15", 35_000, sidebar);
    campaign("Black Friday Blitz", Objective.CONVERSIONS, CampaignStatus.DRAFT, "2025-11-20", "2025-11-30", 120_000);
    campaign("Streaming Originals", Objective.VIDEO_VIEWS, CampaignStatus.ACTIVE, "2025-09-01", "2025-12-31", 95_000, preRoll, ctv);
    campaign("Mobile App Install Drive", Objective.APP_INSTALLS, CampaignStatus.ACTIVE, "2025-10-01", "2025-12-15", 55_000, feed);
    campaign("New Year Countdown", Objective.REACH, CampaignStatus.DRAFT, "2025-12-26", "2026-01-05", 45_000);
    campaign("Spring Collection", Objective.TRAFFIC, CampaignStatus.PAUSED, "2026-03-01", "2026-04-15", 30_000, homepage);
    campaign("Loyalty Rewards", Objective.CONVERSIONS, CampaignStatus.ACTIVE, "2026-01-10", "2026-06-30", 65_000, sidebar);
    campaign("Fitness Challenge", Objective.BRAND_AWARENESS, CampaignStatus.DRAFT, "2026-02-01", "2026-02-28", 25_000);

    deals.saveAll(
        Arrays.asList(
            new Deal("Premium Video Deal", "StreamMax Media", DealType.RTB, AdFormat.VIDEO, money("12.00"), 5_000_000, DealStatus.ACTIVE),
            new Deal("Display Standard Deal", "DailyNews Network", DealType.PROGRAMMATIC, AdFormat.DISPLAY_BANNER, money("8.50"), 8_000_000, DealStatus.ACTIVE),
            new Deal("Mobile App Deal", "PlayHub Games", DealType.RTB, AdFormat.NATIVE, money("9.75"), 3_000_000, DealStatus.ACTIVE),
            new Deal("Video Backfill Deal", "ClipWorld", DealType.PROGRAMMATIC, AdFormat.VIDEO, money("4.20"), 10_000_000, DealStatus.ACTIVE),
            new Deal("Desktop Display Deal", "TechDaily", DealType.RTB, AdFormat.DISPLAY_BANNER, money("5.75"), 6_500_000, DealStatus.PAUSED),
            new Deal("CTV Prime Time", "HomeScreen TV", DealType.PRIVATE_MARKETPLACE, AdFormat.VIDEO, money("28.00"), 1_200_000, DealStatus.ACTIVE),
            new Deal("Podcast Audio Spots", "SoundWave", DealType.PREFERRED, AdFormat.AUDIO, money("15.00"), 900_000, DealStatus.ACTIVE),
            new Deal("Rich Media Takeover", "LifeStyle Hub", DealType.PREFERRED, AdFormat.RICH_MEDIA, money("18.50"), 750_000, DealStatus.ACTIVE),
            new Deal("Sports Live Stream", "ArenaCast", DealType.RTB, AdFormat.VIDEO, money("22.00"), 2_400_000, DealStatus.ACTIVE),
            new Deal("Travel Native Feed", "WanderMag", DealType.PROGRAMMATIC, AdFormat.NATIVE, money("6.40"), 4_100_000, DealStatus.ACTIVE),
            new Deal("Finance Portal Banner", "MarketPulse", DealType.RTB, AdFormat.DISPLAY_BANNER, money("11.25"), 2_900_000, DealStatus.ACTIVE),
            new Deal("Gaming Rewarded Video", "LevelUp Studios", DealType.RTB, AdFormat.VIDEO, money("14.60"), 3_700_000, DealStatus.ACTIVE),
            new Deal("Weather App Interstitial", "SkyCast", DealType.PROGRAMMATIC, AdFormat.RICH_MEDIA, money("7.80"), 5_600_000, DealStatus.ACTIVE),
            new Deal("Recipe Site Sidebar", "KitchenJoy", DealType.RTB, AdFormat.DISPLAY_BANNER, money("3.90"), 3_300_000, DealStatus.PAUSED),
            new Deal("News Homepage Takeover", "Global Herald", DealType.PRIVATE_MARKETPLACE, AdFormat.RICH_MEDIA, money("32.00"), 600_000, DealStatus.ACTIVE)));
  }

  private void user(String username, String password, String name, String email, Role role, String title) {
    AppUser u = new AppUser();
    u.setUsername(username);
    u.setPasswordHash(encoder.encode(password));
    u.setFullName(name);
    u.setEmail(email);
    u.setRole(role);
    u.setJobTitle(title);
    u.setTimezone("UTC");
    users.save(u);
  }

  private Placement placement(
      String name, Country country, Audience audience, TrafficType traffic, AdPosition position,
      AdFormat format, DeviceTargeting device, VideoTargeting video, DealType dealType, String cap) {
    Placement p = new Placement();
    p.setName(name);
    p.setCountry(country);
    p.setAudience(audience);
    p.setTraffic(traffic);
    p.setAdPosition(position);
    p.setAdFormat(format);
    p.setDeviceTargeting(device);
    p.setVideoTargeting(video);
    p.setDealType(dealType);
    p.setFrequencyCap(cap);
    return placements.save(p);
  }

  private void campaign(
      String name, Objective objective, CampaignStatus status, String start, String end,
      long budget, Placement... assigned) {
    Campaign c = new Campaign();
    c.setName(name);
    c.setObjective(objective);
    c.setStatus(status);
    c.setStartDate(LocalDate.parse(start));
    c.setEndDate(LocalDate.parse(end));
    c.setBudget(BigDecimal.valueOf(budget).setScale(2));
    c.setPlacements(new LinkedHashSet<>(Arrays.asList(assigned)));
    c = campaigns.save(c);
    c.setCode(String.format("CMP%03d", c.getId()));
  }

  private static BigDecimal money(String value) {
    return new BigDecimal(value);
  }
}

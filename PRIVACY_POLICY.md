# Privacy Policy for DNSGuard PRO

**Last Updated:** September 17, 2026

**DNSGuard PRO** ("we", "our", or "the App") is developed with an uncompromising commitment to user privacy and digital wellbeing. The App is specifically designed as a strict self-control, porn addiction recovery, and content blocking utility. 

Your privacy is our core foundation. We believe that recovery tools must never compromise personal dignity or data confidentiality. **DNSGuard PRO does not collect, sell, monetize, or transmit your personal data, browsing history, or keystrokes.**

---

## 1. Core Services & Permissions

### A. Android VpnService API
* **Purpose:** DNSGuard PRO utilizes Android's `VpnService` API to establish a local, on-device loopback tunnel. This tunnel intercepts outbound DNS queries and routes them directly to secure, family-safe DNS resolvers (**AdGuard Family Protection**: `family.adguard-dns.com`, `94.140.14.15`, `94.140.15.16`) to block 100% of adult, explicit, and illicit domains, as well as 100% of ads, tracking networks, and malicious popups.
* **No Remote Traffic Routing:** The VPN operates **locally on your device**. None of your web traffic, browsing data, IP packets, or application usage is redirected to any remote proxy or commercial VPN server operated by us.
* **No Logging:** We do not log, inspect, analyze, or store your DNS queries or browsing history.

### B. Android AccessibilityService API
* **Purpose:** The AccessibilityService API is utilized exclusively to provide tamper protection and enforce the user's voluntary 1-year self-control commitment. It prevents impulsive deactivation of the protection suite by monitoring foreground window headers and blocking navigation to the Device Administrator deactivation screen or DNS bypass settings.
* **Google Play Compliance & Privacy Guarantees:**
  * The App **NEVER** monitors, collects, or logs user keystrokes, credit card numbers, passwords, personal messages, or form inputs.
  * The App does not alter user settings without consent, and does not interfere with standard system accessibility features.
  * All window and package checks occur **100% locally in-memory** and are immediately discarded.
  * A prominent in-app disclosure is displayed and explicit user authorization is obtained prior to requesting this permission.

### C. Device Administrator (BIND_DEVICE_ADMIN)
* **Purpose:** DNSGuard PRO requests standard Device Administrator rights to prevent impulsive uninstallation of the application during an active commitment period.
* **Scope:** The permission is used solely to guard against unauthorized application removal. The App does not invoke data wipe (`wipe-data`), camera lockdown (`disable-camera`), or remote device tracking.

---

## 2. Information Storage and Security

* **Local Encrypted Storage:** Your Master Security PIN, pledge timestamp, and high-water mark are secured on your device using Android Jetpack Security's `EncryptedSharedPreferences` backed by AES-256-GCM encryption and hardware-backed KeyStore.
* **No Cloud Accounts or Databases:** DNSGuard PRO functions completely offline. There are no user accounts, external databases, tracking IDs, or remote profiling.
* **No Third-Party SDKs or Ads:** The App contains **zero** commercial advertising SDKs, analytics frameworks (such as Firebase Analytics, Facebook SDK, or Google AdMob), or marketing trackers.

---

## 3. Network Communication

The only outbound network requests performed by the App are:
1. Standard DNS queries routed to safe public resolvers (AdGuard Family Protection: `94.140.14.15`, `94.140.15.16`, `family.adguard-dns.com`) to resolve web domain names while filtering adult material and advertisements.
2. Lightweight Network Time Protocol (NTP) requests (`pool.ntp.org` / `time.google.com`) once every 6 hours via WorkManager to ensure device clock integrity against manual time tampering.

---

## 4. Children's Privacy
DNSGuard PRO is suitable for users of all ages striving to protect themselves or their families from harmful online content. We do not knowingly collect any personally identifiable information from children or adults.

---

## 5. Changes to This Privacy Policy
We may update this Privacy Policy from time to time to reflect operational or regulatory updates. Any changes will be published in this document and updated in future releases.

---

## 6. Contact Us
If you have questions, feedback, or concerns regarding this Privacy Policy or the security of DNSGuard PRO, please contact us via our official repository:
* **Repository:** [https://github.com/zenbijoy/DNSGardPRO](https://github.com/zenbijoy/DNSGardPRO)
* **Developer:** zenbijoy

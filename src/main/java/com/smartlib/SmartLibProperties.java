package com.smartlib;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smartlib")
public class SmartLibProperties {

    private final Bootstrap bootstrap = new Bootstrap();
    private final Loans loans = new Loans();

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public Loans getLoans() {
        return loans;
    }

    public static class Bootstrap {
        private String adminUsername;
        private String adminPassword;
        private String adminEmail;
        private String adminFullName;

        public String getAdminUsername() { return adminUsername; }
        public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
        public String getAdminPassword() { return adminPassword; }
        public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
        public String getAdminEmail() { return adminEmail; }
        public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
        public String getAdminFullName() { return adminFullName; }
        public void setAdminFullName(String adminFullName) { this.adminFullName = adminFullName; }
    }

    public static class Loans {
        private int defaultDurationDays = 14;
        private BigDecimal finePerDay = new BigDecimal("1.50");
        private int maxRenewals = 2;

        public int getDefaultDurationDays() { return defaultDurationDays; }
        public void setDefaultDurationDays(int defaultDurationDays) { this.defaultDurationDays = defaultDurationDays; }
        public BigDecimal getFinePerDay() { return finePerDay; }
        public void setFinePerDay(BigDecimal finePerDay) { this.finePerDay = finePerDay; }
        public int getMaxRenewals() { return maxRenewals; }
        public void setMaxRenewals(int maxRenewals) { this.maxRenewals = maxRenewals; }
    }
}

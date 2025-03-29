package com.tools.model;

import java.util.Map;

/**
 * 表示API信息的实体类
 */
public class ApiInfo {
    private String title;
    private String description;
    private String version;
    private ContactInfo contact;
    private LicenseInfo license;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ContactInfo getContact() {
        return contact;
    }

    public void setContact(ContactInfo contact) {
        this.contact = contact;
    }

    public LicenseInfo getLicense() {
        return license;
    }

    public void setLicense(LicenseInfo license) {
        this.license = license;
    }

    /**
     * 转换为Map表示形式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("title", title);
        map.put("description", description);
        map.put("version", version);
        map.put("contact", contact != null ? contact.toMap() : new java.util.HashMap<>());
        map.put("license", license != null ? license.toMap() : new java.util.HashMap<>());
        return map;
    }

    /**
     * 从Map构建实例
     */
    @SuppressWarnings("unchecked")
    public static ApiInfo fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        ApiInfo info = new ApiInfo();
        info.setTitle((String) map.get("title"));
        info.setDescription((String) map.get("description"));
        info.setVersion((String) map.get("version"));
        
        Map<String, String> contactMap = (Map<String, String>) map.get("contact");
        if (contactMap != null) {
            ContactInfo contact = new ContactInfo();
            contact.setEmail(contactMap.get("email"));
            info.setContact(contact);
        }
        
        Map<String, String> licenseMap = (Map<String, String>) map.get("license");
        if (licenseMap != null) {
            LicenseInfo license = new LicenseInfo();
            license.setName(licenseMap.get("name"));
            info.setLicense(license);
        }
        
        return info;
    }

    /**
     * 联系人信息
     */
    public static class ContactInfo {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Map<String, String> toMap() {
            Map<String, String> map = new java.util.HashMap<>();
            map.put("email", email);
            return map;
        }
    }

    /**
     * 许可证信息
     */
    public static class LicenseInfo {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, String> toMap() {
            Map<String, String> map = new java.util.HashMap<>();
            map.put("name", name);
            return map;
        }
    }
} 
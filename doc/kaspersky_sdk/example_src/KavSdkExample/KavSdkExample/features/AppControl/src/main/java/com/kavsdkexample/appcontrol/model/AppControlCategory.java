/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.model;

public enum AppControlCategory {
    BusinessSoftware("BusinessSoftware"),
    EducationalSoftware("EducationalSoftware"),
    Entertainment("Entertainment"),
    Entertainment_Games("Entertainment_Games"),
    Entertainment_HomeFamilyHobbiesHealth("Entertainment_HomeFamilyHobbiesHealth"),
    Entertainment_OnlineShopping("Entertainment_OnlineShopping"),
    Entertainment_SocialNetworks("Entertainment_SocialNetworks"),
    GraphicDesignSoftware("GraphicDesignSoftware"),
    Information("Information"),
    Information_MappingApplications("Information_MappingApplications"),
    Information_Medical("Information_Medical"),
    Information_NewsreadersAndRssReaders("Information_NewsreadersAndRssReaders"),
    Information_Weather("Information_Weather"),
    Information_Transport("Information_Transport"),
    InternetSoftware_ImVoipAndVideo("InternetSoftware_ImVoipAndVideo"),
    InternetSoftware_OnlineStorage("InternetSoftware_OnlineStorage"),
    InternetSoftware_SoftwareDownloaders("InternetSoftware_SoftwareDownloaders"),
    Multimedia("Multimedia"),
    OperatingSystemsAndUtilities("OperatingSystemsAndUtilities"),
    OperatingSystemsAndUtilities_Launchers("OperatingSystemsAndUtilities_Launchers"),
    Browsers("Browsers"),
    DeveloperTools("DeveloperTools"),
    GoldenImage("GoldenImage"),
    InternetSoftware("InternetSoftware"),
    NetworkingInfrastructureSoftware("NetworkingInfrastructureSoftware"),
    NetworkingSoftware("NetworkingSoftware"),
    OperatingSystemsAndUtilities_SystemUtilities("OperatingSystemsAndUtilities_SystemUtilities"),
    SecuritySoftware("SecuritySoftware"),
    OtherSoftware("OtherSoftware"),
    Unknown("Unknown"),
    BusinessSoftware_EmailSoftware("BusinessSoftware_EmailSoftware");

    private String mName;

    AppControlCategory(String name)
    {
        mName = name;
    }
    public String getName() {
        return mName;
    }
}
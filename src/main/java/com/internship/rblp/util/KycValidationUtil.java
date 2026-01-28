package com.internship.rblp.util;

import com.internship.rblp.models.enums.DocType;

import java.util.regex.Pattern;

public class KycValidationUtil {

    private static final Pattern PAN_PATTERN = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}");
    private static final Pattern AADHAAR_PATTERN = Pattern.compile("^[2-9]{1}[0-9]{11}$");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[A-Z]{1}[0-9]{7}$");

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public static boolean isValidPan(String pan) {
        return pan != null && PAN_PATTERN.matcher(pan.toUpperCase()).matches();
    }

    public static boolean isValidAadhaar(String aadhaar) {
        return aadhaar != null && AADHAAR_PATTERN.matcher(aadhaar).matches();
    }

    public static boolean isValidPassport(String passport) {
        return passport != null && PASSPORT_PATTERN.matcher(passport.toUpperCase()).matches();
    }

    public static boolean isNameMatch(String nameOnDoc, String profileName) {
        if (nameOnDoc == null || profileName == null) return false;
        return nameOnDoc.equalsIgnoreCase(profileName) ||
                profileName.toLowerCase().contains(nameOnDoc.toLowerCase()) ||
                nameOnDoc.toLowerCase().contains(profileName.toLowerCase());
    }
    public static boolean isNumberMatch(String docNumber, String extractedNumber)
    {
        if (docNumber == null || extractedNumber == null) return false;
        return docNumber.equals(extractedNumber);
    }

    public static boolean isValidFileType(String filePath) {
        if (filePath == null) return false;
        String lower = filePath.toLowerCase();
        return lower.endsWith(".pdf") || lower.endsWith(".jpg") || lower.endsWith(".png");
    }

    public static boolean isValidFileSize(long sizeBytes) {
        return sizeBytes <= MAX_FILE_SIZE;
    }

    public static boolean validateNumber(DocType type, String number) {
        return switch (type) {
            case PAN -> isValidPan(number);
            case AADHAAR -> isValidAadhaar(number);
            case PASSPORT -> isValidPassport(number);
        };
    }
}
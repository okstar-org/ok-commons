package org.okstar.platform.common.security;

import java.util.Objects;

/**
 * OkPasswordUtilImpl
 */
public class OkPasswordUtil {

    private OkPasswordUtil() {

    }

    public static String hashPassword(String password, PasswordGenerator passwordGenerator) throws Exception {
        String hashed = "";
        if (Objects.requireNonNull(passwordGenerator) == PasswordGenerator.PBKDF2) {
            hashed = OkPasswordPBKDF2.hashPassword(password);
        }
        return hashed;
    }

    public static String hashPassword(String password) throws Exception {
        return hashPassword(password, PasswordGenerator.PBKDF2);
    }

    public static boolean verifyPassword(String password, String storedHash, PasswordGenerator passwordGenerator) throws Exception {
        if (Objects.requireNonNull(passwordGenerator) == PasswordGenerator.PBKDF2) {
            return OkPasswordPBKDF2.verifyPassword(password, storedHash);
        }
        return false;
    }

    public static boolean verifyPassword(String password, String storedHash) throws Exception {
        return verifyPassword(password, storedHash, PasswordGenerator.PBKDF2);
    }
}

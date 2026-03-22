package org.okstar.platform.common.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * OkPasswordUtils - PBKDF2实现
 */
class OkPasswordPBKDF2 {
    
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    
    public static String hashPassword(String password) throws Exception {
        // 生成随机盐
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        
        // 哈希密码
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();
        
        // 组合盐和哈希值
        byte[] combined = new byte[salt.length + hash.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(hash, 0, combined, salt.length, hash.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }
    
    public static boolean verifyPassword(String password, String storedHash) throws Exception {
        // 解码存储的哈希值
        byte[] combined = Base64.getDecoder().decode(storedHash);
        
        // 提取盐和哈希值
        byte[] salt = new byte[16];
        byte[] hash = new byte[combined.length - 16];
        System.arraycopy(combined, 0, salt, 0, salt.length);
        System.arraycopy(combined, salt.length, hash, 0, hash.length);
        
        // 重新计算哈希
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] testHash = factory.generateSecret(spec).getEncoded();
        
        // 比较哈希值
        return MessageDigest.isEqual(hash, testHash);
    }
}
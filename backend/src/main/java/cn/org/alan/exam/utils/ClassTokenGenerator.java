package cn.org.alan.exam.utils;

import java.security.SecureRandom;


public class ClassTokenGenerator {

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * 生成班级口令
     * @param length
     * @return
     */
    public static String generateClassToken(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder tokenBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            tokenBuilder.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return tokenBuilder.toString();
    }
}
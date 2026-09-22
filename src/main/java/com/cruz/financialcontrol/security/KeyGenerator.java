package com.cruz.financialcontrol.security;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Class Name: KeyGenerator
 * Description:
 *
 * @author edson
 * @date 16/09/2026
 */
public class KeyGenerator {
    static void main(String[] args) {

        byte[] key = new byte[32];

        new SecureRandom().nextBytes(key);

        System.out.println(Base64.getEncoder().encodeToString(key));
    }
}

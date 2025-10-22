package com.pubsystem.auth.util;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class KeyGenerator {

    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;
    private static final String PRIVATE_KEY_FILE = "src/main/resources/keys/private_key.pem";
    private static final String PUBLIC_KEY_FILE = "src/main/resources/keys/public_key.pem";

    /**
     * Generate RSA key pair and save to PEM files
     */
    public static void generateKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Create keys directory if not exists
        Files.createDirectories(Paths.get("src/main/resources/keys"));

        // Save private key
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(PRIVATE_KEY_FILE))) {
            pemWriter.writeObject(keyPair.getPrivate());
        }

        // Save public key
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(PUBLIC_KEY_FILE))) {
            pemWriter.writeObject(keyPair.getPublic());
        }

        System.out.println("RSA Key Pair generated successfully!");
        System.out.println("Private Key: " + PRIVATE_KEY_FILE);
        System.out.println("Public Key: " + PUBLIC_KEY_FILE);
    }

    /**
     * Load private key from PEM file (for local development)
     */
    public static RSAPrivateKey loadPrivateKey(String filename) throws Exception {
        try (FileReader keyReader = new FileReader(filename);
             PEMParser pemParser = new PEMParser(keyReader)) {

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();
            return (RSAPrivateKey) converter.getPrivateKey(privateKeyInfo);
        }
    }

    /**
     * ✅ Load private key from InputStream (for JAR/Docker)
     * This works in both IntelliJ and Docker
     */
    public static RSAPrivateKey loadPrivateKeyFromStream(InputStream inputStream) throws Exception {
        try (InputStreamReader keyReader = new InputStreamReader(inputStream);
             PEMParser pemParser = new PEMParser(keyReader)) {

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();
            return (RSAPrivateKey) converter.getPrivateKey(privateKeyInfo);
        }
    }

    /**
     * Load public key from PEM file (for local development)
     */
    public static RSAPublicKey loadPublicKey(String filename) throws Exception {
        try (FileReader keyReader = new FileReader(filename);
             PEMParser pemParser = new PEMParser(keyReader)) {

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            SubjectPublicKeyInfo publicKeyInfo = (SubjectPublicKeyInfo) pemParser.readObject();
            return (RSAPublicKey) converter.getPublicKey(publicKeyInfo);
        }
    }

    /**
     * ✅ Load public key from InputStream (for JAR/Docker)
     * This works in both IntelliJ and Docker
     */
    public static RSAPublicKey loadPublicKeyFromStream(InputStream inputStream) throws Exception {
        try (InputStreamReader keyReader = new InputStreamReader(inputStream);
             PEMParser pemParser = new PEMParser(keyReader)) {

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            SubjectPublicKeyInfo publicKeyInfo = (SubjectPublicKeyInfo) pemParser.readObject();
            return (RSAPublicKey) converter.getPublicKey(publicKeyInfo);
        }
    }

    /**
     * Main method to generate keys
     */
    public static void main(String[] args) {
        try {
            generateKeys();
        } catch (Exception e) {
            System.err.println("Error generating keys: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
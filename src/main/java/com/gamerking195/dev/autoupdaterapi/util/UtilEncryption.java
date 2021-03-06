package com.gamerking195.dev.autoupdaterapi.util;

import com.gamerking195.dev.autoupdaterapi.AutoUpdaterAPI;
import lombok.Getter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 6/14/17
 */
class UtilEncryption {
    @Getter private static UtilEncryption instance = new UtilEncryption();

    private static SecretKeySpec secretKey;
    boolean useOldKeys;
    private File keyFile;
    private FileConfiguration keyConfig;

    void init() {
        if (keyFile == null)
            keyFile = new File(AutoUpdaterAPI.getInstance().getDataFolder().getParentFile().getAbsolutePath() + "/.auapi/key.enc");

        if (!keyFile.getParentFile().exists())
            keyFile.getParentFile().mkdirs();

        if (keyConfig == null) {
            keyConfig = YamlConfiguration.loadConfiguration(keyFile);
            keyConfig.options().copyDefaults(true);
        }

        if (keyConfig.get("0") == null)
            keyConfig.set("0", RandomStringUtils.randomAscii(20));

        if (keyConfig.get("1") == null)
            keyConfig.set("1", RandomStringUtils.randomAscii(20));

        if (keyConfig.get("2") == null)
            keyConfig.set("2", RandomStringUtils.randomAscii(20));

        if (keyConfig.get("3") == null)
            keyConfig.set("3", RandomStringUtils.randomAscii(20));

        try {
            if (!keyFile.exists())
                keyFile.createNewFile();

            keyFile.setReadable(true, true);
            keyFile.setWritable(true, true);

            keyConfig.save(keyFile);
        } catch (IOException ex) {
            AutoUpdaterAPI.getInstance().printError(ex, "Error occurred while creating the encrypted file.");
        }
    }

    void setKeyNumber(int number) {
        if (useOldKeys) {
            switch (number) {
                case 0:
                    setKey("h9-[hEM*G!DfXp8~");
                    break;
                case 1:
                    setKey("Hb3Kd`z-M(8p%U;f");
                    break;
                case 2:
                    setKey("t?NB%P%WsH]R(6}Bn");
                    break;
                case 3:
                    setKey("f5/K5^n8};u2LxqK");
            }
        } else {
            switch (number) {
                case 0:
                    setKey(keyConfig.getString(String.valueOf(number)));
                    break;
                case 1:
                    setKey(keyConfig.getString(String.valueOf(number)));
                    break;
                case 2:
                    setKey(keyConfig.getString(String.valueOf(number)));
                    break;
                case 3:
                    setKey(keyConfig.getString(String.valueOf(number)));
            }
        }
    }

    private void setKey(String myKey) {
        MessageDigest sha;
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = java.util.Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException ex) {
            AutoUpdaterAPI.getInstance().printError(ex);
        }
    }

    String encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            cipher.init(1, secretKey);

            return Base64.encodeBase64String(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            AutoUpdaterAPI.getInstance().printError(ex, "Error occurred while encrypting string.");
        }

        return null;
    }

    String decrypt(String strToDecrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");

            cipher.init(2, secretKey);
            return new String(cipher.doFinal(Base64.decodeBase64(strToDecrypt)));
        } catch (Exception ex) {
            AutoUpdaterAPI.getInstance().printError(ex, "Error occurred while encrypting string.");
        }

        return null;
    }
}

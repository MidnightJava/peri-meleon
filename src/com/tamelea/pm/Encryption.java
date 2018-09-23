package com.tamelea.pm;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

final class Encryption {
	private static final byte[] salt = {
        (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
        (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
    };
	private static final int iterationCount = 20;
	private static final PBEParameterSpec parameterSpec = 
		new PBEParameterSpec(salt, iterationCount);
	
	Cipher makeEncryptionCipher(char[] password)
	throws GeneralSecurityException
	{
		char[] passchars = password;
		PBEKeySpec keySpec = new PBEKeySpec(passchars);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey pbeKey = keyFactory.generateSecret(keySpec);
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, parameterSpec);
		return pbeCipher;
	}
	
	Cipher makeDecryptionCipher(char[] password)
	throws GeneralSecurityException
	{
		char[] passchars = password;
		PBEKeySpec keySpec = new PBEKeySpec(passchars);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey pbeKey = keyFactory.generateSecret(keySpec);
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, parameterSpec);
		return pbeCipher;
	}
}

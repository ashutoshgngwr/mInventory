package com.github.ashutoshgngwr.minventory.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

	public static String sha1(String data) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] bytes = md.digest(data.getBytes());
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < bytes.length; i++)
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));

			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
}

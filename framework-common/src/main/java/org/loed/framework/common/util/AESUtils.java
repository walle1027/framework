package org.loed.framework.common.util;


import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * @author 杨涛
 * @version 1.0
 * @date 12-9-6 上午11:01
 */

public class AESUtils {
	/**
	 * 密钥算法
	 */
	private static final String KEY_ALGORITHM = "AES";

	private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

	/**
	 * 初始化密钥
	 *
	 * @return byte[] 密钥
	 * @throws Exception
	 */
	public static byte[] initSecretKey() {
		//返回生成指定算法的秘密密钥的 KeyGenerator 对象
		KeyGenerator kg = null;
		try {
			kg = KeyGenerator.getInstance(KEY_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return new byte[0];
		}
		//初始化此密钥生成器，使其具有确定的密钥大小
		//AES 要求密钥长度为 128
		kg.init(128);
		//生成一个密钥
		SecretKey secretKey = kg.generateKey();
		return secretKey.getEncoded();
	}

	/**
	 * 转换密钥
	 *
	 * @param key 二进制密钥
	 * @return 密钥
	 */
	public static Key toKey(byte[] key) {
		//生成密钥
		return new SecretKeySpec(key, KEY_ALGORITHM);
	}

	public static Key toKey(String hexString) throws DecoderException {
		return toKey(Hex.decodeHex(hexString.toCharArray()));
	}

	/**
	 * 加密
	 *
	 * @param data 待加密数据
	 * @param key  密钥
	 * @return byte[]   加密数据
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, Key key) throws Exception {
		return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}

	/**
	 * 加密
	 *
	 * @param data 待加密数据
	 * @param key  二进制密钥
	 * @return byte[]   加密数据
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}

	public static String encryptAsHexString(String content, Key key) throws Exception {
		return Hex.encodeHexString(encrypt(content.getBytes(), key));
	}


	/**
	 * 加密
	 *
	 * @param data            待加密数据
	 * @param key             二进制密钥
	 * @param cipherAlgorithm 加密算法/工作模式/填充方式
	 * @return byte[]   加密数据
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception {
		//还原密钥
		Key k = toKey(key);
		return encrypt(data, k, cipherAlgorithm);
	}

	/**
	 * 加密
	 *
	 * @param data            待加密数据
	 * @param key             密钥
	 * @param cipherAlgorithm 加密算法/工作模式/填充方式
	 * @return byte[]   加密数据
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception {
		//实例化
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		//使用密钥初始化，设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, key);
		//执行操作
		return cipher.doFinal(data);
	}


	/**
	 * 解密
	 *
	 * @param data 待解密数据
	 * @param key  二进制密钥
	 * @return byte[]   解密数据
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		return decrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}

	/**
	 * 解密
	 *
	 * @param data 待解密数据
	 * @param key  密钥
	 * @return byte[]   解密数据
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] data, Key key) throws Exception {
		return decrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}

	public static String decryptHexString(String hexString, Key key) throws Exception {
		byte[] content = decrypt(Hex.decodeHex(hexString.toCharArray()), key);
		return new String(content, "UTF-8");

	}

	/**
	 * 解密
	 *
	 * @param data            待解密数据
	 * @param key             二进制密钥
	 * @param cipherAlgorithm 加密算法/工作模式/填充方式
	 * @return byte[]   解密数据
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception {
		//还原密钥
		Key k = toKey(key);
		return decrypt(data, k, cipherAlgorithm);
	}

	/**
	 * 解密
	 *
	 * @param data            待解密数据
	 * @param key             密钥
	 * @param cipherAlgorithm 加密算法/工作模式/填充方式
	 * @return byte[]   解密数据
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception {
		//实例化
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		//使用密钥初始化，设置为解密模式
		cipher.init(Cipher.DECRYPT_MODE, key);
		//执行操作
		return cipher.doFinal(data);
	}

	private static String showByteArray(byte[] data) {
		if (null == data) {
			return null;
		}
		StringBuilder sb = new StringBuilder("{");
		for (byte b : data) {
			sb.append(b).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("}");
		return sb.toString();
	}
}

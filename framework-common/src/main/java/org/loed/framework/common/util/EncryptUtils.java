package org.loed.framework.common.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import sun.misc.BASE64Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * @author Thomason
 * @version 1.0
 * @since 2016/2/27 11:13
 */

@SuppressWarnings("Duplicates")
public class EncryptUtils {
	public static final String KEY_SHA = "SHA";
	public static final String KEY_MD5 = "MD5";
	public static final String RSA_PUBLIC_KEY = "RSAPublicKey";//RSA公钥
	public static final String RSA_PRIVATE_KEY = "RSAPrivateKey";//RSA私钥
	public static final String ALGORITHM_RSA = "RSA";
	public static final String SIGNATURE_ALGORITHM_RSA = "SHA1WithRSA";
	public static final String DSA_PUBLIC_KEY = "DSAPublicKey";//RSA公钥
	public static final String DSA_PRIVATE_KEY = "DSAPrivateKey";//RSA私钥
	public static final String KEY_ALGORITHM_DSA = "DSA";
	public static final String SIGNATURE_ALGORITHM_DSA = "SHA1WithDSA";
	private static final String CHARACTER_ENCODING = "UTF-8";
	/**
	 * RSA最大加密明文大小
	 */
	private static final int MAX_ENCRYPT_BLOCK = 117;

	/**
	 * RSA最大解密密文大小
	 */
	private static final int MAX_DECRYPT_BLOCK = 128;

	/**
	 * 将请求参数拼接为待加密参数
	 *
	 * @param params 请求参数
	 * @return 带加密参数
	 */
	public static String createSignString(Map<String, String> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		StringBuilder preStr = new StringBuilder();
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);
			if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
				preStr.append(key).append("=").append(value);
			} else {
				preStr.append(key).append("=").append(value).append("&");
			}
		}
		return preStr.toString();
	}

	public static Map<String, String> filterMap(Map<String, String> map, String[] keys) {
		Map<String, String> result = new HashMap<>();
		Set<String> keySet = new HashSet<>();
		if (keys != null) {
			Collections.addAll(keySet, keys);
		}
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (StringUtils.isEmpty(value)) {
				continue;
			}
			if (keySet.contains(key)) {
				continue;
			}
			result.put(key, value);
		}
		return result;
	}


	/**
	 * BASE64Encoder 加密
	 *
	 * @param data 要加密的数据
	 * @return 加密后的字符串
	 */
	public static String encryptBASE64(byte[] data) {
		return Base64.encodeBase64String(data).replaceAll("[\\s*\t\n\r]", "");
	}

	/**
	 * BASE64Decoder 解密
	 *
	 * @param data 要解密的字符串
	 * @return 解密后的byte[]
	 * @throws Exception
	 */
	public static byte[] decryptBASE64(String data) throws Exception {
		return Base64.decodeBase64(data);
	}

	/**
	 * MD5加密
	 *
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptMD5(byte[] data) throws Exception {
		MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
		md5.update(data);
		return md5.digest();
	}

	/**
	 * SHA加密
	 *
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptSHA(byte[] data) throws Exception {
		MessageDigest sha = MessageDigest.getInstance(KEY_SHA);
		sha.update(data);
		return sha.digest();
	}

	/**
	 * 生成密钥
	 *
	 * @return 密钥对象
	 * @throws Exception
	 */
	public static KeyPair generateDSAKey() throws Exception {
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("DSA");
		SecureRandom secureRandom = new SecureRandom();
		//Modulus size must range from 512 to 1024 and be a multiple of 64
		keygen.initialize(1024, secureRandom);
		return keygen.genKeyPair();
	}


	/**
	 * 生成密钥
	 *
	 * @return 密钥对象
	 * @throws Exception
	 */
	public static KeyPair generateRSAKey() throws Exception {
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		SecureRandom secureRandom = new SecureRandom();
		//Modulus size must range from 512 to 1024 and be a multiple of 64
		keygen.initialize(1024, secureRandom);

		return keygen.genKeyPair();
	}


	/**
	 * 用私钥对信息生成数字签名
	 *
	 * @param data       //加密数据
	 * @param privateKey //私钥
	 * @return
	 * @throws Exception
	 */
	public static String sign(byte[] data, String privateKey, String algorithm, String signatureAlgorithm) throws Exception {
		//解密私钥
		byte[] keyBytes = decryptBASE64(privateKey);
		//构造PKCS8EncodedKeySpec对象
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
		//指定加密算法
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		//取私钥匙对象
		PrivateKey privateKey2 = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
		//用私钥对信息生成数字签名
		Signature signature = Signature.getInstance(signatureAlgorithm);
		signature.initSign(privateKey2);
		signature.update(data);

		return encryptBASE64(signature.sign());
	}


	/**
	 * 校验数字签名
	 *
	 * @param data      加密数据
	 * @param publicKey
	 * @param sign      数字签名
	 * @return
	 * @throws Exception
	 */
	public static boolean verify(byte[] data, String publicKey, String sign, String algorithm, String signatureAlgorithm) throws Exception {
		byte[] keyBytes = decryptBASE64(publicKey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		PublicKey pubKey = keyFactory.generatePublic(keySpec);

		Signature signature = Signature.getInstance(signatureAlgorithm);
		signature.initVerify(pubKey);
		signature.update(data);
		return signature.verify(decryptBASE64(sign)); //验证签名
	}


	/**
	 * 用私钥加密
	 *
	 * @param data 加密数据
	 * @param key  密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPrivateKey(byte[] data, String key, String algorithm) throws Exception {
		//解密密钥
		byte[] keyBytes = decryptBASE64(key);
		//取私钥
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		Key privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

		//对数据加密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);

		return sectionalEncrypt(data, cipher);
	}

	private static byte[] sectionalEncrypt(byte[] data, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException, IOException {
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return encryptedData;
	}

	/**
	 * 用私钥解密 * @param data    加密数据
	 *
	 * @param key 密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPrivateKey(byte[] data, String key, String algorithm) throws Exception {
		//对私钥解密
		byte[] keyBytes = decryptBASE64(key);
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		Key privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
		//对数据解密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return sectionalDecrypt(data, cipher);
	}

	/**
	 * 用公钥加密
	 *
	 * @param data 加密数据
	 * @param key  密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPublicKey(byte[] data, String key, String algorithm) throws Exception {
		//对公钥解密
		byte[] keyBytes = decryptBASE64(key);
		//取公钥
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		Key publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

		//对数据解密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return sectionalEncrypt(data, cipher);
	}

	/**
	 * 用公钥解密
	 *
	 * @param data 加密数据
	 * @param key  密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPublicKey(byte[] data, String key, String algorithm) throws Exception {
		//对私钥解密
		byte[] keyBytes = decryptBASE64(key);
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		Key publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

		//对数据解密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, publicKey);

		return sectionalDecrypt(data, cipher);
	}

	private static byte[] sectionalDecrypt(byte[] data, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException, IOException {
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		byte[] decryptedData = out.toByteArray();
		out.close();
		return decryptedData;
	}

	public static String base64Md5(String srcValue) {
		byte[] bytPwd = srcValue.getBytes();
		MessageDigest alg = null;
		String tgtValue = null;
		try {
			alg = MessageDigest.getInstance("MD5");
			alg.update(bytPwd);
			byte[] digest = alg.digest();
			tgtValue = Base64.encodeBase64String(digest).replaceAll("[\\s*\t\n\r]", "");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return tgtValue;
	}

	public static String hmacSHA256(String data, String secretKey)
			throws NoSuchAlgorithmException, InvalidKeyException,
			IllegalStateException, UnsupportedEncodingException {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(secretKey.getBytes(CHARACTER_ENCODING),
				"HmacSHA256"));
		byte[] signature = mac.doFinal(data.getBytes(CHARACTER_ENCODING));
		BASE64Encoder base64Encoder = new BASE64Encoder();
		return new String(base64Encoder.encode(signature).getBytes(),
				CHARACTER_ENCODING);
	}
}

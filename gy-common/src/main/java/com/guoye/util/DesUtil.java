package com.guoye.util;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * 3DES加密
 * 
 * @version 1.0
 * @author
 * 
 */
public abstract class DesUtil {
	 
	
	/**
	 * 密钥算法
	 * @version 1.0
	 * @author
	 */
	public static final String KEY_ALGORITHM = "DESede";
		
	/**
	 * 加密/解密算法/工作模式/填充方式
	 * @version 1.0
	 * @author
	 */	
	public static final String CIPHER_ALGORITHM = "DESede/ECB/PKCS5Padding";
		
	/**
	 * 转换密钥
	 * @param key 二进制密钥
	 * @return key 密钥
	 * 
	 */	
	public static Key toKey(byte[] key) throws Exception{
		//实例化DES密钥材料
		DESedeKeySpec dks = new DESedeKeySpec(key);
		//实例化秘密密钥工厂
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
		//生成秘密密钥
		return keyFactory.generateSecret(dks);
	}

	/**
	 * 解密
	 * @param data 待解密数据
	 * @param key 密钥
	 * @return byte[] 解密数据
	 */	
	public static byte[] decrypt(byte[] data, byte[] key)throws Exception{
		//还原密钥
		Key k = toKey(key);
		/**
		 * 实例化
		 * 使用PKCS7Padding填充方式，按如下代码实现
		 * Cipher.getInstance(CIPHER_ALGORITHM,"BC");
		 */
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		//初始化，设置为解密模式
		cipher.init(Cipher.DECRYPT_MODE, k);
		//执行操作
		return cipher.doFinal(data);
	}
	
	public static byte[] encrypt(byte[] data, byte[] key) throws Exception{
		//还原密钥
		Key k = toKey(key);
		/**
		 * 实例化
		 * 使用PKCS5Padding填充方式，按如下代码实现
		 * Cipher.getInstance(CIPHER_ALGORITHM,"BC");
		 */
		Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
		//初始化，设置为解密模式
		//cipher.init(Cipher.ENCRYPT_MODE, k);
		cipher.init(Cipher.ENCRYPT_MODE,  k);

		//执行操作
		return cipher.doFinal(data);
	}

	
	@SuppressWarnings("unused")
	private static SecureRandom AlgorithmParameterSpec(int i, int j, int k,
			int l, int m, int n, int o, int p) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 生成密钥
	 * 
	 * @return byte[] 二进制密钥
	 */	
	public static byte[] initKey() throws Exception{
		/**
		 * 实例化
		 * 使用128位或192位长度密钥
		 * KeyGenerator.getInstance(KEY_ALGORITHM,"BC");
		 */
		KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
		/**
		 * 初始化
		 *使用128位或192位长度密钥，按如下代码实现
		 *kg.init(128);
		 *kg.init(192);
		 */
		kg.init(168);
		//生成秘密密钥
		SecretKey secretKey = kg.generateKey();
		//获得密钥的二进制编码形式
		return secretKey.getEncoded();
	}
	
	

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
	   
//	   BASE64Decoder dec=new BASE64Decoder();
//	   BASE64Encoder enc = new BASE64Encoder();
		String ming = "123456";
		byte[] s = DesUtil.encrypt(ming.getBytes("UTF-8"), "955e43afa6dcb571acec7328e3b36ca6".getBytes("UTF-8"));
		System.out.println(Base64.encodeBase64String(s));
	}
	
	//10进制数组转换16进制数组
	public static String printbytes(String tip, byte[] b) {
		String ret = "";
		String str;
		// System.out.println("b "+b);
		System.out.print(tip);
		for (int i = 0; i < b.length; i++) {

			str = Integer.toHexString((int) (b[i] & 0xff));

			if (str.length() == 1)
				str = "0" + str;
			System.out.print(str + " ");
			ret = ret + str + " ";
		}
		System.out.println();

		// 02 00 07 00 00 60 70 01 17 35 03 C8
		return ret;
	}
	
}

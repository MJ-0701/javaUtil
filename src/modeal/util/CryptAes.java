package modeal.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CryptAes {
	
	public static void main (String[] args) {
		List<String> list = new ArrayList<String>();

		list.add("wjddms4107");
		printValue("P", list);
	}
	
	private static void printValue(String gubun, List<String> list) {
		try {
			System.out.println("Start");
			String result = "";
			for (int i = 0; i < list.size(); i++) {
				String str = list.get(i);
				if (i != 0) {
					result += "\n";
				}
				if ("E".equals(gubun.toUpperCase())) {
					result += str + "	" + CryptAes.encryptAES(str, ENC_KEY);
				} else if ("D".equals(gubun.toUpperCase())) {
					result += str + "	" + CryptAes.decryptAES(str, ENC_KEY);
				} else if ("P".equals(gubun.toUpperCase())) {
					String strSalt = generateSalt();
					result += str + "	" + getEncrypt(str, strSalt) + "	" + strSalt;
				}
			}
			System.out.println(result);
			System.out.println("End");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private static String getEncrypt(String source, String salt) {
        return getEncrypt(source, salt.getBytes());
    }
    
    private static String getEncrypt(String source, byte[] salt) {
        
        String result = "";
        
        byte[] a = source.getBytes();
        byte[] bytes = new byte[a.length + salt.length];
        
        System.arraycopy(a, 0, bytes, 0, a.length);
        System.arraycopy(salt, 0, bytes, a.length, salt.length);
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes);
            
            byte[] byteData = md.digest();
            
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xFF) + 256, 16).substring(1));
            }
            
            result = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    private static String generateSalt() {
        Random random = new Random();
        
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < salt.length; i++) {
            // byte 값을 Hex 값으로 바꾸기.
            sb.append(String.format("%02x",salt[i]));
        }
        
        return sb.toString();
    }
	
	private static String ENC_KEY = "makeulike@0!5";
	
	private static String decryptAES(String s, String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		if (s == null || s.length() == 0) {
			return s;
		}
		String decrypted = null;
		
		String[] key2 = key.split("");
		String[] key3 = new String[16];
		String key4 = "";
		
		for (int cnt=0; cnt < 13; cnt++) {
			if (key2[cnt] != null) {
				key3[cnt] = key2[cnt];
			}
		}
		
		for (int cnt2=13; cnt2 < 16; cnt2++) {
			key3[cnt2] = "\0";
		}
		key4 = arrayJoin("", key3);
		
		SecretKeySpec skeySpec = new SecretKeySpec(key4.getBytes(), "AES");
		
		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		decrypted = new String(cipher.doFinal(hexToByteArray(s)), "UTF-8");
		return decrypted.trim();
	}
	
	private static byte[] hexToByteArray(String s) {
		byte[] retValue = null;
		if (s != null && s.length() != 0) {
			retValue = new byte[s.length() / 2];
			for (int i = 0; i < retValue.length; i++) {
				retValue[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
			}
		}
		return retValue;
	}
	
	private static String byteArrayToHex(byte buf[]) {
		StringBuffer strbuf = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			strbuf.append(String.format("%02X", buf[i]));
		}
		
		return strbuf.toString();
	}
	
    private static String encryptAES(String text, String key)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
          if (text == null || text.length() == 0) {
            return text;
          }
          
          String[] key2 = key.split("");
          String[] key3 = new String[16];
          String key4 = "";
          
          for (int cnt=0; cnt < 13; cnt++) {
              if (key2[cnt] != null) {
                  key3[cnt] = key2[cnt];
              }
          }
          for (int cnt2=13; cnt2 < 16; cnt2++) {
              key3[cnt2] = "\0";
          }
          key4 = arrayJoin("", key3);
          
         // System.out.println(">>asd>>>"+key4.getBytes());
          
          String encrypted = null;
          byte[] source = text.getBytes("UTF-8");
          
          SecretKeySpec skeySpec = new SecretKeySpec(key4.getBytes("UTF-8"), "AES");
          
          Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
          cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
          int mod = source.length % 16;
          byte[] changeSource = null;
          if (mod != 0) {
            changeSource = new byte[source.length + (16 - mod)];
            System.arraycopy(source, 0, changeSource, 0, source.length);
          } else {
            changeSource = source;
          }
          encrypted = byteArrayToHex(cipher.doFinal(changeSource));
          
          return encrypted;
        }
	
	private static String arrayJoin(String glue, String array[]) {
		String result = "";
		
		for (int i = 0; i < array.length; i++) {
			result += array[i];
			if (i < array.length - 1)
				result += glue;
		}
		
	//	System.out.println("result::"+result+"::::");
		
		return result;
	}
}
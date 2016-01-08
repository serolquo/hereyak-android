package com.locution.hereyak;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import android.content.Context;
import android.provider.Settings;
import android.util.Base64;

#weak encryption and should be switched to use secure keystore

public class ObscureData {
	public static String obscureIt(Context context, String value) {
        try {
            final byte[] bytes = value!=null ? value.getBytes(Constants.UTF8) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(Constants.SERKEET));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(),Settings.System.ANDROID_ID).getBytes(Constants.UTF8), 20));
            return new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP),Constants.UTF8);

        } catch( Exception e ) {
            throw new RuntimeException(e);
        }

    }

	public static String unObscureIt(Context context, String value){
        try {
            final byte[] bytes = value!=null ? Base64.decode(value,Base64.DEFAULT) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(Constants.SERKEET));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(),Settings.System.ANDROID_ID).getBytes(Constants.UTF8), 20));
            return new String(pbeCipher.doFinal(bytes),Constants.UTF8);

        } catch( Exception e) {
            throw new RuntimeException(e);
        }
    }

}

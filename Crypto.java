import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;

public class Crypto {
	static {
		// Fail at startup instead of at runtime if any algorithm is unavailable
		messageDigest(new byte[0]);
		createSignature(generateKeyPair().getPrivate(), new byte[0]);
	}

	public static byte[] messageDigest(byte[] message) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(message);
			return md.digest();
		} catch (NoSuchAlgorithmException x) {
			x.printStackTrace(System.err);
		}
		System.exit(0);
		return null;
	}

	public static KeyPair generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024, SecureRandom.getInstance("SHA1PRNG"));
	 		return keyGen.generateKeyPair();
		} catch (NoSuchAlgorithmException x) {
			x.printStackTrace(System.err);
		}
		System.exit(0);
		return null;
	}

	public static byte[] createSignature(PrivateKey privKey, byte[] message) {
		try {
			Signature sign = Signature.getInstance("SHA256withRSA");
			sign.initSign(privKey);
			sign.update(message);
			return sign.sign();
		} catch (NoSuchAlgorithmException x) {
			x.printStackTrace(System.err);
		} catch (InvalidKeyException x) {
			x.printStackTrace();
		} catch (SignatureException x) {
			x.printStackTrace(System.err);
		}
		System.exit(0);
		return null;
	}

    public static boolean verifySignature(PublicKey pubKey, byte[] message, byte[] signature) {
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
			sign.initVerify(pubKey);
			sign.update(message);
			return sign.verify(signature);
		} catch (NoSuchAlgorithmException x) {
            x.printStackTrace();
		} catch (InvalidKeyException x) {
			x.printStackTrace();
		} catch (SignatureException x) {
			x.printStackTrace();
        }
		System.exit(0);
        return false;
    }
}

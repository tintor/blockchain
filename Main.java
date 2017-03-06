import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Security;
import java.security.Provider;

public class Main {
	static KeyPair[] keys = new KeyPair[10];
	static String[] names = { "alice", "bob", "carol", "david", "eve", "frank", "gaston", "henry", "igor", "john" };
	static Map<PublicKey, String> addressToName = new HashMap<PublicKey, String>();

	public static void main(String[] args) {
		for (int i = 0; i < keys.length; i++) {
			keys[i] = Crypto.generateKeyPair();
			addressToName.put(keys[i].getPublic(), names[i]);
		}

		// Generate two new coins with zero Tx
		Transaction t0 = new Transaction();
		t0.addOutput(100, keys[0].getPublic());
		t0.addOutput(100, keys[0].getPublic());
		TxHandler txh = new TxHandler(t0);
		print(txh);

		// Run first Tx!
		Transaction t = new Transaction();
		t.addOutput(20, keys[1].getPublic());
		t.addOutput(80, keys[0].getPublic());
		t.addInput(t0.hash(), 0, keys[0].getPrivate());

		txh.handleTxs(new Transaction[] { t });
		System.out.println("after tx:");
		print(txh);
	}

	static void print(TxHandler pool) {
		for (Transaction.Output txo : pool.getAll()) {
			System.out.printf("%s : %s\n", addressToName.get(txo.address), txo.value);
		}
	}
}

import java.util.Arrays;
import java.nio.ByteBuffer;

public class UTXO implements Comparable<UTXO> {
    /** Hash of the transaction from which this UTXO originates */
    private byte[] txHash;
    /** Index of the corresponding output in said transaction */
    private final int index;

    public UTXO(byte[] txHash, int index) {
		if (txHash.length != 32) {
			throw new IllegalArgumentException();
		}
        this.txHash = Arrays.copyOf(txHash, txHash.length);
        this.index = index;
    }

	public void put(ByteBuffer b) {
		assert txHash.length == 32;
		b.put(txHash);
		b.putInt(index);
	}

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        UTXO utxo = (UTXO) other;
		return index == utxo.index && Arrays.equals(txHash, utxo.txHash);
    }

    public int hashCode() {
        int hash = 17 + index;
        return hash * 31 + Arrays.hashCode(txHash);
    }

    public int compareTo(UTXO utxo) {
        if (utxo.index > index) return -1;
        if (utxo.index < index) return 1;

        int len1 = txHash.length;
        int len2 = utxo.txHash.length;
        if (len2 > len1) return -1;
        if (len2 < len1) return 1;
        for (int i = 0; i < len1; i++) {
            if (utxo.txHash[i] > txHash[i]) return -1;
            if (utxo.txHash[i] < txHash[i]) return 1;
		}
		return 0;
    }
}

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.PublicKey;

public class TxHandler {
	private final Map<UTXO, Transaction.Output> pool = new HashMap<UTXO, Transaction.Output>();

	public TxHandler(Transaction t0) {
		int output = 0;
		byte[] txHash = t0.hash();
		for (Transaction.Output txo : t0.outputs) {
			pool.put(new UTXO(txHash, output++), txo);
		}
	}

	public ArrayList<Transaction.Output> getAll() {
		ArrayList<Transaction.Output> result = new ArrayList<Transaction.Output>();
		for (UTXO utxo : pool.keySet()) {
			result.add(pool.get(utxo));
		}
		return result;
	}

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are positive, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // (1) all outputs claimed by {@code tx} are in the current UTXO pool,
		for (Transaction.Input txi : tx.inputs) {
            if (!pool.containsKey(txi)) {
                return false;
            }
        }
        // (2) the signatures on each input of {@code tx} are valid,
        for (Transaction.Input txi : tx.inputs) {
            if (!Crypto.verifySignature(pool.get(txi).address, tx.getRawDataToSign(txi), txi.signature)) {
                return false;
            }
        }
        // (3) no UTXO is claimed multiple times by {@code tx},
		HashSet<UTXO> claimed = new HashSet<UTXO>();
		for (Transaction.Input txi : tx.inputs) {
			if (claimed.contains(txi)) {
				return false;
			}
			claimed.add(txi);
        }
        // (4) all of {@code tx}s output values are positive, and
        for (Transaction.Output txo : tx.outputs) {
            if (txo.value <= 0) {
                return false;
            }
        }
        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
        //     values; and false otherwise.
		return txFee(tx) >= 0;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        List<Transaction> txs = new ArrayList<Transaction>();
		for (Transaction tx : possibleTxs) {
			if (isValidTx(tx)) {
				txs.add(tx);
				applyTx(tx);
			}
		}
		return txs.toArray(new Transaction[txs.size()]);
    }

	protected int txFee(Transaction tx) {
		int fee = 0;
        for (Transaction.Input txi : tx.inputs) {
            fee += pool.get(txi).value;
        }
        for (Transaction.Output txo : tx.outputs) {
            fee -= txo.value;
        }
		return fee;
	}

	protected void applyTx(Transaction tx) {
		if (!isValidTx(tx)) {
			throw new IllegalArgumentException();
		}
		for (Transaction.Input txi : tx.inputs) {
			pool.remove(txi);
		}
		byte[] hash = tx.hash();
		int output = 0;
		for (Transaction.Output txo : tx.outputs) {
			pool.put(new UTXO(hash, output++), txo);
		}
	}

	private static boolean overlap(Transaction ta, Transaction tb) {
		Set<UTXO> used = new HashSet<UTXO>();
		for (Transaction.Input txi : ta.inputs) {
			used.add(txi);
        }
		for (Transaction.Input txi : tb.inputs) {
			if (used.contains(txi)) {
				return true;
			}
        }
		return false;
	}

	/**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
	public Transaction[] handleTxsMaxFee(Transaction[] possibleTxs) {
        ArrayList<Transaction> txs = new ArrayList<Transaction>();
		Graph<Transaction> graph = new Graph<Transaction>();
		for (Transaction tx : possibleTxs) {
			if (isValidTx(tx)) {
				txs.add(tx);
				graph.addNode(tx, txFee(tx));
			}
		}

		for (int i = 0; i < txs.size(); i++) {
			Transaction ta = txs.get(i);
			for (int j = i + 1; j < txs.size(); j++) {
				Transaction tb = txs.get(j);
				if (overlap(ta, tb)) {
					graph.addEdge(ta, tb);
				}
			}
		}

		List<Transaction> bestTxs = graph.maxDisconnectedSubset();
		for (Transaction tx : bestTxs) {
			applyTx(tx);
		}
		return bestTxs.toArray(new Transaction[bestTxs.size()]);
    }
}

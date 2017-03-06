import java.util.ArrayList;
import java.util.Arrays;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.nio.ByteBuffer;

public class Transaction {
    public static class Input extends UTXO {
		/** the signature produced to check validity */
        public byte[] signature;

        public Input(byte[] hash, int idx) {
            super(hash, idx);
        }
    }

	public static class Output {
		public final int value;
		public final PublicKey address;

		public Output(int v, PublicKey a) {
			value = v;
			address = a;
		}
	}

    public final ArrayList<Input> inputs = new ArrayList<Input>();
    public final ArrayList<Output> outputs = new ArrayList<Output>();

	public void addOutput(int value, PublicKey address) {
        outputs.add(new Output(value, address));
    }

	public void addInput(byte[] txHash, int index, PrivateKey privKey) {
		assert outputs.size() > 0;
		Input input = new Input(txHash, index);
        inputs.add(input);
		input.signature = Crypto.createSignature(privKey, getRawDataToSign(input));
	}

    public byte[] getRawDataToSign(Input in) {
        // one input and all outputs
		int addressSize = (outputs.size() > 0) ? outputs.get(0).address.getEncoded().length : 0;
		int intSize = Integer.SIZE / 8;
		int bytes = 32 + intSize + outputs.size() * (intSize + addressSize);
        ByteBuffer b = ByteBuffer.allocate(bytes);

		in.put(b);
        for (Output op : outputs) {
            b.putInt(op.value);
			assert op.address.getEncoded().length == addressSize;
			b.put(op.address.getEncoded());
        }
        return b.array();
    }

    public byte[] getRawTx() {
		// all inputs and all outputs
		int signatureSize = (inputs.size() > 0) ? inputs.get(0).signature.length : 0;
		int addressSize = (outputs.size() > 0) ? outputs.get(0).address.getEncoded().length : 0;
		int intSize = Integer.SIZE / 8;
		int bytes = intSize + inputs.size() * (32 + intSize + signatureSize) + outputs.size() * (intSize + addressSize);
        ByteBuffer b = ByteBuffer.allocate(bytes);

		b.putInt((int)inputs.size());
        for (Input in : inputs) {
			in.put(b);
			assert in.signature.length == signatureSize;
            b.put(in.signature);
        }
        for (Output op : outputs) {
            b.putInt(op.value);
			assert op.address.getEncoded().length == addressSize;
			b.put(op.address.getEncoded());
        }
        return b.array();
    }

    public byte[] hash() {
		return Crypto.messageDigest(getRawTx());
    }
}

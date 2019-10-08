import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Scrambler {

	private final static void scramble(File inFile, File outFile) {
		BufferedReader in = null;
		DataOutputStream out = null;

		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "UTF-8"));
			out = new DataOutputStream(new FileOutputStream(outFile));

			String line = in.readLine();

			while (line != null) {
				int[] data = scrambleLine(line);

				for (int d : data) {
					out.writeInt(d);
				}

				out.writeInt('\n');
				line = in.readLine();
			}

			line = in.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
			try {
				out.close();
			} catch (IOException e) {
			}
		}
	}


	private final static int[] scrambleLine(String line) {
		boolean badSeed = true;
		byte[] bytes = line.getBytes();
		int[] data = new int[bytes.length + 2];

		while (badSeed) {
			badSeed = false;
			int seed1 = (int) (Math.random() * 65535);
			int seed2 = (int) (Math.random() * 65535);
			data[0] = seed1;
			data[data.length - 1] = seed2;

			for (int i = 0; i < bytes.length; i++) {
				int d = bytes[i] + seed1 - seed2;

				if (d == '\n') {
					badSeed = true;
					break;
				}

				data[i + 1] = d;
			}
		}

		return data;
	}


	private static final void unscramble(File inFile, File outFile) {
		DataInputStream in = null;
		BufferedWriter out = null;

		try {
			in = new DataInputStream(new FileInputStream(inFile));
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));

			int d = in.readInt();
			int[] buf = new int[4096];

			while (in.available() > 0) {
				int i = 0;

				while (d != '\n') {
					buf[i] = d;
					i++;
					d = in.readInt();
				}

				int[] data = new int[i];
				System.arraycopy(buf, 0, data, 0, i);

				String line = unscrambleLine(data);
				out.write(line);
				out.write("\r\n");

				i++;
				d = in.readInt();
			}
		} catch (EOFException e) {

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
			try {
				out.close();
			} catch (IOException e) {
			}
		}
	}


	private static final String unscrambleLine(int[] data) {
		int seed1 = data[0];
		int seed2 = data[data.length - 1];
		byte[] bytes = new byte[data.length - 2];

		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (data[i + 1] - seed1 + seed2);
		}

		return new String(bytes);
	}


	static void test() {
		String str = "Dies ist ein Test\nUnd noch einer\n";
		int[] data = scrambleLine(str);
		String line = unscrambleLine(data);
		System.out.println(line);
	}


	public static void main(String[] args) {
		if (args == null || args.length < 3) {
			System.err.println("SYNTAX:\n\tScambler [-s | -u] <file-in> <file-out>\n");
			System.exit(1);
		}

		File inFile = new File(args[1]);

		if (!inFile.exists()) {
			System.err.println("File '" + args[1] + "' doesn't exist!");
			System.exit(2);
		}

		File outFile = new File(args[2]);

		if (args[0].equalsIgnoreCase("-S")) {
			scramble(inFile, outFile);
		} else if (args[0].equalsIgnoreCase("-U")) {
			unscramble(inFile, outFile);
		} else {
			System.err.println("SYNTAX:\n\tScambler [-s | -u] <file-in> <file-out>\n");
			System.exit(1);
		}
	}
}

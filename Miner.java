import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Set;

/**
 * Class for a miner.
 */
public class Miner extends Thread {
	Integer hashCount;
	Set<Integer> solved;
	CommunicationChannel channel;
	private Object lock = new Object();

	/**
	 * Creates a {@code Miner} object.
	 * 
	 * @param hashCount
	 *            number of times that a miner repeats the hash operation when
	 *            solving a puzzle.
	 * @param solved
	 *            set containing the IDs of the solved rooms
	 * @param channel
	 *            communication channel between the miners and the wizards
	 */
	public Miner(Integer hashCount, Set<Integer> solved, CommunicationChannel channel) {
		this.hashCount = hashCount;
		this.solved = solved;
		this.channel = channel;
	}

	private static String encryptMultipleTimes(String input, Integer count) {
		String hashed = input;
		for (int i = 0; i < count; ++i) {
			hashed = encryptThisString(hashed);
		}
		return hashed;
	}

	private static String encryptThisString(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

			// convert to string
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xff & messageDigest[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public void run() {
		Message message;
		int parentRoom = 0, currentRoom = 0;
		boolean needToHash = false;
		while (true) {
			message = channel.getMessageWizardChannel();
			if (message.getData().contains("EXIT"))
				return;
			if (message.getData().contains("END"))
				needToHash = false;
			else {
				parentRoom = message.getCurrentRoom();
				message = channel.getMessageWizardChannel();
				currentRoom = message.getCurrentRoom();
				synchronized(lock) {
					if (solved.contains(currentRoom))
						needToHash = false;
					else {
						solved.add(currentRoom);
						needToHash = true;
					}
				}

			}	
			if (needToHash) {
				String data = encryptMultipleTimes(message.getData(), hashCount);
				channel.putMessageMinerChannel(new Message(parentRoom, currentRoom, data));
			}

		}

	}
}
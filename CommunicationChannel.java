import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that implements the channel used by wizards and miners to communicate.
 */
public class CommunicationChannel {
	LinkedBlockingQueue<Message> wizardChannel = new LinkedBlockingQueue<>();
	LinkedBlockingQueue<Message> minerChannel = new LinkedBlockingQueue<>();
	ReentrantLock lock1 = new ReentrantLock();
	ReentrantLock lock2 = new ReentrantLock();
	int checkPut = 0, checkGet = 0;

	public CommunicationChannel() {
	}
	/**
	 * Puts a message on the miner channel (i.e., where miners write to and wizards
	 * read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageMinerChannel(Message message) {
		minerChannel.offer(message);
	}

	/**
	 * Gets a message from the miner channel (i.e., where miners write to and
	 * wizards read from).
	 * 
	 * @return message from the miner channel
	 */
	public Message getMessageMinerChannel() {
		return minerChannel.poll();
	}

	/**
	 * Puts a message on the wizard channel (i.e., where wizards write to and miners
	 * read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageWizardChannel(Message message) {
		lock2.lock();
		try {
			wizardChannel.offer(message);
			if (message.getData().contains("END")) {
				lock2.unlock();
				return;
			}
			else if (message.getData().contains("EXIT")) {			
				lock2.unlock();
				return;
			}
			else
				checkPut ++ ;
		} finally {
			if(checkPut == 2) {
				lock2.unlock();
				lock2.unlock();
				checkPut = 0;
			}
		}
	}

	/**
	 * Gets a message from the wizard channel (i.e., where wizards write to and
	 * miners read from).
	 * 
	 * @return message from the miner channel
	 */
	public Message getMessageWizardChannel() {
		lock1.lock();
		Message msg = wizardChannel.poll();
		while(msg == null)
			msg = wizardChannel.poll();
		if (msg.getData().contains("END")) {
			lock1.unlock();
			return msg;
		}
		else if (msg.getData().contains("EXIT")) {			
			lock1.unlock();
			return msg;
		}
		else {
			checkGet ++;
			if(checkGet == 2) {
				lock1.unlock();
				lock1.unlock();
				checkGet = 0;
			}
			return msg;
		}		
	}
}

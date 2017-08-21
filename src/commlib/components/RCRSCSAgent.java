package commlib.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import commlib.message.RCRSCSMessage;
import commlib.message.RCRSCSMessageConverter;

import rescuecore2.messages.Command;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.messages.AKSpeak;

/**
 * The RCRCSAgent represent The Abstract Agent that have method that convert
 * messages by using this library.
 * 
 * @author takefumi
 * 
 * @param <E>
 */
abstract class RCRSCSAgent<E extends StandardEntity> extends StandardAgent<E> {
	RCRSCSMessageConverter messageConverter;
	private List<RCRSCSMessage> messageList;
	protected List<RCRSCSMessage> receivedMessageList;
	protected int[] subscribedChannels;
	private int messageChannel;

	/**
	 * Constructor<br>
	 * Prepare sended message list and received message list. Additionally,
	 * initialize 'message channel' = -1
	 */
	public RCRSCSAgent() {
		this.messageList = new ArrayList<RCRSCSMessage>();
		this.receivedMessageList = new ArrayList<RCRSCSMessage>();
		this.messageChannel = -1;
	}

	@Override
	/**
	 * Initialize message converter.
	 */
	protected void postConnect() {
		super.postConnect();
		this.messageConverter = new RCRSCSMessageConverter(me().getID(),
				this.model, this.config);
	}

	/**
	 * Set the message channel used in sending messages.
	 * 
	 * @param channel
	 */
	protected final void setMessageChannel(int channel) {
		this.messageChannel = channel;
	}

	/**
	 * Get the using message channel.
	 * 
	 * @return channel
	 */
	protected final int getMessageChannel() {
		return this.messageChannel;
	}

	/**
	 * Receive messages.<br>
	 * In this method, only messages sended from the set channel will be
	 * converted.
	 * 
	 * @param heard
	 */
	protected final void receiveMessage(Collection<Command> heard) {
		this.receivedMessageList = new ArrayList<RCRSCSMessage>();
		for (Command command : heard) {
			if (command instanceof AKSpeak) {
				AKSpeak speak = (AKSpeak) command;
				// System.out.println("bit size " + speak.getContent().length *
				// 8);
				if (speak.getChannel() == this.messageChannel) {
					this.receivedMessageList.addAll(this.messageConverter
							.bytesToMessageList(speak.getContent()));
				}else{
					for (int i=0; i < subscribedChannels.length; i++){
						if(speak.getChannel() == subscribedChannels[i]){
							this.receivedMessageList.addAll(this.messageConverter
									.bytesToMessageList(speak.getContent()));
						}
					}
				}
			}
		}
	}

	/**
	 * Add sended message.
	 * 
	 * @param message
	 */
	protected final void addMessage(RCRSCSMessage message) {
		messageList.add(message);
	}

	/**
	 * Send message by using the set channel.
	 * 
	 * @param time
	 */
	protected final void sendMessage(int time) {
		byte[] data = this.messageConverter.messageToBytes(this.messageList);
		if (data != null) {
			super.sendSpeak(time, this.messageChannel, data);
		}
	}

	@Override
	/**
	 * If specified channel equals the set channel, execute sendMessage method.
	 * Otherwise, execute sendSpeak method.
	 */
	protected final void sendSpeak(int time, int channel, byte[] data) {
		if (channel == this.messageChannel) {
			this.sendMessage(time);
		} else {
			super.sendSpeak(time, this.messageChannel, data);
		}
	}

	protected final int getMessageSize() {
		return this.messageList.size();
	}
}
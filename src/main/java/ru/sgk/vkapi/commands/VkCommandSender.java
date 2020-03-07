package ru.sgk.vkapi.commands;

import java.util.List;

import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.users.UserXtrCounters;

import ru.sgk.vkapi.MainVkAPI;
/**
 * some information about the user in the chat
 * @author 1
 *
 */
public class VkCommandSender 
{
	private int chatId;
	private int userId;
	private String firstName;
	private String lastName;
	private UserActor actor;
	
	public VkCommandSender(int userId, int chatId) throws ApiException, ClientException 
	{
    	if (MainVkAPI.getVkClient() == null)
    		throw new NullPointerException("API is not initialized");
		this.userId = userId;
		this.chatId = chatId;
		this.actor = new UserActor(userId, MainVkAPI.getAccessToken());
		List<UserXtrCounters> counters = 
				MainVkAPI.getVkClient().users().get(MainVkAPI.getGroupActor()).userIds(String.valueOf(actor.getId())).execute();
		this.firstName = counters.get(0).getFirstName();
		this.lastName = counters.get(0).getLastName();
	}
	/**
	 * Send message to user which send command
	 * @param message - message to send 
	 */
	public void sendResponse(String message)
	{
    	if (MainVkAPI.getVkClient() == null)
    		throw new NullPointerException("API is not initialized");
		try {
			MainVkAPI.getVkClient().messages().send(actor).peerId(chatId).message(message).execute();
		} catch (ApiException e) {
			e.printStackTrace();
		} catch (ClientException e) {
			e.printStackTrace();
		}
	}
	public int getUserId() {
		return userId;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public UserActor getActor() {
		return actor;
	}
	
	
}

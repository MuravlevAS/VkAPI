package ru.sgk.vkapi.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.JsonObject;
import com.vk.api.sdk.actions.LongPoll;
import com.vk.api.sdk.callback.longpoll.queries.GetLongPollEventsQuery;
import com.vk.api.sdk.callback.longpoll.responses.GetLongPollEventsResponse;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.responses.GetLongPollServerResponse;
import com.vk.api.sdk.queries.groups.GroupsGetLongPollServerQuery;

import ru.sgk.vkapi.MainVkAPI;

public class VkCommandHandler 
{
	private static String commandPrefix;
	private static Map<String, VkCommandExecutor> commands;
	private static ScheduledExecutorService execService;
	private static ScheduledFuture<?> future = null;
	private static String key;
	private static String server;
	private static Integer ts;
	private static ScheduledFuture<?> refreshFuture = null;
	private static ReentrantLock locker = new ReentrantLock();
	
	public static void setCommandPrefix(String prefix)
	{
    	if (MainVkAPI.getVkClient() == null)
    		throw new NullPointerException("API is not initialized!");
    	if (execService == null)
    		throw new NullPointerException("Command handler is not initialized!");
		if (prefix == null) commandPrefix = "";
		else commandPrefix = prefix;
	}
	/**
	 * @throws NullPointerException if API was not initialized
	 */
	public static void init()
	{
    	if (MainVkAPI.getVkClient() == null)
    		throw new NullPointerException("API is not initialized");
		commandPrefix = "";
		commands = new HashMap<>();
		execService = Executors.newScheduledThreadPool(2);
		GroupsGetLongPollServerQuery query = MainVkAPI.getVkClient().groups().getLongPollServer(MainVkAPI.getGroupActor());
		try {
			GetLongPollServerResponse response = query.execute();
			key = response.getKey();
			server = response.getServer();
			ts = response.getTs();
		} catch (ApiException e) {
			e.printStackTrace();
		} catch (ClientException e) {
			e.printStackTrace();
		}
	}
	
	private static void invoke()
	{
		locker.lock();
    	if (MainVkAPI.getVkClient() == null)
    		throw new NullPointerException("API is not initialized");
		try 
		{	
			LongPoll longPoll = new LongPoll(MainVkAPI.getVkClient());
			GetLongPollEventsQuery events = longPoll.getEvents(server, key, ts);
			GetLongPollEventsResponse eResponse = events.execute();
			List<JsonObject> objList =  eResponse.getUpdates();
			for (JsonObject obj : objList)
			{
				if (obj.getAsJsonPrimitive("type").getAsString().equalsIgnoreCase("message_new"))
				{
					JsonObject object = obj.getAsJsonObject("object");
					String fullText = object.getAsJsonPrimitive("text").getAsString();
					if (fullText.startsWith(commandPrefix) || commandPrefix.equals("") || commandPrefix == null)
					{
						fullText = fullText.replaceFirst(commandPrefix, "");
						String[] fullTextArr = fullText.split(" ");
						
						if (fullTextArr.length>0 && commands.containsKey(fullTextArr[0].toLowerCase()))
						{
							String command = fullTextArr[0];
							if (commands.containsKey(command))
							{
								String[] args;
								if (command.length() == 1)
								{
									args = new String[0];
								}
								else args = Arrays.copyOfRange(fullTextArr, 1, fullTextArr.length);
								int userId = object.getAsJsonPrimitive("from_id").getAsInt();
								int chatId = object.getAsJsonPrimitive("peer_id").getAsInt();
								VkCommandSender sender = new VkCommandSender(userId, chatId);
								commands.get(command).onCommand(command, sender, args);
							}
						}
						
					}
				}
			}

			ts = eResponse.getTs();
		}
		catch (ApiException e) 
		{
			e.printStackTrace();
		}
		catch (ClientException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			locker.unlock();
		}
	}
	private static void refreshKeys()
	{
		locker.lock();
		System.out.println("Server key is expired. Generating new key ");
		GroupsGetLongPollServerQuery query = MainVkAPI.getVkClient().groups().getLongPollServer(MainVkAPI.getGroupActor());
		try {
			GetLongPollServerResponse response = query.execute();
			key = response.getKey();
			server = response.getServer();
			ts = response.getTs();
			System.out.println("New server key has generated.");
		} catch (ApiException e) {
			e.printStackTrace();
		} catch (ClientException e) {
			e.printStackTrace();
		}
		locker.unlock();
	}
	public static void addCommand(String commandName, VkCommandExecutor executor)
	{
    	if (MainVkAPI.getVkClient() == null)
    		throw new NullPointerException("API is not initialized");
		commands.put(commandName.toLowerCase(), executor);
		if (future == null)
			future = execService.scheduleAtFixedRate(() -> invoke(), 0, 50, TimeUnit.MILLISECONDS);
		if (refreshFuture == null)
			refreshFuture = execService.scheduleAtFixedRate(() -> refreshKeys(), 0, 30, TimeUnit.MINUTES);
	}
	
	private static void stop()
	{
		System.out.println("Vk command handler has stopped");
		if (future != null)
			future.cancel(false);
		if (refreshFuture == null)
			refreshFuture.cancel(false);
	}
	public static void destroy()
	{
		stop();
		future = null;
		refreshFuture = null;
		execService.shutdown();
		execService = null;
	}
}

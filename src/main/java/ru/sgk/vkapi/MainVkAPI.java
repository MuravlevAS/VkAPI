package ru.sgk.vkapi;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;

import ru.sgk.vkapi.commands.VkCommandHandler;

/**
 * Hello world!
 *
 */
public class MainVkAPI 
{
	private static int groupId = 0; 
	private static String accessToken = null;
	private static GroupActor actor = null;
	
	private static VkApiClient vkClient = null;
	
	private static int adminId = 0;
	private static String adminAccessToken = null;
	
	/**
	 * Initializing of API
	 * how to get access tokens you can see in vk docs - <a href="https://vk.com/dev/access_token?f=1.%20Ключ%20доступа%20пользователя">vk.com/dev/access_token</a> 
	 * @param groupId - group if
	 * @param accessToken access token for group
	 * @param adminId group admin ID
	 * @param adminAccessToken access token for admin
	 */
	public static void init( int groupId, String accessToken, int adminId, String adminAccessToken)
	{
		MainVkAPI.groupId = groupId;
		MainVkAPI.accessToken = accessToken;
		MainVkAPI.adminId = adminId; 
		MainVkAPI.adminAccessToken = adminAccessToken;
		TransportClient client = new HttpTransportClient();
		vkClient = new VkApiClient(client);
		actor = new GroupActor(groupId, accessToken);
		VkCommandHandler.init();
		
	}
	public static void main(String[] args) 
	{
		
	}
	/**
	 * setting status to group
	 * @param status - status to set
	 */
    public static void setGroupStatus(String status)
    {
    	if (vkClient == null)
    		throw new NullPointerException("API is not initialized");
    	try 
    	{
			UserActor actor = new UserActor(adminId, adminAccessToken);
			vkClient.status().set(actor).groupId(MainVkAPI.getGroupId()).groupId(MainVkAPI.getGroupId()).text(status).execute();
		}
    	catch (ApiException e) 
    	{
			e.printStackTrace();
		}
    	catch (ClientException e)
    	{
			e.printStackTrace();
		}
    }
    /**
     * uninitializing API: uninitializing command handler, null'ing all variables 
     */
    public static void destroy()
    {
    	if (vkClient == null)
    		throw new NullPointerException("API is not initialized");
    	VkCommandHandler.destroy();
    	groupId = 0;
    	accessToken = null;
    	actor = null;
    	vkClient = null;
    	adminId = 0;
    	adminAccessToken = null;
    	
    }

	public static int getGroupId() 
	{
    	if (vkClient == null)
    		throw new NullPointerException("API is not initialized");
		return groupId;
	}

	public static String getAccessToken() 
	{
    	if (vkClient == null)
    		throw new NullPointerException("API is not initialized");
		return accessToken;
	}

	public static GroupActor getGroupActor() 
	{
    	if (vkClient == null)
    		throw new NullPointerException("API is not initialized");
		return actor;
	}
    
    public static VkApiClient getVkClient()
    {
    	if (vkClient == null)
    		throw new NullPointerException("API is not initialized");
    	return vkClient;
    }

	public static int getAdminId() 
	{
    	if (vkClient == null)
    		throw new NullPointerException("API is not initialized");
		return adminId;
	}

	public static String getAdminAccessToken() 
	{
    	if (vkClient == null)
    		throw new NullPointerException("API is not initialized");
		return adminAccessToken;
	}
    
    
}

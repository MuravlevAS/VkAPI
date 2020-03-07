package ru.sgk.vkapi.commands;

public interface VkCommandExecutor  
{
	public void onCommand(String commandName, VkCommandSender sender, String args[]);
}

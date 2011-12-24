package net.ardvaark.jackbot;

import org.codehaus.classworlds.ClassWorld;

public class JackBotLauncher
{
    public static final String REALM_CORE = "jackbot.core";
    
    public static void main(String[] args) throws Exception
    {
        ClassWorld world = new ClassWorld();
        world.newRealm(REALM_CORE, JackBotLauncher.class.getClassLoader());
        main(args, world);
    }
    
    public static void main(String[] args, ClassWorld world) throws Exception
    {
        JackBot bot = new JackBot();
        bot.setClassWorld(world);
        bot.run(args);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bluC;

import java.util.HashMap;

/**
 *
 * @author John Schneider
 */
public class Flags
{
    private static HashMap<String, String> flags = new HashMap<>();
    
    public static void set(String flagName, String value)
    {
        flags.put(flagName, value);
    }
    
    public static String get(String flagName)
    {
        return flags.get(flagName);
    }
}

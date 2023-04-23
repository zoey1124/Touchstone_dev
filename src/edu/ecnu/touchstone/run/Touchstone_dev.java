package edu.ecnu.touchstone.run;

public class Touchstone_dev{
    public static void main(String[] args) {
        // args[0]: the path of the configuration file 
        if (args.length != 1) {
			System.out.println("Please specify the configuration file for Touchstone!");
			System.exit(0);
		} 
        try {
            Configurations configurations = new Configurations(args[0]);
            // parse SQL file 
    
            // run controller
            String[] cmd = new String[]{"zsh", "-c", ""};
            Process process = Runtime.getRuntime().exec(cmd);

            // run data generator 
    
            // run db connector
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
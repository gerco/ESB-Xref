package nl.progaia.esbxref.launcher;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class Launcher {

	public final static String PREF_MQ_HOME = "com.sonicsw.mq.home";
	public final static String PREF_XQ_HOME = "com.sonicsw.xq.home";
	
	private final static String[] myJars = new String[] {
		"ESBXref-core.jar",
		"MFUtils-1.0-SNAPSHOT.jar"
	};
	
	private final static String[] mqJars = new String[] {
		"sonic_Client.jar",
		"sonic_Crypto.jar",
		"mgmt_client.jar",
		"mgmt_config.jar",
		"sonic_mgmt_client.jar",
		"xbean.jar",
		"commons-logging.jar",
		"xercesImpl.jar",
		"MFdirectory.jar",
		"mfcontext.jar"
	};
	
	private final static String[] xqJars = new String[] {
		"xq_config.jar",
		"xq_core.jar",
		"jsr173_api.jar",
		"sonic_deploy.jar"
	};
	
	private static final String MAIN_CLASS = "nl.progaia.esbxref.ESBXref";
	
	private static Preferences prefs = Preferences.userNodeForPackage(Launcher.class);
	
	/**
	 * Find out the correct classpath for ESB and MQ and then launch! If the classpath
	 * is not yet set correctly, ask for it and store in prefs.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
//		System.out.println(Thread.currentThread().getContextClassLoader());
		
		do {
			String mqHome = prefs.get(PREF_MQ_HOME, "");
			String xqHome = prefs.get(PREF_XQ_HOME, "");
			
			boolean mqHomeGood = new File(new File(mqHome), "lib/sonic_Client.jar").exists();
			boolean xqHomeGood = new File(new File(xqHome), "lib/xq_core.jar").exists();

			if(mqHomeGood && xqHomeGood) {
				try {
					System.setProperty("com.sonicsw.xq.home", xqHome);
					setClassLoaderAndLaunch(
							new File(mqHome + "/lib"), 
							new File(xqHome + "/lib"), 
							args);
				} catch (MalformedURLException e) {
					// Something is wrong!
					e.printStackTrace();
					System.exit(1);
				}
				
				// When we get here, the app was launched successfully. Break
				// the loop because we don't want to ask the user for the dirs
				// anymore.
				break;
			}
		} while (askUserForHomeLocations());
	}

	/**
	 * Put the correct jars from mqHome and xqHome on the classpath and launch.
	 * @throws MalformedURLException 
	 */
	private static void setClassLoaderAndLaunch(File mqLib, File xqLib, String[] args) throws MalformedURLException {
		List<URL> urls = new ArrayList<URL>();
		
		// Construct the list of URLs from the jar names
		for(String name: myJars) {
			urls.add(new File(name).toURL());
		}
		for(String name: mqJars) {
			urls.add(new File(mqLib, name).toURL());
		}
		for(String name: xqJars) {
			urls.add(new File(xqLib, name).toURL());
		}
		
//		System.out.println("Class path: " + urls);
		
		// Set the Thread context classloader to the new classloader. New classes will be 
		// loaded using this classloader.
		ClassLoader newClassLoader = URLClassLoader.newInstance(
				urls.toArray(new URL[0]), 
				Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(newClassLoader);
//		System.out.println(Thread.currentThread().getContextClassLoader());
		
		// Now load and launch the main class with the new classloader
		try {
			Class<?> mainClass = newClassLoader.loadClass(MAIN_CLASS);
			Method mainMethod = mainClass.getDeclaredMethod("main", new Class[] {String[].class});
			mainMethod.invoke(null, new Object[] {args});
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unable to load the main class " + MAIN_CLASS, e);
		} catch (SecurityException e) {
			throw new RuntimeException("Unable to load the main class " + MAIN_CLASS, e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Unable to load the main class " + MAIN_CLASS, e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to invoke the main method", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to invoke the main method", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Unable to invoke the main method", e);
		}
	}

	/**
	 * Open a dialog to ask the user for the location of MQHOME and XQHOME.
	 * 
	 * @return True when the user confirmed. False when the user canceled launch. 
	 */
	private static boolean askUserForHomeLocations() {
		SonicLocationsDialog dialog = new SonicLocationsDialog();
		try {
			return dialog.askForSonicLocations();
		} finally {
			dialog.dispose();
		}
	}
	
}

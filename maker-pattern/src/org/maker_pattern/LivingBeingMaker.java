/**
 * Mar 14, 2014 File created by Ramiro.Serrato
 */
package org.maker_pattern;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.maker_pattern.animals.Cat;
import org.maker_pattern.animals.Dog;
import org.maker_pattern.animals.DogFamily;
import org.maker_pattern.plants.Ceiba;
import org.maker_pattern.plants.Daisy;
import org.maker_pattern.plants.Plant;

/**
 * A very basic, light-weight, pragmatic programmatic alternative to DI frameworks 
 *  Maker Pattern versions: 
 *  0.1 First version using an external class that included a Map storing the singleton instances and getInstance method, supporting 
 *      singleton and prototype beans
 *  0.2 Redesigned the pattern, now everything is in a single class (enum) and uses kind of factory methods for retrieving instances
 *  	This version is also capable of wiring beans inside the same enum and cross enum wiring too, allowing us to separate the beans
 *      by domain or responsibility in different files and wire them if needed
 *  0.3 Added init, start, shutdown, clear and clearAll life cycle methods
 *  0.4 Added Properties parameter to getInstance method for supporting initialization properties
 *  0.5 Removed init method as it was not really useful
 * @author Ramiro.Serrato
 *
 */
public enum LivingBeingMaker {
	
	/*** Object configuration, definition, wiring ***/
	//  Pattern:   NAME (isSingleton) { createInstance() { <object creation logic > } }

	SPARKY_DOG (true) {
		@Override
		public Dog makeInstance(Properties properties) {
			Dog sparky = new Dog();
			sparky.setName(properties.getProperty("sparky.name"));
			return sparky;
		}
	},

	PINKY_DOG (true) {
		@Override
		public Dog makeInstance(Properties properties) {
			Dog pinky = new Dog();
			pinky.setName(properties.getProperty("pinky.name"));
			return pinky;
		}
	},
	
	SPARKY_FAMILY (true) {  // A wired object
		@Override
		public DogFamily makeInstance(Properties properties) {
			DogFamily dogFamily = new DogFamily();
			Dog parent1 = get(SPARKY_DOG);
			Dog parent2 = get(PINKY_DOG);
			dogFamily.setParent1(parent1);
			dogFamily.setParent2(parent2);
			return dogFamily;
		}
	},
		
	SIAMESE (false)  { @Override public Cat makeInstance(Properties properties) { return new Cat(); } },
	LABRADOR (false) { @Override public Dog makeInstance(Properties properties) { return new Dog(); } },
	
	DAISY (false) { 
		@Override	
		public Plant makeInstance(Properties properties) {	
			Daisy daisy = new Daisy();
			daisy.setKind(properties.getProperty("daisy_kind"));
			return daisy;	
		} 
	},
	
	CEIBA (false) { 
		@Override 
		public Plant makeInstance(Properties properties) { 
			Ceiba ceiba = new Ceiba();
			ceiba.setAge(new Integer(properties.getProperty("ceiba_age")));
			return ceiba;	
		} 
	};
	
	static {  // here you can define static stuff like properties or xml loaded configuration 
		findHooks(); // this line is necessary for supporting MakerHook
		
	    Properties livingBeingroperties = new Properties();
		
		try { // load plant and animal configuration from xml and properties files
			livingBeingroperties.loadFromXML(LivingBeingMaker.class.getResourceAsStream("resources/plant-config.xml"));
			livingBeingroperties.load(LivingBeingMaker.class.getResourceAsStream("resources/animal-config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException(e.getMessage());
		}
		
		generalProperties = livingBeingroperties;
	}
	
	/**** From here framework code, change this only if you know what you are doing ****/	
	
	public static <T> T get(LivingBeingMaker livingBeingMaker) {
		return livingBeingMaker.getInstance(generalProperties);
	}
	
	private static void findHooks() {
		String packageName = System.getProperty("maker.hooks.package");
		packageName = (packageName == null) ? LivingBeingMaker.class.getPackage().getName() : packageName; // if the property is not set it will use current package
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		try {
	        File[] dirContent = new File(new URI(classLoader.getResource(packageName.replace(".", "/")).toString()).getPath()).listFiles();

	        for(File file: dirContent){
	            String currentName = file.getName();
	            if(currentName.contains(".class")) {
	            	Class<?> aHook = classLoader.loadClass(packageName + "." + currentName.substring(0, currentName.lastIndexOf(".")));
	            	if(!aHook.isInterface() && MakerHook.class.isAssignableFrom(aHook)) {
	            		Class.forName(aHook.getName(), true, classLoader);  // load and initialize
	            	}
	            }
	        }
		} catch (URISyntaxException | ClassNotFoundException e) {
			throw new IllegalStateException("Error while trying to load MakerHook components", e);
		}
	}

	protected static Properties generalProperties;
	
	private LivingBeingMaker(Boolean singleton) {
		this.singleton = singleton;
	}
	
	private Boolean singleton;
	private Object instance;
	protected MakerHook hook;
	
	public Boolean isSingleton() {
		return singleton;
	}
	
	public void setHook(MakerHook hook) {
		this.hook = hook;
	}
	
	/**
	 * This is the method that handles the creation of instances based on the flag singleton it 
	 *  will create a singleton or a prototype instance
	 * @param properties A Properties object that may contain information needed for the instance creation
	 * @return The instance as a generic Object
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getInstance(Properties properties) {
		T localInstance = (T) instance;
		
		if (singleton) {
			if (instance == null) {
				synchronized (this) {
					if (instance == null) {
						instance = (hook != null) ? hook.makeInstance() : this.makeInstance(properties);
						localInstance = (T) instance;
						start(localInstance);
					}
				}
			}
		}
			
		else {
			localInstance = (T) (hook != null ? hook.makeInstance() : this.makeInstance(properties));
			start(localInstance);
		}
					
		return localInstance;
	}
	
	/**
	 * Similar to {@link #init()} method, the difference is that the logic here will be executed each time an instance
	 *  is created, for prototypes that means every time the getInstance method is called, for singletons only the first
	 *  time the getInstance method is called
	 */
	protected void start(Object object) {
		;
	}
	
	/**
	 * You can put some shutdown logic here, this method can be overriden inside the enum value definition. 
	 *  Shutdown logic can be: external resources release, clear chache, etc. 
	 */
	public void shutdown() throws Exception {
		;
	}
	
	/**
	 * This method will call the {@link #shutdown()} method for all the enums in the maker
	 * @throws Exception 
	 */
	public void shutdownAll() throws Exception {
		for (LivingBeingMaker value : LivingBeingMaker.values()) {
			  value.shutdown();
		}
	}
	
	/**
	 * This method will set the instance to null, useful for reseting singleton instances
	 */
	public void clear() {
		instance = null;
	}
	
	/**
	 * Will call the {@link #clear()} method for all the enums defined in the maker
	 */
	public void clearAll() {
		for (LivingBeingMaker value : LivingBeingMaker.values()) {
			  value.clear();
		}
	}
	
	/**
	 * This method contains the logic for creating the instance, it receives a Properties
	 *  object as a parameter that may contain initial properties for creating the instances
	 * @param properties a Properties object
	 * @return The created instance as an Object
	 */
	protected abstract Object makeInstance(Properties properties);
	
	/**
	 * Interface for creating MakerHooks that are useful for overriding instance creation logic
	 *  for example if your main Maker factory is in a library and you need to override it in your application
	 * @author Ramiro.Serrato
	 *
	 */
	public static interface MakerHook { 
		public <T> T makeInstance();
	}
}

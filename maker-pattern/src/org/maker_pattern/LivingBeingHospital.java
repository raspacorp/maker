/**
 * Mar 14, 2014 File created by Ramiro.Serrato
 */
package org.maker_pattern;

import static org.maker_pattern.LivingBeingMaker.*;

import org.maker_pattern.animals.Animal;
import org.maker_pattern.animals.AnimalFamily;
import org.maker_pattern.animals.Cat;
import org.maker_pattern.animals.Dog;
import org.maker_pattern.animals.DogFamily;
import org.maker_pattern.plants.Plant;

/**
 * @author Ramiro.Serrato
 *
 */
public class LivingBeingHospital {
	
	/*** Singletons, look at the different ways for injecting dependencies ***/

	private Dog sparky = LivingBeingMaker.getDog(SPARKY_DOG); // By concrete class
	private Dog pinky = LivingBeingMaker.getDog(PINKY_DOG); // By concrete class
	
	private DogFamily sparkyFamily = LivingBeingMaker.getDogFamily(SPARKY_FAMILY);	// this is a wired bean 
	
	/*** Prototypes, (new instance every time) objects ***/
	
	private Animal labrador1 = LivingBeingMaker.getDog(LABRADOR); // by abstract class 
	private Animal labrador2 = LivingBeingMaker.getAnimal(LABRADOR); // by abstract class 
	private Animal siamese = LivingBeingMaker.getAnimal(SIAMESE);  // By abstract class
	private Plant myDaisy = LivingBeingMaker.getPlant(DAISY); // Using interface injection
	private Plant myCeiba = LivingBeingMaker.getPlant(CEIBA); // Using interface injection
	
	private void cureAnimal(Animal animal) {
		System.out.print("Curing animal...");
		animal.makeNoise();  // it hurts
	}
	
	private void curePlant(Plant plant) {
		System.out.print("Curing plant...");
		plant.grow(); // now it grows
	}
	
	private void attendLabradorDogBirth(DogFamily dogFamily) {
		System.out.print("Attending dog birth...");
		Dog newDog = LivingBeingMaker.getDog(LABRADOR);
		dogFamily.newMember(newDog);
	}
	
	private void attendSiameseCatBirth(AnimalFamily<Cat> catFamily) {
		System.out.print("Attending cat birth...");		
		Cat newCat = LivingBeingMaker.getCat(SIAMESE);
		catFamily.newMember(newCat);
	}
	
	private void cureAnimals() {
		cureAnimal(siamese);
		cureAnimal(labrador1);
		cureAnimal(labrador2);
	}
	
	private void curePlants() {
		curePlant(myDaisy);
		curePlant(myCeiba);
	}
	
	public void doHospitalDay() {
		cureAnimals();
		curePlants();	
		attendLabradorDogBirth(sparkyFamily);
	}
	
	public static void main(String[] args) {
		LivingBeingHospital generalHospital = new LivingBeingHospital();
		generalHospital.doHospitalDay();
	}
}

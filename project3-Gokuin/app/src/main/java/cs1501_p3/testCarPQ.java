package cs1501_p3;
import java.util.NoSuchElementException;

//test file written to test code in CarsPQ
public class testCarPQ 
{
    public static void main(String[]args)
    {
        String testFilePath = "C:/Users/tatty/1501Github/project3-Gokuin/app/src/test/resources/cars.txt";
        CarsPQ cpq = new CarsPQ(testFilePath);
        Car c = cpq.getLowPrice("Ford", "Escort");
        cpq.displayValues();
        System.out.println("Lowest price in the price heap: " + cpq.getLowPrice().getPrice() + "$");
        System.out.println("Lowest mileage in the mileage heap: " + cpq.getLowMileage().getMileage() + " miles");

        //checking get should all print out true
        System.out.println((cpq.get("PUAF85WU5R6L6H1P9").getColor().equals("Red")));
        System.out.println((cpq.get("X1U2PEJSC361L10MZ").getColor().equals("Green")));
        System.out.println((cpq.get("16Z2DPEHSUK5KCMEH").getColor().equals("Yellow")));
        //checking update color should print true
        cpq.updateColor("1Y5NWYGLY5F4PX4HH", "White");
        System.out.println((cpq.get("1Y5NWYGLY5F4PX4HH").getColor().equals("White")));
        //update price and mileage should be the new lowest price and mileage in the heaps

        cpq.updatePrice("1Y5NWYGLY5F4PX4HH", 900);
        cpq.updateMileage("X1U2PEJSC361L10MZ", 900);
        cpq.displayValues();
        System.out.println("Lowest price in the price heap: " + cpq.getLowPrice().getPrice() + "$");
        System.out.println("Lowest mileage in the mileage heap: " + cpq.getLowMileage().getMileage() + " miles");
        //should print out the vin 1Y5NWYGLY5F4PX4HH
        System.out.println("Lowest Price ford Escort: " + cpq.getLowPrice("Ford","Escort").getVIN()+ " " + cpq.getLowPrice("Ford","Escort").getPrice() + " dollars");
        //should print out the vin  X1U2PEJSC361L10MZ
        System.out.println("Lowest Mileage ford fiesta: " + cpq.getLowMileage("Ford","Fiesta").getVIN() + " " +cpq.getLowMileage("Ford","Fiesta").getMileage() + " miles");
        cpq.remove("678PL45NTNWRED0RJ");
        try
        {
            cpq.get("678PL45NTNWRED0RJ");
        }
        catch(NoSuchElementException e)
        {
            System.out.println(".....Passed");
        }
        //not working correctly after the remove issue with the remove
        System.out.println("Lowest price in the price heap: " + cpq.getLowPrice().getPrice() + "$");
        System.out.println("Lowest mileage in the mileage heap: " + cpq.getLowMileage().getMileage() + " miles");
        cpq.displayValues();
        cpq.remove("Y9BXE6H7957YNKD2C");//throws NullPointerException
        cpq.remove("RAMM7ZJBSFZ0HRTTN");
        cpq.remove("16Z2DPEHSUK5KCMEH");
        cpq.remove("SM0G8H2WXK466CRCA");
        System.out.println("\nDisplaying values after 4 more removes.....\n");
        cpq.displayValues();

        /*-----------------------------------Ideas for extra tests-----------------------------------:
        1. what happens if i update 900 to be 5000
            passed
        2. non existent vin deletion
            passed
        3. updating highest value to be the lowest
            passed
        4. get on non existent vin
            passed
        5. trying to push a duplicate vin in
            passed
        6. trying to push a non valid vin in
            not tested by prof
        7. inserting a new low
            passed
        8.removing all the cars from the pq
            passed had to add a bit mrore code
        */

        cpq.updatePrice("1Y5NWYGLY5F4PX4HH", 12900);
        System.out.println("Lowest price in the price heap: " + cpq.getLowPrice().getPrice() + "$");
        cpq.displayValues();
        //should print out the message "The requested VIN is not in the structure"
        System.out.println("Non heap vin deletion:");
        cpq.remove("5");
        cpq.updatePrice("GNX5TS04SM5V5EXP8", 5);
        cpq.displayValues();//5 should be at the top of the price heap print out
        Car c1 = new Car("5", "Ford", "Fiesta", 20, 200000, "White");
        cpq.add(c1);
        cpq.displayValues();
        try
        {
            cpq.get("2");
        }
        catch(Exception e)
        {
            System.out.println("Get on nonexistent vin.....passed");
        }
        try
        {
            cpq.add(c1);
        }
        catch(Exception e)
        {
            System.out.println("Add on duplicate car.....passed");
        }
        CarsPQ cpq2 = new CarsPQ();
        //checking to make sure removing the last item doesnt throw an error
        cpq2.add(c1);
        cpq2.remove("5");
    }    
}
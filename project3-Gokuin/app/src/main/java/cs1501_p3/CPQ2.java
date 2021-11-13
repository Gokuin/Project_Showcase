/*CarsPQ class written by Taittinger Gabelhart for 1501 p3
  this class represents a indexable priority queue that will store cars based on two values
  lowest price, lowest mileage
  NOTE: This class uses a IndexMinPq and a TrieST that were written by Robert sedegewick
  and adapted by me to help solve both the run time requirements of the assignment
  and to make sure the correct items are returned.
 */
//packages
package cs1501_p3;

import java.io.BufferedReader;
import java.io.FileInputStream;
//imports
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;


public class CPQ2 implements CarsPQ_Inter
{
    //------Private variables for the CarsPQ class below-----

    //holds the car objects used in price 
    private Car[] carCheap;
    //holds the car objects used in mileage
    private Car[] carMheap;
    //holds a VIN index pair
    private TrieST<Integer> carPTrieST;
    //holds a VIN index pair
    private TrieST<Integer> carMTrieST;
    //IMPQ[] to hold each PQ with the lowest price for each Make + Model 
    //private IndexMinPQ<Integer>[] makeModelPA;
    private MMPQ[] makeModelCPA;
    private MMMPQ[] makeModelCMA;  
    //IMPQ[] to hold each PQ with the lowest mileage for each Make + Model 
    //takes a make+model as a key and holds the index in the IMPQ array for that make + model
    private TrieST<Integer> MMPTrieST;
    //takes a make+model as a key and holds its corresponding pq for mileage
    private TrieST<Integer> MMMTrieST;

    //takes a make+model as a key and holds the car as the value for each lowest make and model price
    private TrieST<Car> MMPCTrieST;
    //takes a make + model as akey and holds the car as the value for each lowest make and model mileage
    private TrieST<Car> MMMCTrieST;

    //************Variables for arrays below************/
    //one based indexing holds the items index in carCHeap
    private int[] pqC = new int[10];
    //one based indexing holds the items index in carMHeap
    private int[] pqM = new int[10];
    //0 based holds the items index in pqC
    private int[] qpC = new int[10];
    //0 based holds the items index in pqM
    private int[] qpM = new int[10];
    //counter used to track current index for pq
    //ie pqc[1] index stored at qpc[0]
    private int pqc;
    //counter used to track current index for qp
    private int qpc;
    //counter used to keep track of the current index of make and model inserts for the IMPQ[]'s
    private int IMPQC;

    /*Base Constructor that intializes an empty carPQ object */
    public CPQ2()
    {
        //intial keys array for the cars
        carCheap = new Car[10];
        carMheap = new Car[10];
        makeModelCPA = new MMPQ[10];
        makeModelCMA = new MMMPQ[10];
        carPTrieST = new TrieST<Integer>();
        carMTrieST = new TrieST<Integer>();
        MMPTrieST = new TrieST<Integer>();
        MMMTrieST = new TrieST<Integer>();
        MMPCTrieST = new TrieST<Car>();
        MMMCTrieST = new TrieST<Car>();
        pqc = 0;
        qpc = 0;
        IMPQC = 0;

        //intialize array values as -1 to show nothings there
        for(int z = 0; z < pqC.length; z++)
        {
            pqC[z] = 0;
            pqM[z] = 0;
            qpC[z] = -1;
            qpM[z] = -1;
        }
    }

    /*Overloaded constructor that takes in a file path of a car data to be added to the heap */
    public CPQ2(String dataFilePath)
    {
        //collectin objects
        carPTrieST = new TrieST<Integer>();
        carMTrieST = new TrieST<Integer>();
        MMPTrieST = new TrieST<Integer>();
        MMMTrieST = new TrieST<Integer>();

        MMPCTrieST = new TrieST<Car>();
        MMMCTrieST = new TrieST<Car>();
        carCheap = new Car[10];
        carMheap = new Car[10];
        makeModelCPA = new MMPQ[10];
        makeModelCMA = new MMMPQ[10];
        pqc = 0;
        qpc = 0;
        IMPQC = 0;

        String VIN = "";//1
        String make = "";//2
        String model = "";//3
        String price = "";//4
        String mileage = "";//5
        String color = "";//6
        String current = "";
        int u = 0;

        try 
        {
            // Open the file
            FileInputStream fstream = new FileInputStream(dataFilePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            //intialize array values as -1 to show nothings there
            for(int z = 0; z < pqC.length; z++)
            {
                pqC[z] = 0;
                pqM[z] = 0;
                qpC[z] = -1;
                qpM[z] = -1;
            }
            //while there is lines in the file
            while ((strLine = br.readLine()) != null)
            {
                for(char letter : strLine.toCharArray())
                {
                    if(letter != ':')
                    {
                        current += letter;
                    }
                    else
                    {
                        u++;
                        switch(u)
                        {
                            case 1:
                            VIN = current;
                            current = "";
                            break;

                            case 2:
                            make = current;
                            current = "";
                            break;

                            case 3:
                            model = current;
                            current = "";
                            break;

                            case 4:
                            price = current;
                            current = "";
                            break;

                            case 5:
                            mileage = current;
                            current = "";
                            break;
                        }
                    }
                }
                color = current;
                current = "";
                u = 0;
                if(!(VIN.equals("# VIN")))
                {
                    add(new Car(VIN, make, model, Integer.valueOf(price), Integer.valueOf(mileage), color));
                }
                else
                {
                    //make sure we dont try and add the example line of columns titles to the structures
                    continue;
                }
            }
            //close the stream after were done using it
            fstream.close();
        } 
        catch (IOException e) 
        {
            System.out.println(e.getLocalizedMessage());
        }
    }

    @Override
    /**
	 * Add a new Car to the data structure
	 * Should throw an `IllegalStateException` if there is already car with the
	 * same VIN in the datastructure.
	 *
	 * @param 	c Car to be added to the data structure
	 */
    public void add(Car c) throws IllegalStateException 
    {
        //check for null data being passed in
        if(c == null)
        {
            throw new NullPointerException("The passed in car c is null.");
        }
        pqc++;
        //check for the empty case 
        if(carPTrieST.isEmpty() && carMTrieST.isEmpty())
        {
            //updoot pq counter

            //add object c to the heaps
            carCheap[qpc] = c;
            carMheap[qpc] = c;
            //put the key value pair into the car trie
            carPTrieST.put(c.getVIN(),qpc);
            carMTrieST.put(c.getVIN(),qpc);
            MMPQ cpPQ = new MMPQ();
            MMMPQ cmPQ = new MMMPQ();
            cpPQ.add(c);
            cmPQ.add(c);
            //now put those make and models into the arrays to hold them
            makeModelCPA[IMPQC] = cpPQ;
            makeModelCMA[IMPQC] = cmPQ;
            /*use the MAKE+MODEL as the key and the index
              of where we inserted each pq into the array to hold them as the value*/
            MMPTrieST.put((c.getMake() + c.getModel()), IMPQC);
            MMMTrieST.put((c.getMake() + c.getModel()), IMPQC);
            //update the index counter for the pq array collection
            IMPQC++;
            //update the index arrays
            pqC[pqc] = qpc;
            pqM[pqc] = qpc;
            qpC[qpc] = pqc;
            qpM[qpc] = pqc;
            //increment the counter for qp
            qpc++;
            //done adding the base now we can just return
            return;
        }
        //else carCHeap and carMHeap are not empty
        try
        {
            get(c.getVIN());
            //this throws an exception if the vin is not in the array
            //so if it gets passed the call to get the VIN already exists in the structure
            throw new IllegalStateException("There is already a car with the listed VIN in the datastructure.");
        }
        catch(NoSuchElementException nsee)
        {
            //if get throws a NSEE that means the vin is not present in the heap structure
            //add the item in both the heaps and pq and qp
            if(qpc < carCheap.length && pqc < pqC.length)
            {
                carCheap[qpc] = c;
                carMheap[qpc] = c;
                carPTrieST.put(c.getVIN(), qpc);
                carMTrieST.put(c.getVIN(), qpc);

                /*now we need to check and see if we already have an
                  existing PQ for the new car to add*/
                if(MMPTrieST.contains(c.getMake() + c.getModel()))
                {
                    /*that means we already have a PQ for this make + models info
                      so we just get that index from the trie and insert our new int into 
                      its associated PQ*/

                    //grab the index of where that PQ is in the array collections
                    int MMPAIndex = MMPTrieST.get(c.getMake() + c.getModel());
                    int MMMAIndex = MMMTrieST.get(c.getMake() + c.getModel());
                    MMPQ cpPQ = makeModelCPA[MMPAIndex];
                    MMMPQ cmPQ = makeModelCMA[MMMAIndex];
                    cpPQ.add(c);
                    cmPQ.add(c);
                }
                else
                {
                    MMPQ cpPQ = new MMPQ();
                    MMMPQ cmPQ = new MMMPQ();
                    cpPQ.add(c);
                    cmPQ.add(c);
                    //now put those make and models into the arrays to hold them
                    makeModelCPA[IMPQC] = cpPQ;
                    makeModelCMA[IMPQC] = cmPQ;
                    /*use the MAKE+MODEL as the key and the index
                    of where we inserted each pq into the array to hold them as the value*/
                    MMPTrieST.put((c.getMake() + c.getModel()), IMPQC);
                    MMMTrieST.put((c.getMake() + c.getModel()), IMPQC);
                    IMPQC++;
                }
            }
            else
            {
                resizeArrays();
                carCheap[qpc] = c;
                carMheap[qpc] = c;
                carPTrieST.put(c.getVIN(), qpc);
                carMTrieST.put(c.getVIN(), qpc);

                /*now we need to check and see if we already have an
                  existing PQ for the new car to add*/
                if(MMPTrieST.contains(c.getMake() + c.getModel()))
                {
                    /*that means we already have a PQ for this make + models info
                      so we just get that index from the trie and insert our new int into 
                      its associated PQ*/
  
                    //grab the index of where that PQ is in the array collections
                    int MMPAIndex = MMPTrieST.get(c.getMake() + c.getModel());
                    int MMMAIndex = MMMTrieST.get(c.getMake() + c.getModel());
                    MMPQ cpPQ = makeModelCPA[MMPAIndex];
                    MMMPQ cmPQ = makeModelCMA[MMMAIndex];
                    cpPQ.add(c);
                    cmPQ.add(c);
                }
                else
                {
                    MMPQ cpPQ = new MMPQ();
                    MMMPQ cmPQ = new MMMPQ();
                    cpPQ.add(c);
                    cmPQ.add(c);
                    makeModelCPA[IMPQC] = cpPQ;
                    makeModelCMA[IMPQC] = cmPQ;
                    /*use the MAKE+MODEL as the key and the index
                    of where we inserted each pq into the array to hold them as the value*/
                    MMPTrieST.put((c.getMake() + c.getModel()), IMPQC);
                    MMMTrieST.put((c.getMake() + c.getModel()), IMPQC);
                    IMPQC++;
                }
            }
            pqC[pqc] = qpc;
            pqM[pqc] = qpc;
            qpC[qpc] = pqc;
            qpM[qpc] = pqc;
            //increment the counters
            qpc++;
            //done adding the base now we can just return
            //maintain order in the price heap
            swimC(pqc);
            //maintain order in the mileage heap
            swimM(pqc);
        }
    }

    @Override
    /**
	 * Retrieve a new Car from the data structure
	 * Should throw a `NoSuchElementException` if there is no car with the 
	 * specified VIN in the datastructure.
	 *
	 * @param 	vin VIN number of the car to be updated
	 */
    public Car get(String vin) throws NoSuchElementException 
    {
        if(carPTrieST.contains(vin))
        {
            int indexT = (carPTrieST.get(vin));
            int indexQp = qpC[indexT];
            int indexHeap = pqC[indexQp];
            return carCheap[indexHeap];
        }
        throw new NoSuchElementException("The requested VIN is not in the structure");
    }

    @Override
    /**
	 * Update the price attribute of a given car
	 * Should throw a `NoSuchElementException` if there is no car with the 
	 * specified VIN in the datastructure.
	 *
	 * @param 	vin VIN number of the car to be updated
	 * @param	newPrice The updated price value
	 */
    public void updatePrice(String vin, int newPrice) throws NoSuchElementException 
    {
        int index = -1;
        try
        {
            Car cToBeUpdated = get(vin);
            int oldPrice = cToBeUpdated.getPrice();
            //used in the make model tries to tell us which index in the mmpq[] holds that pq
            String mm = cToBeUpdated.getMake() + cToBeUpdated.getModel();
            MMPQ mmpQ = makeModelCPA[MMPTrieST.get(mm)];
            mmpQ.updatePrice(vin, newPrice);
            MMMPQ mmmpQ = makeModelCMA[MMMTrieST.get(mm)];
            mmmpQ.updatePrice(vin, newPrice);
            //then we update the price here
            cToBeUpdated.setPrice(newPrice);
            index = getPQCI(vin);
            if(oldPrice > cToBeUpdated.getPrice())
            {
                swimC(index);
            }
            else if(oldPrice < cToBeUpdated.getPrice())
            {
                sinkC(index);
            }
            return;
        }
        catch(NoSuchElementException e)
        {
            throw new NoSuchElementException("The requested car for price update is not in the heap.");
        }
    }

    @Override
    /**
	 * Update the mileage attribute of a given car
	 * Should throw a `NoSuchElementException` if there is not car with the 
	 * specified VIN in the datastructure.
	 *
	 * @param 	vin VIN number of the car to be updated
	 * @param	newMileage The updated mileage value
	 */
    public void updateMileage(String vin, int newMileage) throws NoSuchElementException 
    {
        int index = -1;
        try
        {
            int oldMileage;
            Car cToBeUpdated = get(vin);
            oldMileage = cToBeUpdated.getMileage();
            //used in the make model tries to tell us which index in the mmpq[] holds that pq
            String mm = cToBeUpdated.getMake() + cToBeUpdated.getModel();
            //update the mileage of that item in its pq
            MMPQ mmpQ = makeModelCPA[MMPTrieST.get(mm)];
            mmpQ.updateMileage(vin, newMileage);
            MMMPQ mmmpQ = makeModelCMA[MMMTrieST.get(mm)];
            mmmpQ.updateMileage(vin, newMileage);
            //then we update the price here
            cToBeUpdated.setMileage(newMileage);
            index = getPQMI(vin);
            //if the new mileage is less we swim up with the changed index
            if(oldMileage > cToBeUpdated.getMileage())
            {
                swimM(index);
            }
            //if the new mileage is greater we sink with the changed index
            else if(oldMileage < cToBeUpdated.getMileage())
            {
                sinkM(index);
            }
            return;
        }
        catch(NoSuchElementException e)
        {
            throw new NoSuchElementException("The requested car for mileage update is not in the heap.");
        }
    }

    @Override
    /**
	 * Update the color attribute of a given car
	 * Should throw a `NoSuchElementException` if there is not car with the 
	 * specified VIN in the datastructure.
	 *
	 * @param 	vin VIN number of the car to be updated
	 * @param	newColor The updated color value
	 */
    public void updateColor(String vin, String newColor) throws NoSuchElementException 
    {
        try
        {
            String mm = "";
            Car cToBeUpdated = get(vin);
            mm = cToBeUpdated.getMake() + cToBeUpdated.getModel();
            int aI = MMPTrieST.get(mm);
            MMPQ pq = makeModelCPA[aI];
            Car pqCar = pq.get(vin);
            pqCar.setColor(newColor);
            int a2 = MMMTrieST.get(mm);
            MMMPQ m  = makeModelCMA[a2];
            Car pqMCAr = m.get(vin);
            pqMCAr.setColor(newColor);
            cToBeUpdated.setColor(newColor);
            return;
        }
        catch(NoSuchElementException e)
        {
            throw new NoSuchElementException("The requested car for color update is not in the heap.");
        }
    }

    @Override
    /**
	 * Remove a car from the data structure
	 * Should throw a `NoSuchElementException` if there is not car with the 
	 * specified VIN in the datastructure.
	 *
	 * @param 	vin VIN number of the car to be removed
	 */
    public void remove(String vin) throws NoSuchElementException 
    {
        try
        {
            if(carPTrieST.size() == 1)
            {
                Car c = get(vin);
                carCheap[0] = null;
                carMheap[0] = null;
                pqC[1] = 0;
                pqM[1] = 0;
                qpC[0] = -1;
                qpM[0] = -1;
                carPTrieST.delete(c.getVIN());
                carMTrieST.delete(c.getVIN());
                //decrement the size
                pqc--;
                int mmPindex = MMPTrieST.get(c.getMake() + c.getModel());
                int mmmindex = MMMTrieST.get(c.getMake() + c.getModel());
                makeModelCPA[mmPindex].remove(c.getVIN());
                makeModelCMA[mmmindex].remove(c.getVIN());
                return;
            }
            Car c = get(vin);
            //grab the index in the pq of the car
            int indexOfCar = carPTrieST.get(c.getVIN());
            //grab that car and store in temp
            Car temp = carCheap[indexOfCar];
            //set the index of car to be the car at the end
            carCheap[indexOfCar] = carCheap[pqc-1];
            //put the car to be deleted at the end
            carCheap[pqc-1] = temp;
            //now delete that car from the heap
            carCheap[pqc-1] = null;
            //do the same for the mileage heap
            //set the end car to be the old index of removal
            carMheap[indexOfCar] = carMheap[pqc-1];
            //set the end to be the car to remove
            carMheap[pqc-1] = temp;
            //now delete that end from the heap
            carMheap[pqc-1] = null;
            //remove reference to the end from both of the pqs
            int z = qpC[pqc-1];
            int x = qpM[pqc-1];
            pqC[z] = 0;
            pqM[x] = 0;
            qpC[pqc-1] = -1;
            qpM[pqc-1] = -1;
            //decrement the size
            pqc--;
            //update the end cars index in the trie to be its new postion
            if(carCheap[indexOfCar] != null)
            {
                carPTrieST.put(carCheap[indexOfCar].getVIN(), indexOfCar);
            }
            if(carMheap[indexOfCar] != null) 
            {
                carMTrieST.put(carMheap[indexOfCar].getVIN(), indexOfCar);
            }
            swimC(indexOfCar);
            sinkC(indexOfCar);
            swimM(indexOfCar);
            sinkM(indexOfCar);
            //remove the deleted car from the trie ST
            carPTrieST.delete(vin);
            carMTrieST.delete(vin);
            int mmPindex = MMPTrieST.get(c.getMake() + c.getModel());
            int mmmindex = MMMTrieST.get(c.getMake() + c.getModel());
            makeModelCPA[mmPindex].remove(c.getVIN());
            makeModelCMA[mmmindex].remove(c.getVIN());
        }
        catch(NoSuchElementException e)
        {
            System.out.println(e.getLocalizedMessage());
        }
    }

    @Override
    /**
	 * Get the lowest priced car (across all makes and models)
	 * Should return `null` if the data structure is empty
	 *
	 * @return	Car object representing the lowest priced car
	 */
    public Car getLowPrice() 
    {
        if(carCheap == null || carCheap.length == 0)
        {
            return null;
        }
        /*
          qpC has the lowest cost item's index stored at index 0 
          which returns the index of that item in the pqc array
          pqC has the index of the items place in the carC heap
          which returns the index of that item in the car C heap
          carCHeap holds all the added car objects
          which returns the acutal car object from the heap*/
        return carCheap[pqC[1]];
    }

    @Override
    /**
	 * Get the lowest priced car of a given make and model
	 * Should return `null` if the data structure is empty
	 *
	 * @param	make The specified make
	 * @param	model The specified model
	 * 
	 * @return	Car object representing the lowest priced car
	 */
    public Car getLowPrice(String make, String model) 
    {
        if(!(MMPTrieST.contains(make + model)))
        {
            return null;
        }
        else
        {
            //holds the index of the pq that holds the
            //lowest price for that make + model
            int index = MMPTrieST.get(make + model);
            return makeModelCPA[index].getLowPrice();
        }
    }

    @Override
    /**
	 * Get the car with the lowest mileage (across all makes and models)
	 * Should return `null` if the data structure is empty
	 *
	 * @return	Car object representing the lowest mileage car
	 */
    public Car getLowMileage() 
    {
        if(carMheap == null || carMheap.length == 0)
        {
            return null;
        }
        return carMheap[(pqM[1])];
    }

    @Override
    /**
	 * Get the car with the lowest mileage of a given make and model
	 * Should return `null` if the data structure is empty
	 *
	 * @param	make The specified make
	 * @param	model The specified model
	 *
	 * @return	Car object representing the lowest mileage car
	 */
    public Car getLowMileage(String make, String model) 
    {
        if(!(MMPTrieST.contains(make + model)))
        {
            return null;
        }
        else
        {
            int index = MMMTrieST.get(make + model);
            return makeModelCMA[index].getLowMileage();
        }
    }

/*-------------------------------------HELPER FUNCITONS FOR THIS CLASS BELOW--------------------------------------- */
    
//helper method prints out all the values in the order theyre stored in the pqC AND pqM arrays
    public void displayValues()
    {
        System.out.println("Display order of the lowest Price heap:");
        for(int i = 1; i < pqC.length; i++)
        {
            if(i > 14)
            {
                break;
            }
            if((pqC[i]) != -1 && carCheap[(pqC[i])] != null)
                System.out.println("Index " + i + " in  pqc holds index: " + pqC[i] + " for carCHeap; Price: " + carCheap[(pqC[i])].getPrice());
        }
        System.out.println("\nDisplay order of the lowest Mileage heap:");
        for(int i = 1; i < pqM.length; i++)
        {
            if(i > 14)
            {
                break;
            }
            if(pqM[i] != -1 && carMheap[(pqM[i])] != null)
                System.out.println("Index " + i + " in  pqm holds index: " + pqM[i] + " for carMHeap; Mileage: " + carMheap[(pqM[i])].getMileage());
        }
    }

//returns the index of the vin in the qpC array
    private int getPQCI(String vin)
    {
        int indexT = (carPTrieST.get(vin));
        int indexQp = qpC[indexT];
        return indexQp;
    }

//returns the index of the vin in the qpM array
    private int getPQMI(String vin)
    {
        int indexT = (carMTrieST.get(vin));
        int indexQp = qpM[indexT];
        return indexQp;
    }

/*helper method 
only called when the sizes of pq and qp need to be made larger or the heaps need resized*/
    private  void resizeArrays()
    {
        //need to resize all four arrays two for carCheap and two for carM heap
        int[] newPqC = new int[(pqC.length) * 2];
        int[] newPqM = new int[(pqM.length) * 2];
        int[] newQpC = new int[(qpC.length) * 2];
        int[] newQpM = new int[(qpM.length) * 2];
        Car[] newCheap = new Car[(carCheap.length * 2)];
        Car[] newMheap = new Car[(carMheap.length * 2)];
        MMPQ[] newmakeModelCPA = new MMPQ[(makeModelCPA.length * 2)];
        MMMPQ[] newmakeModelCMA = new MMMPQ[(makeModelCMA.length * 2)];
        //now just copy the old contents into the new arrays
        //!!!!!!!!remember the pq arrays start at index 1!!!!!!!!
        //copying all the items from pqC and pqM into the larger arrays
        //any value past the old arrays size gets intialized to -1
        for (int i = 0; i < newPqC.length; i++ ) 
        {
            if(i < pqC.length)
            {
                newPqC[i] = pqC[i];
                newPqM[i] = pqM[i];
                newQpC[i] = qpC[i];
                newQpM[i] = qpM[i];
                newCheap[i] = carCheap[i];
                newMheap[i] = carMheap[i];
                newmakeModelCPA[i] = makeModelCPA[i];
                newmakeModelCMA[i] = makeModelCMA[i];
            }
            else
            {
                newPqC[i] = 0;
                newPqM[i] = 0;
                newQpC[i] = -1;
                newQpM[i] = -1;
            }
        }
        //lastly just assign the old arrays to the newly resized ones with all their old items still in it
        pqC = newPqC;
        pqM = newPqM;
        qpC = newQpC;
        qpM = newQpM;
        carCheap = newCheap;
        carMheap = newMheap;
        makeModelCPA = newmakeModelCPA;
        makeModelCMA = newmakeModelCMA;
    }  
    
/*helper method for comparing prices
returns true if i > j, returns false if i < j*/
    private Boolean greaterC(int i , int j)
    {
        Car iC = carCheap[(pqC[i])];
        Car jC = carCheap[(pqC[j])];
        return (iC.getPrice() > jC.getPrice());
    }

/*helper method for comparing mileages
returns true if i > j, returns false if i < j*/
    private Boolean greaterM(int i , int j)
    {
        return (carMheap[(pqM[i])].getMileage() > carMheap[(pqM[j])].getMileage());
    }

    /*helper method for price heaps
      exchanges the two items*/
    private void exchangeC(int i , int j)
    {
        int swapindex = pqC[i];
        pqC[i] = pqC[j];
        pqC[j] = swapindex;
        qpC[pqC[i]] = i;
        qpC[pqC[j]] = j;
    }

    /*helper method for mileage heap
      exchanges the two items*/
    private void exchangeM(int i , int j)
    {
        int swapindex = pqM[i];
        pqM[i] = pqM[j];
        pqM[j] = swapindex;
        qpM[pqM[i]] = i;
        qpM[pqM[j]] = j;
    }

    /*helper method for min price heap
      for when we inserted at the bottom of the heap and the item
      needs to be moved up the heap*/
    private void swimC(int k)
    {
        while(k > 1 && greaterC(k/2,k))
        {
            exchangeC(k/2, k);
            k = k/2;
        }
    }

    private void sinkM(int k)
    {
        while (2*k <= qpc) {
            int j = 2*k;
            if (j < qpc && greaterM(j, j+1)) j++;
            if (!greaterM(k, j)) break;
            exchangeM(k, j);
            k = j;
        }
    }

    private void sinkC(int k)
    {
        while (2*k <= qpc) 
        {
            int j = 2*k;
            if (j < qpc && greaterC(j, j+1)) j++;
            if (!greaterC(k, j)) break;
            exchangeC(k, j);
            k = j;
        }
    }
    /*helper method for min mileage heap
      for when we inserted at the bottom of the heap and the item
      needs to be moved up the heap*/
    private void swimM(int k)
    {
        while(k > 1 && greaterM(k/2,k))
        {
            exchangeM(k, k/2);
            k = k/2;
        }
    }   
}
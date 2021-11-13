package cs1501_p3;

import java.util.NoSuchElementException;

public class MMPQ 
{
    //holds the car objects used in price 
    private Car[] carCheap;
    //holds the car objects used in mileage
    private Car[] carMheap;
    //holds a VIN index pair
    private TrieST<Integer> carPTrieST;
    //holds a VIN index pair
    private TrieST<Integer> carMTrieST;

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

    /*Base Constructor that intializes an empty mmPQ object */
    public MMPQ()
    {
        //intial keys array for the cars
        carCheap = new Car[10];
        carMheap = new Car[10];
        carPTrieST = new TrieST<Integer>();
        carMTrieST = new TrieST<Integer>();
        pqc = 0;
        qpc = 0;

        //intialize array values as -1 to show nothings there
        for(int z = 0; z < pqC.length; z++)
        {
            pqC[z] = 0;
            pqM[z] = 0;
            qpC[z] = -1;
            qpM[z] = -1;
        }
    }

    public void add(Car c) throws IllegalStateException 
    {
        //check for null data being passed in
        if(c == null)
        {
            throw new NullPointerException("The passed in car c is null.");
        }
        //check for the empty case 
        if(carPTrieST.isEmpty() && carMTrieST.isEmpty())
        {
            //updoot pq counter
            pqc++;
            //add object c to the heaps
            carCheap[qpc] = c;
            carMheap[qpc] = c;
            //put the key value pair into the car trie
            carPTrieST.put(c.getVIN(),qpc);
            carMTrieST.put(c.getVIN(),qpc);
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
            pqc++;
            if(qpc < carCheap.length && pqc < pqC.length)
            {
                carCheap[qpc] = c;
                carMheap[qpc] = c;
                carPTrieST.put(c.getVIN(), qpc);
                carMTrieST.put(c.getVIN(), qpc);

            }
            else
            {
                resizeArrays();
                carCheap[qpc] = c;
                carMheap[qpc] = c;
                carPTrieST.put(c.getVIN(), qpc);
                carMTrieST.put(c.getVIN(), qpc);
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

    public void remove(String vin) throws NoSuchElementException 
    {
        try
        {
            //to remove from the heap we grab the index to be deleted and swap it with the end item
            //then delete the item now at the end
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
        }
        catch(NoSuchElementException e)
        {
            System.out.println(e.getLocalizedMessage());
        }
    }

    public void updatePrice(String vin, int newPrice) throws NoSuchElementException 
    {
        int t = -1;
        try
        {
            Car cToBeUpdated = get(vin);
            String mm = cToBeUpdated.getMake() + cToBeUpdated.getModel();
            //then we update the price here
            cToBeUpdated.setPrice(newPrice);
            int indexT = (carPTrieST.get(vin));
            int indexQp = qpC[indexT];
            t = indexQp;
            swimC(t);
            sinkC(t);
            return;
        }
        catch(NoSuchElementException e)
        {
            throw new NoSuchElementException("The requested car for price update is not in the heap.");
        }
    }

    public void updateMileage(String vin, int newMileage) throws NoSuchElementException 
    {
        int t = -1;
        try
        {
            /*if theres no car found with get
              it will throw an exception*/
            Car cToBeUpdated = get(vin);
            cToBeUpdated.setMileage(newMileage);
            int indexT = (carMTrieST.get(vin));
            int indexQp = qpM[indexT];
            t = indexQp;
            swimM(t);
            sinkM(t);
            return;
        }
        catch(NoSuchElementException e)
        {
            throw new NoSuchElementException("The requested car for mileage update is not in the heap.");
        }
    }

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

    public Car getLowMileage() 
    {
        if(carMheap == null || carMheap.length == 0)
        {
            return null;
        }
        return carMheap[(pqM[1])];
    }

    /*-----------HELPER METHODS BELOW----------------- */

    private void updateIndexesC(String v1, int Nindex)
    {
        carPTrieST.put(v1, Nindex);
    }

    private void updateIndexesM(String v1, int Nindex)
    {
        carMTrieST.put(v1, Nindex);
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

     /*helper method for comparing prices
      returns true if i > j, returns false if i < j*/
      private Boolean greaterC(int i , int j)
      {
        if((pqC[i]) != -1 && carCheap[(pqC[i])] == null)
        {
          return false;
        }
        else if((pqC[i]) == -1)
        {
          return false;
        }
        if((pqC[j] != -1 && carCheap[(pqC[j])] == null))
        {
          return true;
        }
        else if((pqC[j] == -1))
        {
          return true;
        }
        return (carCheap[(pqC[i])].getPrice() > carCheap[(pqC[j])].getPrice());
      }
  
      /*helper method for comparing mileages
        returns true if i > j, returns false if i < j*/
      private Boolean greaterM(int i , int j)
      {
          if((pqM[i]) != -1 && carMheap[(pqM[i])] == null)
          {
            return false;
          }
          else if((pqM[i]) == -1)
          {
            return false;
          }
          if((pqM[j] != -1 && carMheap[(pqM[j])] == null))
          {
            return true;
          }
          else if((pqM[j] == -1))
          {
            return true;
          }
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
}

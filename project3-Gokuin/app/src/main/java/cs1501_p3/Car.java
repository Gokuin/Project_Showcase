/*Car class written by Taittinger Gabelhart for 1501 p3
  this class will be used in conjunction with a indexable heap structure
 */

//packages
package cs1501_p3;

public class Car implements Car_Inter
{

    /*A unique VIN number (17 character string of numbers and capital letters
	  (but no I (i), O (o), or Q (q) to avoid confusion with numerals 1 and
	  0) */
    private int n = 2;
    private String VIN;
    /*The car's make (e.g., Ford, Toyota, Honda) */
    private String make;
    /*The car's model (e.g., Fiesta, Camry, Civic) */
    private String model;
    /*The price to purchase (in whole dollars) */
    private int priceToPurchase;
    /*The mileage of the car (in whole miles) */
    private int mileage;
    /*The color of the car */
    private String color;

    /*constructor for the Car object
      takes in a String VIN, String make, String model, int price, int mileage, String color*/
    public Car(String VIN, String make, String model, int price, int mileage, String color)
    {
        if(vinCheck(VIN))
        {
            this.VIN = VIN;
        }
        else
        {
            //just make it a default vin
            this.VIN = "123456789034abce" + ++n;
            System.out.println("Invalid vin passed in contains invalid letter.....");
        }
        this.make = make;   
        this.model = model;
        this.priceToPurchase = price;
        this. mileage = mileage;
        this.color = color;
    }

    //helper function to make sure no invalid chars are present the vin string
    private Boolean vinCheck(String vin)
    {
        int x = 0;
        while(x < vin.length())
        {
            if(vin.charAt(x) == 'I' || vin.charAt(x) == 'i' || vin.charAt(x) == 'O'||vin.charAt(x) == 'o'||vin.charAt(x) == 'Q'||vin.charAt(x) == 'q')
            {
                return false;
            }
            x++;
        }
        return true;
    }

    @Override
    /**
	 * Getter for the VIN attribute
	 *
	 * @return 	String The VIN
	 */
    public String getVIN() 
    {
        return VIN;
    }

    @Override
    /**
	 * Getter for the make attribute
	 *
	 * @return 	String The make
	 */
    public String getMake() 
    {
        return make;
    }

    @Override
    /**
	 * Getter for the model attribute
	 *
	 * @return 	String The model
	 */
    public String getModel() 
    {
        return model;
    }

    @Override
    /**
	 * Getter for the price attribute
	 *
	 * @return 	String The price
	 */
    public int getPrice() 
    {
        return priceToPurchase;
    }

    @Override
    /**
	 * Getter for the mileage attribute
	 *
	 * @return 	String The mileage
	 */
    public int getMileage() 
    {
        return mileage;
    }

    @Override
    /**
	 * Getter for the color attribute
	 *
	 * @return 	String The color
	 */
    public String getColor() 
    {
        return color;
    }

    @Override
    /**
	 * Setter for the price attribute
	 *
	 * @param 	newPrice The new Price
	 */
    public void setPrice(int newPrice) 
    {
        if(!(newPrice < 0))
            this.priceToPurchase = newPrice;
    }

    @Override
    /**
	 * Setter for the mileage attribute
	 *
	 * @param 	newMileage The new Mileage
	 */
    public void setMileage(int newMileage) 
    {
        if(!(newMileage < 0))
            this.mileage = newMileage;
        
    }

    @Override
    /**
	 * Setter for the color attribute
	 *
	 * @param 	newColor The new color
	 */
    public void setColor(String newColor) 
    {
        this.color = newColor;
        
    }
}
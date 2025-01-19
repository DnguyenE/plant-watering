package main;

import edu.princeton.cs.introcs.StdDraw;
import org.firmata4j.I2CDevice;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ssd1306.SSD1306;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

public class Main {

    //initializing constant variables
    private static final int A0 = 14;
    private static final int D2 = 2;
    private static final HashMap<Integer, Double> hashmap = new HashMap<>();
    private static int hashmapCounter = 1;
    private static boolean updatedEvent = false;  //boolean to check if the event changes (dry to moist)
    public static final int threshold = 680;


    //function for checking the moisture if it is dry or not
    public static boolean isDry(long moisture){
        return moisture > threshold;
    }

    public static double moistureToVoltage(long moisture){
        return ((double) moisture / 1023) * 5;
    }

    public static void graphHash (HashMap<Integer, Double> hashmap){

        StdDraw.clear();

        StdDraw.line(0, 3.25, hashmap.size(), 3.25);
        StdDraw.line(0,0,0,6);
        StdDraw.line(0,0,hashmap.size(), 0);

        StdDraw.setXscale(-3, hashmap.size());
        StdDraw.setYscale(-3, 6);

        StdDraw.text((double) hashmap.size() / 2, -2, "Time (s)");
        StdDraw.text(-2, 3, "V");

        for (int i = 1; i < hashmap.size();i++){
            StdDraw.text(i, hashmap.get(i), "*");
        }
    }

    //main function
    public static void main (String [] args) throws IOException {

        IODevice grove = new FirmataDevice("/dev/cu.usbserial-0001");  //init grove device
        DecimalFormat decimal = new DecimalFormat("00.00");

        try {  //start connection to grove
            grove.start();
            System.out.println("Grove Board is Connected!");
            grove.ensureInitializationIsDone();

        } catch (Exception ex){  //catch errors
            System.out.print("Nothing working");
        } finally {

            try {  //inside try loop
                //init constant connections to grove
                Pin myMoisture = grove.getPin(A0);
                myMoisture.setMode(Pin.Mode.ANALOG);
                Pin myPump = grove.getPin(D2);
                myPump.setMode(Pin.Mode.OUTPUT);

                //init connections to grove oled
                I2CDevice i2c = grove.getI2CDevice((byte)0x3C);
                SSD1306 oled = new SSD1306(i2c, SSD1306.Size.SSD1306_128_64);

                oled.init();

                while (hashmapCounter < 40) {
                    oled.getCanvas().drawString(
                            0,
                            0,
                            "Voltage: " + decimal.format(moistureToVoltage(myMoisture.getValue())));  //voltage on oled

                    hashmap.put(hashmapCounter, moistureToVoltage(myMoisture.getValue()));
                    hashmapCounter++;

                    if (isDry(myMoisture.getValue())){  //if the moisture returns dry

                        if (updatedEvent != isDry(myMoisture.getValue())){ //if eventChange changed values
                            updatedEvent = isDry(myMoisture.getValue());
                            myPump.setValue(1);  //change pump values
                            //oled will change on eventChange only
                            oled.clear();
                            System.out.println("Plant is dry! Needs Watering");
                            oled.getCanvas().drawString(0,30, "Plant is Watering");
                        } else {  //if eventChange did not change values
                            myPump.setValue(1);  //change pump values
                        }

                        System.out.println("Voltage: " + decimal.format(moistureToVoltage(myMoisture.getValue())));

                    } else {  //if the moisture is moist (not dry)

                        if (updatedEvent != isDry(myMoisture.getValue())){  //if eventChange changed values
                            updatedEvent = isDry(myMoisture.getValue());
                            myPump.setValue(0);  //change pump values
                            //oled will change on eventChange only
                            oled.clear();
                            System.out.println("Plant is moist! No watering required.");
                            oled.getCanvas().drawString(0,30, "No Watering Required");
                        } else {
                            myPump.setValue(0);  //change pump values
                        }

                        System.out.println("Voltage: " + decimal.format(moistureToVoltage(myMoisture.getValue())));

                    }

                    graphHash(hashmap);

                    oled.display();  //display oled information
                    Thread.sleep(1000);  //have a delay of a second before the loop runs and checks again
                }

            } catch (Exception ex){  //catch error for inside try loop
                System.out.println("Not Working");
            }
            System.out.println("Grove Stopped Connection");
            grove.stop();  //stop connection to grove

        }
    }
}

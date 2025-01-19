package Testing;

import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MainTesting {

    @Test
    public void isDryTesting(){

        for (int i = 0; i < 1023; i++){
            if (i < Main.threshold){
                Assertions.assertFalse(Main.isDry(i));
            } else if (i == Main.threshold){
                Assertions.assertFalse(Main.isDry(i));
            } else if (i > Main.threshold){
                Assertions.assertTrue(Main.isDry(i));
            } else {
                System.out.println("Error in Testing");
            }
        }
    }

    @Test
    public void moistureToVoltageTesting() {
        for (int i = 0; i < 1023; i++){
            double value = ((double)i / 1023) * 5;
            Assertions.assertEquals(value, Main.moistureToVoltage(i));
        }
    }



}

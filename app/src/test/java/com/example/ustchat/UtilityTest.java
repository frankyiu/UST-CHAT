package com.example.ustchat;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class UtilityTest {

    @Test
    public void generateIntegerWithLeadingZerosTest_isTrue() {
        String sampleStudentNameDigit = Utility.generateIntegerWithLeadingZeros(100000, 5);
        assertTrue(sampleStudentNameDigit.matches("[\\d]{5}"));
    }

}

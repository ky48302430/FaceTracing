package com.ylm.facetracing;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws FileNotFoundException {
        assertEquals(4, 2 + 2);
        File file = new File("D:\\Lance\\ndk\\lsn27_opencv_face\\资料\\img\\bg");
        File[] jpg = file.listFiles();
        new FileOutputStream(new File("D:\\Lance\\ndk\\lsn27_opencv_face\\资料\\img\\bg.txt"));
    }
}
package id.dreamfighter.android.thermalprinter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.dreamfighter.android.thermalprinter.utils.Epp200PrintBuilder;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class Epp200PrintBuilderUnitTest {
    @Test
    public void simple_isCorrect() throws Exception {
        Epp200PrintBuilder builder = Epp200PrintBuilder.Build()
                .center("center").newline()
                .right("right").newline()
                .left("left");
        assertEquals("             center             \n" +
                "                           right\n" +
                "left                            ", builder.format());
    }

    @Test
    public void overlap_isCorrect() throws Exception {
        Epp200PrintBuilder builder = Epp200PrintBuilder.Build()
                .center("I want to eat nasi goreng center in the dark").newline()
                .right("I want to eat nasi goreng center in the dark").newline()
                .left("I want to eat nasi goreng center in the dark");
        assertEquals("I want to eat nasi goreng center\n" +
                "           in the dark          \n" +
                "I want to eat nasi goreng center\n" +
                "                     in the dark\n" +
                "I want to eat nasi goreng center\n" +
                "in the dark                     ", builder.format());
    }

    @Test
    public void subcriber_isCorrect() throws Exception {
        Epp200PrintBuilder builder = Epp200PrintBuilder.Print(null)
                .append("1").append("2").append("3").newline();
        assertEquals("I want to eat nasi goreng center\n" +
                "           in the dark          \n" +
                "I want to eat nasi goreng center\n" +
                "                     in the dark\n" +
                "I want to eat nasi goreng center\n" +
                "in the dark                     ", builder.format());
    }

    @Test
    public void table_isCorrect() throws Exception {
        Map<String,String> row1 = new HashMap<>();
        row1.put("name","Gula receng");
        row1.put("qty","1");
        row1.put("price","Rp1000,00");

        Map<String,String> row2 = new HashMap<>();
        row2.put("name","Mie Telor Ayam");
        row2.put("qty","10");
        row2.put("price","Rp10.000,00");

        Map<String,String> row3 = new HashMap<>();
        row3.put("name","Tepung Aping");
        row3.put("qty","2");
        row3.put("price","Rp20.000,00");

        List<Map<String,String>> rows = new ArrayList<Map<String,String>>();
        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        List<Map<String,String>> cols = new ArrayList<>();
        Map<String,String> col1 = new HashMap<String,String>();
        col1.put("key","name");
        col1.put("name","Name");


        Map<String,String> col2 = new HashMap<String,String>();
        col2.put("key","qty");
        col2.put("name","Quantity");


        Map<String,String> col3 = new HashMap<String,String>();
        col3.put("key","price");
        col3.put("name","Price");
        col3.put("align","right");

        cols.add(col1);
        cols.add(col2);
        cols.add(col3);

        Epp200PrintBuilder builder = Epp200PrintBuilder.Build()
                .table(rows,cols);
        assertEquals("I want to eat nasi goreng center\n" +
                "           in the dark          \n" +
                "I want to eat nasi goreng center\n" +
                "                     in the dark\n" +
                "I want to eat nasi goreng center\n" +
                "in the dark                     ", builder.format());
    }
}
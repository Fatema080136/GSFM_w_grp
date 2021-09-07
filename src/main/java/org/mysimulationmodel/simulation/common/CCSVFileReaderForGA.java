package org.mysimulationmodel.simulation.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CCSVFileReaderForGA
{
    public static Map<String,ArrayList<String>> readDataFromCSV2()
    {

        Map<String,ArrayList<String>> m_realdata = new HashMap<>();
        Path pathToFile = Paths.get( "C:\\Users\\fatema\\Desktop\\modelbeforecombinigwithhaosmodel" +
                "\\hamburg_model_without_calibration\\Trasportation_letters_journal\\HBS\\real_data_tuc.csv" );

        try ( BufferedReader l_br = Files.newBufferedReader( pathToFile, StandardCharsets.US_ASCII ) )
        {
            String l_line = l_br.readLine();
            while ( l_line != null )
            {
                String[] l_attributes = l_line.split(",");
                //System.out.println( "gggg "+l_attributes[6]);

                ArrayList<String> l_temp = new ArrayList<>();
                l_temp.add(l_attributes[4]);
                l_temp.add(l_attributes[5]);

                //System.out.println( l_attributes[5] + " wh "+l_attributes[6]);
                m_realdata.put( new StringBuffer(new StringBuffer(l_attributes[1]).append(l_attributes[3])).append(l_attributes[12]).toString()
                        , l_temp );

                l_line = l_br.readLine();
            }
        }
        catch ( IOException ioe) { ioe.printStackTrace(); }
        return m_realdata;
    }

    public static Map<String,ArrayList<Integer>> input()
    {
        Map<String,ArrayList<Integer>> input = new HashMap<>();

        Path pathToFile = Paths.get( System.getProperty("user.dir").concat("/GAinput_indv1_new.csv" ));

        try ( BufferedReader l_br = Files.newBufferedReader( pathToFile, StandardCharsets.US_ASCII ) )
        {
            String l_line = l_br.readLine();
            while ( l_line != null )
            {
                String[] l_attributes = l_line.split(",");
                if(!l_attributes[1].equals("scenario_id") )
                {
                    ArrayList<Integer> l_temp = new ArrayList<>();
                    l_temp.add(Integer.valueOf(l_attributes[1]));
                    l_temp.add(Integer.valueOf(l_attributes[3]));
                    input.put(new StringBuffer(l_attributes[1]).append("@").append(l_attributes[2]).toString(),l_temp);
                }
                l_line = l_br.readLine();
            }
        }
        catch ( IOException ioe) { ioe.printStackTrace(); }
        return input;
    }

}

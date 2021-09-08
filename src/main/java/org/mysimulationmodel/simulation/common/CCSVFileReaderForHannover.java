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

public class CCSVFileReaderForHannover
{
    public static Map<String,ArrayList<String>> readDataFromCSV()
    {
        Map<String,ArrayList<String>> m_realdata = new HashMap<>();
        Path pathToFile = Paths.get( System.getProperty( "user.dir" ).concat("/UniHannover_416_original_target_coordinates.csv") );

        try ( BufferedReader l_br = Files.newBufferedReader( pathToFile, StandardCharsets.US_ASCII ) )
        {
            String l_line = l_br.readLine();
            while ( l_line != null )
            {
                String[] l_attributes = l_line.split(",");
                if( !l_attributes[0].equals("frame") ) {
                    ArrayList<String> l_temp = new ArrayList<>();
                    l_temp.add( String.valueOf( Double.parseDouble(l_attributes[2])/2.1185660421977854e+01f ) );
                    l_temp.add(String.valueOf( Double.parseDouble(l_attributes[3])/2.1185660421977854e+01f ));

                    m_realdata.put( new StringBuffer(String.valueOf((int)((Double.parseDouble(l_attributes[0]) * 100)) / 100.0))
                            .append(String.valueOf((int)((Double.parseDouble(l_attributes[1]) * 100)) / 100.0)).toString(), l_temp);

                }
                l_line = l_br.readLine();
            }
        }
        catch ( IOException ioe) { ioe.printStackTrace(); }
        return m_realdata;
    }

    //for genetic algo simulation
    public static Map<String, String> readDataFromCSVForSimulation()
    {

        Map<String,String> m_realdata = new HashMap<>();
        Path pathToFile = Paths.get( System.getProperty( "user.dir" ).concat("/UniHannover_416_original_target_coordinates.csv") );
        try ( BufferedReader l_br = Files.newBufferedReader( pathToFile, StandardCharsets.US_ASCII ) )
        {
            String l_line = l_br.readLine();
            while ( l_line != null )
            {
                String[] l_attributes = l_line.split(",");

                if(l_attributes.length == 5){
                    m_realdata.put( new StringBuffer(l_attributes[0]).append(l_attributes[1]).toString()
                            .replaceAll("\\s+",""), l_attributes[6] );}

                l_line = l_br.readLine();
            }
        }
        catch ( IOException ioe) { ioe.printStackTrace(); }
        return m_realdata;
    }
}

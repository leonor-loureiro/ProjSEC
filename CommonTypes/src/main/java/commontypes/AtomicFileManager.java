package commontypes;

import commontypes.Good;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class AtomicFileManager {

    public static void atomicFileMove(String sourceFile, String destinationFile)
            throws IOException {
        Path sourcePath = Paths.get(sourceFile);
        Path destinationPath = Paths.get(destinationFile);
        Files.move(sourcePath, destinationPath, StandardCopyOption.ATOMIC_MOVE);
    }

    public static void atomicWriteObjectToFile(String destFilename, ArrayList<?> content)
            throws IOException {

        //Temp file
        File tempFile = null;

        //For the write
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        //For the read
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        try {

            //Create a tmp file
            // Uses resource directory to avoid issues with multiple drives
            tempFile = File.createTempFile(destFilename + "-", ".tmp",  new File("../resources/"));

            fos = new FileOutputStream(tempFile.getAbsolutePath());
            oos = new ObjectOutputStream(fos);

            //Write object to file output stream
            oos.writeObject(content);

            //Flush the data from the stream into the buffer
            fos.flush();

            //Commit data to disk
            fos.getFD().sync();

            //Close buffer
            fos.close();


            //Data was successfully written to temp file
            //replace destinationFile with tempFile
            atomicFileMove(tempFile.getAbsolutePath(), destFilename);

        }finally {
            //Close output streams/buffers
            if(oos != null)
                oos.close();
            if(fos != null)
                fos.close();

            //Close input streams//buffers
            if(ois != null)
                ois.close();
            if(fis != null)
                fis.close();

            //Delete temporary file, if it was created
            if(tempFile != null && tempFile.exists())
                tempFile.delete();
        }

    }


}

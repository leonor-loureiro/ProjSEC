package commontypes;

import crypto.Crypto;
import crypto.CryptoException;
import javafx.util.Pair;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.*;
import java.util.*;

public class Utils {

    /**
     * This function is responsible for serializing an array list
     * @param arrayList array list to be serialized
     * @param filename file to store the serialized array list
     * @throws IOException
     */
    public static void serializeArrayList(ArrayList<?> arrayList, String filename) throws IOException {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(filename);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(arrayList);
        }finally {
            if(oos != null)
                oos.close();
            if(fos != null)
                fos.close();
        }
    }

    /**
     * This funtion is responsible for deserializing an array list
     * @param filename name of the file where the serialized object is stored
     * @return array list
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static ArrayList deserializeArrayList( String filename) throws IOException, ClassNotFoundException {
        ArrayList arrayList;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(filename);
            ois = new ObjectInputStream(fis);
            arrayList = (ArrayList) ois.readObject();
        }finally {
            ois.close();
            fis.close();
        }
        return arrayList;
    }

    private static final char[] alphabet = {'a', 'b' };/*, 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
            'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9','`','~','!','@','#','$','%','^','&','*','(',')','-','_','=','+',
            '|','{','}','[',']',';',':',',','<','.','>','/','?'};*/

    private static boolean verifyMD5(String MD5, String word){
        try {
            String hexText = computeMD5(word);

            return hexText.equals(MD5);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String computeMD5(String word) throws NoSuchAlgorithmException {
        MessageDigest digestInstance = MessageDigest.getInstance("MD5");

        //Calculate the MD5 digest
        byte[] digest = digestInstance.digest(word.getBytes());

        //Convert the digest into a single number representation
        BigInteger digestN = new BigInteger(1, digest);

        //Convert message to hex string
        StringBuilder hexText = new StringBuilder(digestN.toString(16));
        while (hexText.length() < 32)
            hexText.insert(0, "0");
        return hexText.toString();
    }

    public static List<String> crackMD5(String MD5){
        List<String> matches = new ArrayList<>();
        String word;
        for(char c1 : alphabet){
            for(char c2 : alphabet){
                word = String.valueOf(c1) + String.valueOf(c2);
                if(verifyMD5(MD5, word)){
                    matches.add(word);
                }
            }
        }
        return matches;
    }

    public static String crackPassword(byte[] plainText, byte[] cypher, byte[] IV){
        List<String> possiblePasswords = new ArrayList<>();
        nCharacterWords(possiblePasswords, alphabet, "", 24);
        System.out.println(possiblePasswords.size());

        for(String pass: possiblePasswords) {
            try {
                byte[] c = Crypto.encryptAES(pass.getBytes(), plainText, IV).getKey();
                if(Arrays.equals(c, cypher))
                    return pass;
            } catch (CryptoException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private static void nCharacterWords(List<String> out, char[] alphabet, String prefix, int n){
        if(n == 0) {
            //System.out.println(prefix);
            out.add(prefix);
            return;
        }
        for(char c : alphabet){
            nCharacterWords(out, alphabet, prefix + String.valueOf(c), n-1);
        }
    }

    private static HashSet<String> permute(String chars)
    {

        HashSet<String> set = new HashSet<>();

        for (int i=0; i<chars.length(); i++)
        {
            // Remove the character at index i from the string
            String remaining = chars.substring(0, i) + chars.substring(i+1);

            // Find all permutations of remaining chars
            for (String permutation : permute(remaining))
            {
                // Concatenate the first character with the permutations of the remaining chars
                set.add(chars.charAt(i) + permutation);
            }

        }
        return set;
    }

    private static String randomString(char[] alphabet, int n){
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        while (str.length() < n){
            int index = random.nextInt(2);
            str.append(alphabet[index]);
        }
        return str.toString();
    }


    public static void main(String[] args) throws CryptoException {
        String pass = randomString(alphabet, 24);
        System.out.println(pass);
        Pair<byte[], byte[]> cipherAndIV = Crypto.encryptAES(pass.getBytes(), "leonor".getBytes(), null);
        long start = System.currentTimeMillis();
        System.out.println(crackPassword("leonor".getBytes(), cipherAndIV.getKey(), cipherAndIV.getValue()));
        System.out.println("Time = " + (System.currentTimeMillis() - start));
    }

}

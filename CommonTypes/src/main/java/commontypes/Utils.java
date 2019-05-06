package commontypes;

import communication.Message;
import crypto.Crypto;
import crypto.CryptoException;
import javafx.util.Pair;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.*;
import java.util.*;

import static java.lang.System.currentTimeMillis;

public class Utils {

    public static Random random = new Random();
    /**
     * This function is responsible for serializing an array list
     * @param arrayList array list to be serialized
     * @param filename file to store the serialized array list
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
     * This function is responsible for deserializing an array list
     * @param filename name of the file where the serialized object is stored
     * @return array list
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



    /**************************************************************************
     *
     *                              ANTI-SPAM
     *
     **************************************************************************/

    private static final char[] alphabet = {'a', 'b' , 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
            'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9','`','~','!','@','#','$','%','^','&','*','(',')','-','_','=','+',
            '|','{','}','[',']',';',':',',','<','.','>','/','?'};

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

    /**
     * Find the random that hashed with the data has the given prefix
     * @param prefix md5 prefix
     * @param data data
     * @return random value
     */

    public static int proofOfWork(String prefix, String data) throws NoSuchAlgorithmException {
        String md5;
        int random = 0;
        int end = prefix.length();
        while(true){
            md5 = computeMD5(data + random);
            System.out.println(random + " -- " + md5.substring(0, end));
            if(md5.substring(0, end).equals(prefix))
                return random;
            random++;
        }
    }

    /**
     * Finds the data that generates the md5 hash
     * @param MD5 hash
     */
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


    /**
     * Finds the password that encrypting with AES-128 the plaintext generates the cypher
     */
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


    /**
     * Generates all possible words in the alphabet with the given prefix and length n
     * @param out stores the output
     * @param alphabet word's alphabet
     * @param prefix word's prefix
     * @param n word's length
     */
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

    /**
     * Generates all possible combinations of the characters in the string
     * @param chars input string
     * @return set of possible combinations
     */
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

    /**
     * Converts a byte array to hex string
     * @param byteArray byte array
     * @return hex string
     */
    public static String bytesToHex(byte[] byteArray) {

        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();

    }

    /**
     * Selects a random word
     * @param alphabet word's alphabet
     * @param n word's length
     * @return random word
     */
    private static String randomString(char[] alphabet, int n){
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        while (str.length() < n){
            int index = random.nextInt(alphabet.length);
            str.append(alphabet[index]);
        }
        return str.toString();
    }


    public static void main(String[] args) throws NoSuchAlgorithmException {

        long start = System.currentTimeMillis();
        String prefix = "12345";

        String data = Message.Operation.TRANSFER_GOOD + "|buyer|seller|good00|true|6|seller_7364592|";
        int result = proofOfWork(prefix, data);
        System.out.println("result=" + result);
        System.out.println("Time = " + (System.currentTimeMillis() - start)/1000);
    }

}

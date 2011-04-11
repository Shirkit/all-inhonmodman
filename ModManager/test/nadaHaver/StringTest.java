/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nadaHaver;

/**
 *
 * @author Shirkit
 */
public class StringTest {

    public static void main(String[] args) {

        String testString = "120938H>?<E) (*#l)  (*L192L731O````W=-0O 23423R209 382034 2L?><?>????D";
        String pattern = "[^A-Z,0-9, ]";
        String strippedString = testString.replaceAll(pattern, "");
        System.out.println("Original String is:         " + testString);
        System.out.println("After Replacing Characters: " + strippedString);
    }
}

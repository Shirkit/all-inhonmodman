/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package business.actions;

/**
 *
 * @author Usuário
 */
public interface ActionEditFileActions {

    /**
     * Replaced to be used by the XStreamConverter
     */
    public String toString(String string);

    /**
     * @return the content
     */
    public String getContent();

    /**
     * @param content the content to set
     */
    public void setContent(String content);



}

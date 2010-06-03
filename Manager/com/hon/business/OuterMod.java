/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package business;

/**
 *
 * @author Shirkit
 */
public class OuterMod {
    
    private boolean enabled;
    private Mod mod;
    private int priority;
    
    public OuterMod(Mod mod) {
        this.mod = mod;
        this.priority = -999999999;
    }

    public Mod getMod() {
        return mod;
    }

    /**
     * Method to check if the mod is enabled.
     * @return true if the mod is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Method to enable the current mod/
     */
    public void enable() {
        enabled = true;
    }

    /**
     * Method to disable the current mod/
     */
    public void disable() {
        enabled = false;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

}

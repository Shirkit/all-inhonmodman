/*
 Jaxe - Editeur XML en Java

 Copyright (C) 2008 Observatoire de Paris-Meudon

 Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conform�ment aux dispositions de la Licence Publique G�n�rale GNU, telle que publi�e par la Free Software Foundation ; version 2 de la licence, ou encore (� votre choix) toute version ult�rieure.

 Ce programme est distribu� dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans m�me la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de d�tail, voir la Licence Publique G�n�rale GNU .

 Vous devez avoir re�u un exemplaire de la Licence Publique G�n�rale GNU en m�me temps que ce programme ; si ce n'est pas le cas, �crivez � la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
 */

package jaxe;

import java.awt.datatransfer.*;
import java.io.IOException;

import org.w3c.dom.DocumentFragment;


public class XMLTransferable implements Transferable {
    public static DataFlavor XMLFragmentFlavor = new DataFlavor(FragmentXML.class, "Fragment XML");
    private FragmentXML fragment;
    
    public XMLTransferable(FragmentXML fragment) {
        this.fragment = fragment;
    }
    
    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] tdf = new DataFlavor[2];
        tdf[0] = XMLFragmentFlavor;
        tdf[1] = DataFlavor.stringFlavor;
        return(tdf);
    }
    
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(DataFlavor.stringFlavor))
            return(fragment.toString());
        else
            return(fragment);
    }
    
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return(DataFlavor.stringFlavor.equals(flavor) || XMLFragmentFlavor.equals(flavor));
    }
    
}

/*
Jaxe - Editeur XML en Java

Copyright (C) 2002 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conformément aux dispositions de la Licence Publique Générale GNU, telle que publiée par la Free Software Foundation ; version 2 de la licence, ou encore (à votre choix) toute version ultérieure.

Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans même la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de détail, voir la Licence Publique Générale GNU .

Vous devez avoir reçu un exemplaire de la Licence Publique Générale GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.macjaxe.generic;

import java.awt.Image;
import java.io.*;

import jaxe.Jaxe;
import jaxe.macjaxe.MacJaxe;

/**
 * Class à inclure au lieu de MacJaxeMacOS sur les plate-formes autres que MacOS X.
 */
public class MacJaxeGeneric implements MacJaxe {

    public MacJaxeGeneric() {
    }

    public void handleAbout() {
    }

    public void handleQuit() {
    }

    public void handleOpenFile(final File f) {
    }
    
    public void handleOpenApplication() {
    }
    
    public Image convertirPICT(final InputStream in) {
        return null;
    }
}

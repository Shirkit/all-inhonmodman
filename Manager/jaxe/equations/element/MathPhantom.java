/*
Jaxe - Editeur XML en Java

Copyright (C) 2003 Observatoire de Paris-Meudon

Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier conform�ment aux dispositions de la Licence Publique G�n�rale GNU, telle que publi�e par la Free Software Foundation ; version 2 de la licence, ou encore (� votre choix) toute version ult�rieure.

Ce programme est distribu� dans l'espoir qu'il sera utile, mais SANS AUCUNE GARANTIE ; sans m�me la garantie implicite de COMMERCIALISATION ou D'ADAPTATION A UN OBJET PARTICULIER. Pour plus de d�tail, voir la Licence Publique G�n�rale GNU .

Vous devez avoir re�u un exemplaire de la Licence Publique G�n�rale GNU en m�me temps que ce programme ; si ce n'est pas le cas, �crivez � la Free Software Foundation Inc., 675 Mass Ave, Cambridge, MA 02139, Etats-Unis.
*/

package jaxe.equations.element;

import java.awt.Graphics;

/**
 * This class represents a phantom of a math element
 * This is used as spacer
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @author <a href="mailto:sielaff@vern.chem.tu-berlin.de">Marco Sielaff</a>
 * @version %I%, %G%
 */
public class MathPhantom extends MathElement
{

  /** The XML element from this class */
  public final static String ELEMENT = "mphantom";  

  /**
   * Paints this element
   *
   * @param g The graphics context to use for painting
   * @param posX The first left position for painting
   * @param posY The position of the baseline
   */
  @Override
public void paint(final Graphics g, final int posX, final int posY)
  { 
  }  
}

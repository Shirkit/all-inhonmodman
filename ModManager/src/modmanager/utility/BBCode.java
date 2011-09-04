/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modmanager.utility;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Shirkit
 */
public class BBCode {

    public static String bbCodeToHtml(String text) {

        String temp = text.replace("\n", "<br>");
        Map<String, String> bbMap = new HashMap<String, String>();
        bbMap.put("\\[b\\](.+?)\\[/b\\]", "<strong>$1</strong>");
        bbMap.put("\\[i\\](.+?)\\[/i\\]", "<span style='font-style:italic;'>$1</span>");
        bbMap.put("\\[u\\](.+?)\\[/u\\]", "<span style='text-decoration:underline;'>$1</span>");
        bbMap.put("\\[s\\](.+?)\\[/s\\]", "<span style='text-decoration:line-through;'>$1</span>");
        bbMap.put("\\[h1\\](.+?)\\[/h1\\]", "<h1>$1</h1>");
        bbMap.put("\\[h2\\](.+?)\\[/h2\\]", "<h2>$1</h2>");
        bbMap.put("\\[h3\\](.+?)\\[/h3\\]", "<h3>$1</h3>");
        bbMap.put("\\[h4\\](.+?)\\[/h4\\]", "<h4>$1</h4>");
        bbMap.put("\\[h5\\](.+?)\\[/h5\\]", "<h5>$1</h5>");
        bbMap.put("\\[h6\\](.+?)\\[/h6\\]", "<h6>$1</h6>");
        bbMap.put("\\[quote\\](.+?)\\[/quote\\]", "<blockquote>$1</blockquote>");
        bbMap.put("\\[p\\](.+?)\\[/p\\]", "<p>$1</p>");
        bbMap.put("\\[p=(.+?),(.+?)\\](.+?)\\[/p\\]", "<p style='text-indent:$1px;line-height:$2%;'>$3</p>");
        bbMap.put("\\[center\\](.+?)\\[/center\\]", "<div align='center'>$1</div>");
        bbMap.put("\\[align=(.+?)\\](.+?)\\[/align\\]", "<div align='$1'>$2</div>");
        bbMap.put("\\[color=(.+?)\\](.+?)\\[/color\\]", "<span style='color:$1;'>$2</span>");
        bbMap.put("\\[size=(.+?)\\](.+?)\\[/size\\]", "<span style='font-size:$1;'>$2</span>");
        bbMap.put("\\[img\\](.+?)\\[/img\\]", "<img src='$1' />");
        bbMap.put("\\[img=(.+?),(.+?)\\](.+?)\\[/img\\]", "<img width='$1' height='$2' src='$3' />");
        bbMap.put("\\[email\\](.+?)\\[/email\\]", "<a href='mailto:$1'>$1</a>");
        bbMap.put("\\[email=(.+?)\\](.+?)\\[/email\\]", "<a href='mailto:$1'>$2</a>");
        bbMap.put("\\[url\\](.+?)\\[/url\\]", "<a href='$1'>$1</a>");
        bbMap.put("\\[url=(.+?)\\](.+?)\\[/url\\]", "<a href='$1'>$2</a>");
        //bbMap.put("\\[imgshow\\](.+?)\\[/imgshow\\]", "<div><iframe src='http://search.qxinnet.com/imgshow.php?k=$1&amp;imglist=0&amp;forum=0' frameborder='0' width='600' height='500' allowtransparency='0' scrolling='no'></iframe><p><small><a href='http://apps.facebook.com/imgshow/?keyword=$1' target='_blank'>Talk About $1</a></small></p></div>");
        //bbMap.put("\\[youtube\\](.+?)\\[/youtube\\]", "<object width='640' height='380'><param name='movie' value='http://www.youtube.com/v/$1'></param><embed src='http://www.youtube.com/v/$1' type='application/x-shockwave-flash' width='640' height='380'></embed></object>");
        //bbMap.put("\\[video\\](.+?)\\[/video\\]", "<video src='$1' />");

        for (Map.Entry entry : bbMap.entrySet()) {
            temp = temp.replaceAll("(?i)" + entry.getKey().toString(), entry.getValue().toString());
        }
        return temp;
    }

    public static String honCodeToBBCode(String text) {
        String temp = text;
        int i = text.indexOf("^");
        Map<String, String> map = new HashMap<String, String>();
        map.put("w", "[color=white]");
        map.put("r", "[color=red]");
        map.put("t", "[color=teal]");
        map.put("y", "[color=yellow]");
        boolean tagOpenned = false;
        while (i != -1) {
            // Search for next element after ^
            String temp2 = "" + temp.charAt(i + 1);
            // Check if it's a letter in the map
            String temp3 = map.get(temp2);
            if (temp3 != null) {
                if (tagOpenned) {
                    temp = temp.substring(0, i) + "[/color]" + temp3 + temp.substring(i + 1);
                } else {
                    temp = temp.substring(0, i) + temp3 + temp.substring(i + 1);
                    tagOpenned = true;
                }
            } else {
                // Check for sequence of 3 numbers
                if (Character.isDigit(temp.charAt(i + 1)) && Character.isDigit(temp.charAt(i + 2)) && Character.isDigit(temp.charAt(i + 3))) {
                    float fx = (((int) temp.charAt(i + 1)) * 15) / 9;
                    float fy = (((int) temp.charAt(i + 2)) * 15) / 9;
                    float fz = (((int) temp.charAt(i + 3)) * 15) / 9;
                    int x = (int) fx;
                    int y = (int) fy;
                    int z = (int) fz;
                    if (tagOpenned) {
                        temp = temp.substring(0, i) + "[/color][color=#" + x + "" + y + "" + z + "]" + temp.substring(i + 4);
                        System.out.println(temp);
                    } else {
                        temp = temp.substring(0, i) + "[color=#" + x + "" + y + "" + z + "]" + temp.substring(i + 4);
                        System.out.println(temp);
                        tagOpenned = true;
                    }
                }
            }
            i = temp.indexOf("^", i + 1);
        }
        if (tagOpenned) {
            temp = temp + "[/color]";
        }
        return temp;
    }

    public static void main(String[] args) {
        System.out.println(honCodeToBBCode("a ^153roi"));
    }
}

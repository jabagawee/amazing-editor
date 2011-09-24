/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reposerv;

import java.util.ArrayList;
import java.io.*;

/**
 *
 * @author Stephen
 */
public class RealTimeEditingObject {
    public String text;
    public String filename;
    public int version;
    public ArrayList<RepoServ> clientsEditing;
    public BufferedWriter out;
    public RealTimeEditingObject(String initText, String fn, int ver, ArrayList<RepoServ> clients) {
        text = initText;
        filename = fn;
        version = ver;
        clientsEditing = clients;
    }
}

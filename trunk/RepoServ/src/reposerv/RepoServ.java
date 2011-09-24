/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reposerv;

import java.io.*;
import java.net.*;
import java.util.*;
import reposerv.diff_match_patch.*;
/**
 *
 * @author Stephen
 */
class FileFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
        return dir.isFile();
    }
}

public class RepoServ implements Runnable {
    
    //Inactivity set to 5 minutes
    private final static int INACTIVITY_CUTOFF = 300000; //milliseconds til inactive shutdown
    public final static int GRANULARITY = 10000; //How big chunks do we split or data.

    private Socket connection = null;
    private String timeStamp = null;
    public String username = "Unauthenticated";
    private int ID;
    private ArrayList<RepoServ> clients;
    private ArrayList<RealTimeEditingObject> rteos;
    public RealTimeEditingObject currentRTEO = null;
    public InetAddress ip = null;
    
    RepoServ(Socket s, int i, ArrayList<RepoServ> c, ArrayList<RealTimeEditingObject> rt, InetAddress address) {
        this.connection = s;
        this.ID = i;
        this.clients = c;
        this.rteos = rt;
        this.ip = address;
    }
    
    public void run() {
        try {
            System.out.println("Client connected from IP "+connection.getInetAddress());
            BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
            long timeSinceLastActivity = System.currentTimeMillis();
            boolean clientConnected = true;
            boolean authenticated = false;
            while (clientConnected) {
                if(is.available()==0) { //Idle, no messages.
                    long timeNow = System.currentTimeMillis();
                    if (timeNow-timeSinceLastActivity > INACTIVITY_CUTOFF) {
                        clientConnected = false;
                        System.out.println("Inactive, timing out.");
                    }
                    continue;
                }
                timeSinceLastActivity = System.currentTimeMillis();
                int character;
                //PROTOCOL:
                //byte 0 = command
                //byte 1-4 = length of message (as int)
                //byte 5-end = message
                // commands:
                // r = 114 = Request file, format [114][nums][nums]....[nums]
                int commandNum = is.read();
                byte[] lengthBuffer = new byte[4];
                is.read(lengthBuffer, 0, 4);
                int msgLength = RepoUtils.getInt(lengthBuffer, 0);
                byte[] message = new byte[msgLength];
                is.read(message, 0, msgLength);
                System.out.println("Message Protocol Command: " + (char) commandNum+"/"+msgLength);
                //System.out.println("Message Length: " + msgLength);
                //System.out.println("Received Message: " + new String(message));

                byte[] returnBytes = RepoUtils.getErrorMessage("Unexpected error.");
                
                if((char)commandNum == 'a') {
                    byte[] pwL = new byte[4];
                    is.read(pwL, 0, 4);
                    int pwLen = RepoUtils.getInt(pwL, 0);
                    byte[] pw = new byte[pwLen];
                    is.read(pw, 0, pwLen);
                    String un = new String(message);
                    String password = new String(pw);
                    boolean usernameAlreadyLogged = false;
                    System.out.println("Attempting auth with UN: "+un+" PW: "+password);
                    for(int i = 0; i<clients.size(); i++) {
                        if(clients.get(i).username == null ? un == null : clients.get(i).username.equals(un))
                            usernameAlreadyLogged = true;
                    }
                    if(usernameAlreadyLogged)
                        returnBytes = RepoUtils.getErrorMessage(un+" is already logged on.");
                    else if(un.startsWith("user")&&password.equals("cookies")) {
                        authenticated = true;
                        returnBytes = RepoUtils.getSuccessMessage(un+" login success.");
                        System.out.println("Login successful for "+un);
                        username = un;
                    } else {
                        returnBytes = RepoUtils.getErrorMessage("Username or password is incorrect.");
                        System.out.println("Login failed for "+un);
                    }
                } else if ((char)commandNum == 'l') { //disconnect, ([l]ogoff)
                    clientConnected = false;
                    if(this.currentRTEO.clientsEditing.size() == 1)
                        this.currentRTEO.out.close();
                    OutputStream os = connection.getOutputStream();
                    os.write(RepoUtils.getSuccessMessage("Disconnecting, adios!"));
                    os.flush();
                    continue;
                } else if(authenticated == false) {
                    switch((char) commandNum) {
                        case 'e': //waste next 4 chars
                            is.skip(4);
                            break;
                        case 'r': //waste next 4 chars
                            is.skip(4);
                            break;
                        case 'w': //waste next n chars, where n is specified by next 4 chars
                            byte[] fileLengthBuffer = new byte[4];
                            is.read(fileLengthBuffer, 0, 4);
                            int fileLength = RepoUtils.getInt(fileLengthBuffer, 0);
                            is.skip(fileLength);
                            break;
                    }
                    OutputStream os = connection.getOutputStream();
                    System.out.println("Unauthenticated request, refusing command "+(char)commandNum);
                    os.write(RepoUtils.getErrorMessage("Unauthenticated, refused command "+(char)commandNum));
                    os.flush();
                    continue;
                } else {
                    switch ((char) commandNum) {
                        case 'y': //Stamds for getting all the diffs of a file.
                            String filenam = new String(message);
                            byte[] diffs = RepoCommands.getDiffs(filenam);
                            returnBytes = RepoUtils.headerizeMessage('y', diffs);
                            break;
                        case 'q': //Stands for get directory of files. Why q? BECAUSE I LIKE IT
                            File mainDir = new File("diffs");
                            String[] allFiles = mainDir.list();
                            java.util.Arrays.sort(allFiles, String.CASE_INSENSITIVE_ORDER);
                            String concatStrings = "";
                            for(int i = 0; i < allFiles.length; i++) {
                                concatStrings += allFiles[i];
                                if(i != allFiles.length-1)
                                    concatStrings += ";";
                            }
                            returnBytes = RepoUtils.headerizeMessage('q', concatStrings.getBytes());
                            break;
                        case 'd': //Diff message for RTEO, update server side version of file and then propagate changes.
                            if (currentRTEO == null) {
                                returnBytes = RepoUtils.getErrorMessage("Sending diffs when not bound to RTEO");
                                System.out.println("Sending diffs when not bound to RTEO");
                                break;
                            }
                            String diffString = new String(message); // get diff string
                            if (message.length == 0)
                                break;
                            diff_match_patch differ = new diff_match_patch();
                            LinkedList patches = (LinkedList)differ.patch_fromText(diffString);
                            String newText = (String)differ.patch_apply(patches, currentRTEO.text)[0];
                            currentRTEO.text = newText;
                            for(int i = 0; i<currentRTEO.clientsEditing.size(); i++) {
                                RepoServ client = currentRTEO.clientsEditing.get(i);
                                if(!client.connection.isClosed()&&client.username == null ? this.username != null : !client.username.equals(this.username)) { //don't propagate to self, silly!
                                    OutputStream ostream = client.connection.getOutputStream();
                                    ostream.write(RepoUtils.headerizeMessage('d', message));
                                    ostream.flush();
                                }
                            }
                            String[] diffsArray = diffString.split("\n");
                            for(int i = 0; i < diffsArray.length; i++) {
                                if(diffsArray[i].startsWith("@")) {
                                    currentRTEO.version++;
                                }
                                currentRTEO.out.write((diffsArray[i]+"\n"));
                                currentRTEO.out.flush();
                                if(currentRTEO.version%GRANULARITY == 0) {
                                    int diffsFileNum = currentRTEO.version / GRANULARITY;
                                    File currDiffs = new File("diffs/" + currentRTEO.filename + "/diffs" + diffsFileNum);
                                    currentRTEO.out.flush();
                                    currentRTEO.out.close();
                                    currentRTEO.out = new BufferedWriter(new FileWriter(currDiffs, true));
                                    RepoCommands.writeFile(new File("diffs/"+currentRTEO.filename+"/base"+currentRTEO.version/GRANULARITY), currentRTEO.text.getBytes());
                                }
                            }
                            returnBytes = RepoUtils.getSuccessMessage("diff received");
                            break;
                        case 'e': //Binds to a file for real time editing, and unbinds from previos RTEO.
                            if(currentRTEO != null) {
                                currentRTEO.clientsEditing.remove(this);
                                if (currentRTEO.clientsEditing.isEmpty()) {
                                    rteos.remove(currentRTEO);
                                    currentRTEO.out.flush();
                                    currentRTEO.out.close();
                                }
                                currentRTEO = null;
                            }
                            String fn = new String(message);
                            byte[] vsn = new byte[4];
                            is.read(vsn, 0, 4);
                            int vers = RepoUtils.getInt(vsn, 0);
                            if(vers==-1) {
                                vers = RepoCommands.numDiffs(new String(message));
                            }
                            boolean rteoOpened = false;
                            ArrayList<RepoServ> soloClientList = new ArrayList<RepoServ>();
                            soloClientList.add(this);
                            RealTimeEditingObject activeRTEO = new RealTimeEditingObject("", fn, vers, soloClientList);
                            for(int i = 0; i<rteos.size();i++) {
                                RealTimeEditingObject obj = rteos.get(i);
                                if((obj.filename == null ? fn == null : obj.filename.equals(fn)) && obj.version == vers) {
                                    rteoOpened = true;
                                    activeRTEO = obj;
                                    activeRTEO.clientsEditing.add(this);
                                    i = rteos.size();
                                }
                            }
                            if(!rteoOpened) {
                                String newOpText = RepoCommands.getVersionNLambda(fn, vers);
                                if(newOpText == null) {
                                    returnBytes = RepoUtils.getErrorMessage("Error making new RTEO "+fn+" ver. "+vers);
                                    System.out.println("Error making new RTEO "+fn+" ver. "+vers);
                                }
                                activeRTEO.text = newOpText;
                                rteos.add(activeRTEO);
                                int numDiffs = RepoCommands.numDiffs(fn);
                                int diffsFileNum = numDiffs/GRANULARITY;
                                File currDiffs = new File("diffs/"+fn+"/diffs"+diffsFileNum);
                                activeRTEO.version = numDiffs;
                                activeRTEO.out = new BufferedWriter(new FileWriter(currDiffs, true));
                                //returnBytes = RepoUtils.getSuccessMessage("Made new RTEO, "+fn+" ver. "+vers);
                                returnBytes = RepoUtils.headerizeMessage('f', newOpText.getBytes());
                                System.out.println("Made new RTEO, "+fn+" ver. "+vers);
                            } else {
                                //returnBytes = RepoUtils.getSuccessMessage("Bound to existing RTEO "+fn+" ver. "+vers);
                                returnBytes = RepoUtils.headerizeMessage('f', activeRTEO.text.getBytes());
                                System.out.println("Bound to existing RTEO "+fn+" ver. "+vers);
                            }
                            currentRTEO = activeRTEO;
                            break;
                        case 'u': //Unbinds from current RTEO.
                            if(currentRTEO == null) { //No bound RTEO, error.
                                returnBytes = RepoUtils.getErrorMessage("No bound RTEO found.");
                                System.out.println("No bound RTEO found.");
                            } else { //Unbind RTEO.
                                currentRTEO.clientsEditing.remove(this);
                                if (currentRTEO.clientsEditing.isEmpty()) {
                                    rteos.remove(currentRTEO);
                                    currentRTEO.out.flush();
                                    currentRTEO.out.close();
                                }
                                returnBytes = RepoUtils.getSuccessMessage("Unbound RTEO "+currentRTEO.filename+" ver. "+currentRTEO.version);
                                System.out.println("Unbound RTEO "+currentRTEO.filename+" ver. "+currentRTEO.version);
                                this.currentRTEO = null;
                            }
                            break;
                        case 'r': //Get and ftp file back:
                            //PROTOCOL: byte end-end+3 = length of version num
                            // byte end+3 to endend = version num to retrieve
                            byte[] vs = new byte[4];
                            is.read(vs, 0, 4);
                            int version = RepoUtils.getInt(vs, 0);
                            if(version==-1) {
                                version = RepoCommands.numDiffs(new String(message));
                            }
                            System.out.println("Attempting to retreive '" + new String(message) + "' file, version " + version);
                            String file = RepoCommands.getVersionNLambda(new String(message), version);
                            if (file == null) {
                                returnBytes = RepoUtils.getErrorMessage("Failed to retreive the file: " + new String(message));
                                System.out.println("Failed to retrieve file, error logged.");
                            } else {
                                byte[] fileBytes = file.getBytes();
                                returnBytes = RepoUtils.headerizeMessage('f', fileBytes);
                                System.out.println("Successfully sent file contents, size " + fileBytes.length);
                            }
                            break;
                        case 'w': //Writing a file: makes new file if doesn't exist, otherwise writes a diff.
                            //This has an extra 4 bytes for length of file, followed by file contents.
                            String filename = new String(message);
                            File ourFile = new File(filename);
                            byte[] fileLengthBuffer = new byte[4];
                            is.read(fileLengthBuffer, 0, 4);
                            int fileLength = RepoUtils.getInt(fileLengthBuffer, 0);
                            byte[] fileContents = new byte[fileLength];
                            is.read(fileContents, 0, fileLength);
                            if (ourFile.exists()) { //file exists, write diffs
                                File dir = new File("diffs/" + filename);
                                if (!dir.exists()) {
                                    dir.mkdir();
                                }
                                File baseFile = new File("diffs/"+filename+"/base0");
                                if (!baseFile.exists()) { //Write new file.
                                    boolean successful = RepoCommands.writeFile(new File("diffs/" + filename + "/base0"), fileContents);
                                    if (!successful) {
                                        returnBytes = RepoUtils.getErrorMessage("File diff failed, could not write file.");
                                    } else {
                                        returnBytes = RepoUtils.getSuccessMessage("File diff written.");
                                    }
                                } else { //Write diff file.
                                    /*
                                     * diff_match_patch differ1 = new diff_match_patch();
                                    String patch = differ1.patch_toText(differ1.patch_make(oldFile, new String(fileContents)));
                                    boolean successful = RepoCommands.writeFile(new File("diffs/" + filename + "/" + currVer), patch.getBytes());
                                    if (!successful) {
                                        returnBytes = RepoUtils.getErrorMessage("File diff failed, could not write file.");
                                    } else {
                                        returnBytes = RepoUtils.getSuccessMessage("File diff written.");
                                    }
                                     * 
                                     */
                                    returnBytes = RepoUtils.getErrorMessage("Cannot write new file when it already exists.");
                                }
                            } else { //No such file, make file.
                                boolean successful = RepoCommands.writeFile(ourFile, fileContents);
                                if (!successful) {
                                    returnBytes = RepoUtils.getErrorMessage("File writing was not successful.");
                                } else {
                                    File dir = new File("diffs/" + filename);
                                    if (!dir.exists()) {
                                        dir.mkdir();
                                    }
                                    returnBytes = RepoUtils.getSuccessMessage("File written.");
                                }
                            }
                            break;
                        default:
                            returnBytes = RepoUtils.getErrorMessage("Unrecognized command: " + (char) commandNum);
                            break;
                    }
                }
                System.out.println("");
                OutputStream os = connection.getOutputStream();
                os.write(returnBytes);
                os.flush();
            }
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            try {
                clients.remove(this);
                if(this.currentRTEO.clientsEditing.size() == 1)
                    rteos.remove(this.currentRTEO);
                this.currentRTEO.clientsEditing.remove(this);
                System.out.println("Closing connection with client "+connection.getInetAddress());
                connection.close();
            } catch (IOException e) {}
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        int port = 19999;
        int count = 0;
        try {
            ServerSocket socket1 = new ServerSocket(port, 0, InetAddress.getLocalHost());
            
            //BELOW FOR LOCALHOST LOCAL TESTING
            //ServerSocket socket1 = new ServerSocket(port, 0, InetAddress.getByName("localhost"));
            System.out.println("Starting up Repository Server...");
            System.out.println("Server @ "+socket1.getLocalSocketAddress());
            ArrayList<RepoServ> users = new ArrayList<RepoServ>();
            ArrayList<RealTimeEditingObject> rteos = new ArrayList<RealTimeEditingObject>();
            while (true) {
                Socket connection = socket1.accept();
                RepoServ runnable = new RepoServ(connection, ++count, users, rteos, connection.getInetAddress());
                users.add(runnable);
                Thread thread = new Thread(runnable);
                thread.start();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}

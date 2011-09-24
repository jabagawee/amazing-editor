/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reposerv;

import java.io.*;
import java.util.LinkedList;

/**
 *
 * @author Stephen
 */
public class RepoCommands {
    
    public static byte[] getByteArrayFile(byte[] fn) {
        try {
            String filename = new String(fn);
            File theFile = new File(filename);
            byte[] fileBytes = new byte[(int)theFile.length()];
            FileInputStream fis = new FileInputStream(theFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(fileBytes, 0, fileBytes.length);
            return fileBytes;
        } catch (Exception IOException) {
            return null;
        }
    }
    public static byte[] getByteArrayFile(File theFile) {
        try {
            byte[] fileBytes = new byte[(int)theFile.length()];
            FileInputStream fis = new FileInputStream(theFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(fileBytes, 0, fileBytes.length);
            return fileBytes;
        } catch (Exception IOException) {
            return null;
        }
    }
    
    public static boolean writeFile(File file, byte[] contents) {
        BufferedOutputStream bos = null;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(contents);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    //Gets version 'n' of the given file as a string.
    public static String getVersionN(String filename, int n) {
        try {
            diff_match_patch differ = new diff_match_patch();
            int startDiff = n / 10;
            int diffsToApply = n % 10;
            File baseFile = new File("diffs/" + filename + "/" + startDiff * 10);
            if (n < 10) { //Using original file as base.
                baseFile = new File(filename);
            }
            String text = new String(getByteArrayFile(baseFile));
            for (int i = 1; i < diffsToApply + 1; i++) {
                String patchText = new String(getByteArrayFile(new File("diffs/" + filename + "/" + (10 * startDiff + i))));
                text = (String) differ.patch_apply((LinkedList) differ.patch_fromText(patchText), text)[0];
            }
            return text;
        } catch(Exception e) {
            return null;
        }
    }
    
    public static int numDiffs(String filename) {
        try {
            diff_match_patch differ = new diff_match_patch();
            File baseDir = new File("diffs/" + filename);
            int numDiffFiles = baseDir.list().length/2;
            int count = 0;
            for(int i = 0; i<numDiffFiles; i++) {
                String diffText = new String(getByteArrayFile(new File("diffs/" + filename + "/diffs" + i)));
                String[] diffs = diffText.split("\n");
                for(int j = 0; j < diffs.length; j++) {
                    if(diffs[j].startsWith("@"))
                        count++;
                }
            }
            return count;
        } catch(Exception e) {
            return 0;
        }
    }
    
    public static String getVersionNLambda(String filename, int n) {
        try {
            diff_match_patch differ = new diff_match_patch();
            int startDiff = n / RepoServ.GRANULARITY;
            int diffsToApply = n % RepoServ.GRANULARITY;
            File baseFile = new File("diffs/" + filename + "/base" + startDiff);
            String text = new String(getByteArrayFile(baseFile));
            if (diffsToApply == 0)
                return text;
            String diffText = new String(getByteArrayFile(new File("diffs/" + filename + "/diffs" + startDiff)));
            String[] diffs = diffText.split("\n");
            String patch = "";
            for (int j = 0; j < diffsToApply; j++) {
                if(diffs[j].startsWith("@") && j!=0) {
                    text = (String) differ.patch_apply((LinkedList) differ.patch_fromText(patch), text)[0];
                    patch = "";
                }
                patch += (diffs[j]+"\n");
            }
           text = (String) differ.patch_apply((LinkedList) differ.patch_fromText(patch), text)[0];
            return text;
        } catch(Exception e) {
            return null;
        }
    }
    
    public static byte[] getDiffs(String filename) {
        try {
            diff_match_patch differ = new diff_match_patch();
            String diffs = "";
            int count = 0;
            File diffFile = new File("diffs/"+filename+"/diffs0");
            //while(diffFile.exists()) {
            //    diffs = new String(getByteArrayFile(diffFile));
            //    count++;
            //    diffFile = new File("diffs/"+filename+"/diffs"+count);
            //}
            //return diffs;
            return getByteArrayFile(diffFile);
        } catch(Exception e) {
            return null;
        }
    }
}

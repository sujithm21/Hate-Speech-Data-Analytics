/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TermCounter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author AnimeshChaturvedi
 */
public class BasicUtilities {

    public static ArrayList<String> getStopWords(String fileName) {
        ArrayList<String> rawKeywords = new ArrayList<String>();
        try {
            FileReader fl = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fl);
            String word;
            while ((word = br.readLine()) != null) {
                rawKeywords.add(word.trim());
            }
            br.close();
            fl.close();
        } catch (IOException e) {
            System.out.println("Missing File");
        }
        return rawKeywords;
    }
    
    
    public static ArrayList<String> getHateTermStem(String fileName) {
        ArrayList<String> rawKeywords = new ArrayList<String>();
        try {
            FileReader fl = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fl);
            String word;
            while ((word = br.readLine()) != null) {
                rawKeywords.add(word.trim());
            }
            br.close();
            fl.close();
        } catch (IOException e) {
            System.out.println("Missing File");
        }
        return rawKeywords;
    }

    public static void deleteFolder(File file) {
        for (File subFile : file.listFiles()) {
            if (subFile.isDirectory()) {
                deleteFolder(subFile);
            } else {
                subFile.delete();
            }
        }
    }

    public static HashMap<?, ?> sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }
    
    public static HashMap sortByKeys(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getKey()).compareTo(((Map.Entry) (o2)).getKey());
            }
        });

        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public static ArrayList<String> sortAscending(ArrayList<String> arrayList) {
        Collections.sort(arrayList);
        return arrayList;
    }

    public static ArrayList<String> unique(ArrayList<String> list) {
        Set<String> unique = new HashSet<String>();
        for (String word : list) {
            unique.add(word);
        }
        return new ArrayList<String>(unique);
    }
    
    public static boolean containsIgnoreCase(String str, String subString) {
        return str.toLowerCase().contains(subString.toLowerCase());
    }
    
    public static int countFreq(String pat, String txt) {        
        int M = pat.length();        
        int N = txt.length();        
        int res = 0;
 
        /* A loop to slide pat[] one by one */
        for (int i = 0; i <= N - M; i++) {
            /* For current index i, check for 
        pattern match */
            int j;            
            for (j = 0; j < M; j++) {
                if (txt.charAt(i + j) != pat.charAt(j)) {
                    break;
                }
            }
 
            // if pat[0...M-1] = txt[i, i+1, ...i+M-1] 
            if (j == M) {                
                res++;                
                j = 0;                
            }            
        }        
        return res;        
    }
}

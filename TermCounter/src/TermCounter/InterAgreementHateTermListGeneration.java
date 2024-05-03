package TermCounter;

import static TermCounter.BasicUtilities.containsIgnoreCase;
import static TermCounter.BasicUtilities.countFreq;
import static TermCounter.BasicUtilities.deleteFolder;
import static TermCounter.BasicUtilities.getStopWords;
import static TermCounter.BasicUtilities.sortAscending;
import static TermCounter.BasicUtilities.sortByKeys;
import static TermCounter.BasicUtilities.sortByValues;
import static TermCounter.BasicUtilities.unique;

import java.io.BufferedReader;
import opennlp.tools.stemmer.PorterStemmer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import static java.util.stream.Collectors.toMap;

/**
 *
 * @author AnimeshChaturvedi
 */
public class InterAgreementHateTermListGeneration {

    static String path = "D:\\mini\\";
    static HashMap<String, Integer> allTermFrequency = new HashMap<>();
    static HashMap<String, Integer> topTermFrequency = new HashMap<>();
    static ArrayList<String> outerJoinHTs_Single_HTList = new ArrayList<String>();
    static ArrayList<String> interHTs_Multi_HTList_FScore = new ArrayList<String>();
    static ArrayList<String> interHTs_Multi_HTList_Percentage = new ArrayList<String>();
    static HashMap<Integer, Integer> countLinesIfMultipleTermOccurs = new HashMap<>();
    static HashMap<String, Integer> sumMultipleTermOccurs = new HashMap<>();
    static HashMap<String, Integer> countLinesIfATermOccurs = new HashMap<>();
    static HashMap<String, Integer> sumATermOccurs = new HashMap<>();
    static HashMap<String, Integer> HTLines = new HashMap<>();
    static int total_lines = 0;
    static int onlyHate = 0, onlyOffensive = 0, onlyNonOffensive = 0, hateOffensive = 0, offensiveNonOffensive = 0, hateNonOffensive = 0;
    static float onlyHatePercent = 0, onlyOffensivePercent = 0, onlyNonOffensivePercent = 0, hateOffensivePercent = 0, offensiveNonOffensivePercent = 0, hateNonOffensivePercent = 0;
    static int totalHate = 0, totalOffensive = 0, totalNonOffensive = 0, totalHateOffensive = 0, totalOffensiveNonOffensive = 0, totalHateNonOffensive = 0;
    static int onlyHateTerms = 0, hateOffensiveTerms = 0, offensiveNonOffensiveTerms = 0, nonOffensiveTerms = 0;
//    static int totalOnlyHateTerms = 0, totalOffensiveNonOffensiveTerms = 0, totalHateOffensiveTerms = 0, totalNonOffensiveTerms = 0;

    public static void main(String[] args) throws Exception {

        System.out.println("             HateTermsOverSpeechClass  ");
        File f1 = new File(path + "\\Datasets\\");
        File f2 = new File(path + "\\BestHTList\\");

        File[] dbFiles = f1.listFiles();
        File[] HTFiles = f2.listFiles();

        File countLinesIfAnyTermOccursFile = new File(path + "\\SummaryHateTermsHTs(N).csv");
        BufferedWriter bufwriter = new BufferedWriter(new FileWriter(countLinesIfAnyTermOccursFile, false));
        bufwriter.write("Dataset Name and Class, HateList Name, HateTerms(N), N(Entries), TotalLines, %(Entries)\n");
        bufwriter.close();

// Deprecated ConfusionMatrix.csv function  
//        File ConfusionMatrixFile = new File(path + "\\ConfusionMatrix.csv");
//        BufferedWriter firstLineConfusionMatrixFileWriter = new BufferedWriter(new FileWriter(ConfusionMatrixFile, false));
//        firstLineConfusionMatrixFileWriter.write("HateList Name, Dataset Name and Class, HT Lines, #Lines, % HTs Lines, TP, TN, FP, FN, Accuracy, Recall, Precision, F-Measure \n");
//        firstLineConfusionMatrixFileWriter.close();
// Deprecated ConfusionMatrix.csv function  
        File interAgreementConfusionMatrixFile = new File(path + "\\InterAgreementConfusionMatrix.csv");
        BufferedWriter firstLineInterAgreementConfusionMatrixFileWriter = new BufferedWriter(new FileWriter(interAgreementConfusionMatrixFile, false));
        firstLineInterAgreementConfusionMatrixFileWriter.write("HateList Name, Dataset Name and Class, HT Lines, #Lines, % HTs Lines, %TP, %TN, %FP, %FN, Accuracy, Recall, Precision, F-Measure \n");
        firstLineInterAgreementConfusionMatrixFileWriter.close();

        interHTs_Multi_HTList_FScore = new ArrayList<String>();

        totalDatasetLines(dbFiles);
        totalHTs(HTFiles);

        for (int j = 0; j < HTFiles.length; j++) {
            String HTFileName = HTFiles[j].getName().substring(0, HTFiles[j].getName().lastIndexOf("."));
            System.out.println("\nHate Term File Name: " + HTFiles[j].getName());
            sumMultipleTermOccurs = new HashMap<>();
            sumATermOccurs = new HashMap<>();

            File dirHTFiles = new File(path + HTFileName);
            if (dirHTFiles.exists()) {
                deleteFolder(dirHTFiles);
            }
            if (!dirHTFiles.exists()) {
                dirHTFiles.mkdir();
                System.out.println("Create dbFile " + path + HTFiles[j].getName().substring(0, HTFiles[j].getName().lastIndexOf(".")));
            }

//            File TermMatrixFile = new File(path + "\\" + HTFileName + "\\TermConfusionMatrix.csv");
//            BufferedWriter writerFirstLineTermMatrix = new BufferedWriter(new FileWriter(TermMatrixFile, false));
//            writerFirstLineTermMatrix.write("Hate Terms (HTs), HateClass HTs Lines, # Offensive+NonOffensive HTs Lines, # HateClassLines, Recall(HateClass), Precision(HateClass), # Hate+Offensive HTs Lines, NonOffensive HTs Lines, # Hate+Offensive Lines, Recall(Hate+Offensive), Precision(Hate+Offensive)\n");
//            writerFirstLineTermMatrix.close();
            File intraAgreementTermMatrixFile = new File(path + "\\" + HTFileName + "\\IntraAgreementTermMatrix.csv");
            BufferedWriter writerFirstLineInterAgreementTermMatrix = new BufferedWriter(new FileWriter(intraAgreementTermMatrixFile, false));
            writerFirstLineInterAgreementTermMatrix.write("Hate Terms (HTs), HateClass HTs Lines, # Offensive+NonOffensive HTs Lines, # HateClassLines, Recall(HateClass), Precision(HateClass), # Hate+Offensive HTs Lines, NonOffensive HTs Lines, # Hate+Offensive Lines, Recall(Hate+Offensive), Precision(Hate+Offensive)\n");
            writerFirstLineInterAgreementTermMatrix.close();

            for (int i = 0; i < dbFiles.length; i++) {
                String DBFileName = dbFiles[i].getName().substring(0, dbFiles[i].getName().lastIndexOf("."));

                File dirHTLists = new File(dirHTFiles.getAbsolutePath() + "\\" + DBFileName);
                if (dirHTLists.exists()) {
                    deleteFolder(dirHTLists);
                }
                if (!dirHTLists.exists()) {
                    dirHTLists.mkdir();
                    System.out.println("Create HTFile " + dirHTFiles.getAbsolutePath() + "\\" + DBFileName);
                }

                allTermFrequency = new HashMap<>();
                topTermFrequency = new HashMap<>();
                total_lines = 0;
                countLinesIfMultipleTermOccurs = new HashMap<>();
                countLinesIfATermOccurs = new HashMap<>();

                process(dbFiles[i], HTFiles[j]);

                makeAllTopHateTermFrequencies(HTFileName, DBFileName);
//                
                sumHateTermLines(DBFileName);
                makeSummaryHatePercentLineFiles(HTFileName, DBFileName);
                sumHateLines(DBFileName);
            }

//            summaryConfusionMatrix4Cases(HTFileName);
//            summaryTermConfusionMatrix4Cases(HTFileName); 
            interAgreementConfusionMatrix(HTFileName);
            intraHTsListMatrix(HTFileName);

            outerJoinHTs_Single_HTList = new ArrayList<String>();
            makeOuterJoinHateTermFrequencies(dbFiles, HTFileName);
            outerJoinHTs_Single_HTList = new ArrayList<String>();
            makeOuterJoinHateTermPercentLines(dbFiles, HTFileName);

            System.out.println("\n");
        }
        interHTs_Multi_HTList_FScore = new ArrayList<String>();
        interAgreementTerms(HTFiles);
        interHTs_Multi_HTList_Percentage = new ArrayList<String>();
        interHTsPercentLines(HTFiles);

// Deprecated ConfusionMatrix.csv function  
//        BufferedWriter lastLineConfusionMatrixFileWriter = new BufferedWriter(new FileWriter(ConfusionMatrixFile, true));
//        lastLineConfusionMatrixFileWriter.write("Accuracy = TP + TN / ( TP + TN + FP + FN), , , , , , , , , , , , \n");
//        lastLineConfusionMatrixFileWriter.write("Recall = TP / (TP + FP), , , , , , , , , , , , \n");
//        lastLineConfusionMatrixFileWriter.write("Precision = TP / (TP + FN), , , , , , , , , , , , \n");
//        lastLineConfusionMatrixFileWriter.write("F-Measure = 2(Pre*Rec)/(Pre+Rec), , , , , , , , , , , , \n");
//        lastLineConfusionMatrixFileWriter.close();
// Deprecated ConfusionMatrix.csv function  
        BufferedWriter lastLineInterAgreementConfusionMatrixFileWriter = new BufferedWriter(new FileWriter(interAgreementConfusionMatrixFile, true));
        lastLineInterAgreementConfusionMatrixFileWriter.write("Case: Class A Over B, , , , , , , , , , , , \n");
        lastLineInterAgreementConfusionMatrixFileWriter.write("Case 1: Hate Over Non-Offensive, , , , , , , , , , , , \n");
//        lastLineInterAgreementConfusionMatrixFileWriter.write("Case 2: Hate Over Offensive, , , , , , , , , , , , \n");
//        lastLineInterAgreementConfusionMatrixFileWriter.write("Case 3: Hate Over Offensive+Non-Offensive, , , , , , , , , , , , \n");
        lastLineInterAgreementConfusionMatrixFileWriter.write("Case 2: Hate+Offensive Over Non-Offensive, , , , , , , , , , , , \n");
        lastLineInterAgreementConfusionMatrixFileWriter.write("Case 3: Offensive Over Non-Offensive, , , , , , , , , , , , \n");
//        lastLineInterAgreementConfusionMatrixFileWriter.write("Case 6: Non-Offensive Over Hate+Offensive, , , , , , , , , , , , \n");

        lastLineInterAgreementConfusionMatrixFileWriter.write("TP True Positive, = %HT Lines in Class A , , , , , , , , , , , \n");
        lastLineInterAgreementConfusionMatrixFileWriter.write("TN True Negative, = %(#Lines - HT Lines) in Class B, , , , , , , , , , , \n");
        lastLineInterAgreementConfusionMatrixFileWriter.write("FP False Positive, = %HT Lines in Class B, , , , , , , , , , , \n");
        lastLineInterAgreementConfusionMatrixFileWriter.write("FN False Negative, = %(#Lines - HT Lines) in Class A, , , , , , , , , , , \n");

        lastLineInterAgreementConfusionMatrixFileWriter.write("Accuracy = TP + TN / ( TP + TN + FP + FN), , , , , , , , , , , , \n");
        lastLineInterAgreementConfusionMatrixFileWriter.write("Precision = TP / (TP + FP), , , , , , , , , , , , \n");
        lastLineInterAgreementConfusionMatrixFileWriter.write("Recall = TP / (TP + FN), , , , , , , , , , , , \n");
        lastLineInterAgreementConfusionMatrixFileWriter.write("F-Measure = 2(Pre*Rec)/(Pre+Rec), , , , , , , , , , , , \n");
        lastLineInterAgreementConfusionMatrixFileWriter.close();
    }

    public static void process(File DBFile, File HTFile) throws FileNotFoundException, ParseException, IOException{
        System.out.println("\nProcessing the DBFile and HTFile: " + "\n " + DBFile + ",\n " + HTFile);
        HashMap<Integer, HashMap<String, Integer>> termFrequencies = new HashMap<Integer, HashMap<String, Integer>>();
        ArrayList<String> rawKeywords = getStopWords(HTFile.getAbsolutePath());
        rawKeywords = unique(rawKeywords);
        rawKeywords = sortAscending(rawKeywords);

        String DBFileName = DBFile.getName().substring(0, DBFile.getName().lastIndexOf("."));
        String HTFileName = HTFile.getName().substring(0, HTFile.getName().lastIndexOf("."));
        String line = "";
        PorterStemmer s = new PorterStemmer();
        BufferedReader br = new BufferedReader(new FileReader(DBFile));
        line = br.readLine();
        ArrayList<String> hateTermsOfList = new ArrayList<>();

        while ((line = br.readLine()) != null) {
            String lineOriginal = line;
            String lineStructure = lineOriginal.replace("\"", "").replace(",", "");
            lineStructure = lineOriginal.replace("\t", ", ");
            int count = 0;
            for (int i = 0; i < lineStructure.length(); i++) {
                if (lineStructure.charAt(i) == ',') {
                    count++;
                }
            }
            if (count != 5) {
//                System.err.println("error in line: " + lineStructure); 
            }

            int counter = 0;
            String tokenString = "";
            line = line.replaceAll("\\W", " ");
            String[] tokens = line.split(" ");
            ArrayList<String> hateTermStemAdded = new ArrayList<>();
            ArrayList<String> hateTermAdded = new ArrayList<>();

            if (!line.equals("")) {
                total_lines = total_lines + 1;
                for (String hateTerm : rawKeywords) {
                    if (hateTerm.contains(" ")) {
                        if (!hateTermStemAdded.contains(s.stem(hateTerm.toLowerCase()))) {
                            if (line.toLowerCase().contains(hateTerm.toLowerCase()) || line.toLowerCase().contains(s.stem(hateTerm.toLowerCase()))) {
                                int occurence = countFreq(s.stem(hateTerm.toLowerCase()), line.toLowerCase());
                                for (int i = 0; i < occurence; i++) {
                                    counter = counter + 1;
                                    tokenString = tokenString + hateTerm + "; ";
                                    hateTermStemAdded.add(s.stem(hateTerm.toLowerCase()));
                                    hateTermAdded.add(hateTerm.toLowerCase());
                                    hateTermsOfList.add(hateTerm.toLowerCase());
                                }
                            }
                        }
                    } else if (!hateTermStemAdded.contains(s.stem(hateTerm.toLowerCase())) && line.toLowerCase().contains(hateTerm.toLowerCase())) {
                        for (int x = 0; x < tokens.length; x++) {
//                        if (tokens[x].toLowerCase().contains(hateTerm.toLowerCase()) && !hateTerm.matches("\\d+")) {
                            if (s.stem(tokens[x]).equalsIgnoreCase(s.stem(hateTerm))) {
                                counter = counter + 1;
                                tokenString = tokenString + hateTerm + "; ";
                                hateTermStemAdded.add(s.stem(hateTerm.toLowerCase()));
                                hateTermAdded.add(hateTerm.toLowerCase());
                                hateTermsOfList.add(hateTerm.toLowerCase());
                            }
                        }
                    }
                }

                if (counter == 0) {
                    if (countLinesIfMultipleTermOccurs.get(0) == null) {
                        countLinesIfMultipleTermOccurs.put(0, 1);
                    } else {
                        int i = countLinesIfMultipleTermOccurs.get(0);
                        countLinesIfMultipleTermOccurs.put(0, i + 1);
                    }
                    File transactionDBFile = new File(path + HTFileName + "\\" + DBFileName + "\\N(" + counter + ")_HTs.csv");
                    if (!transactionDBFile.exists()) {
                        BufferedWriter bufwriter = new BufferedWriter(new FileWriter(transactionDBFile, false));
                        bufwriter.write("HateTerm, count, hate_speech, offensive_language, neither, class, tweet\n");
                        bufwriter.write(tokenString.trim() + "--, " + lineStructure + "\n");
                        bufwriter.close();// closes the file
                    } else {
                        BufferedWriter bufwriter = new BufferedWriter(new FileWriter(transactionDBFile, true));
                        bufwriter.write(tokenString.trim() + "--, " + lineStructure + "\n");
                        bufwriter.close();// closes the file
                    }
                }

                if (counter != 0) {
                    File transactionDBFile = new File(path + HTFileName + "\\" + DBFileName + "\\N(" + counter + ")_HTs.csv");
                    if (!transactionDBFile.exists()) {
                        BufferedWriter bufwriter = new BufferedWriter(new FileWriter(transactionDBFile, false));
                        bufwriter.write("HateTerm, count, hate_speech, offensive_language, neither, class, tweet\n");
                        bufwriter.write(tokenString.substring(0, tokenString.length() - 2) + ", " + lineStructure + "\n");
                        bufwriter.close();// closes the file
                    } else {
                        BufferedWriter bufwriter = new BufferedWriter(new FileWriter(transactionDBFile, true));
                        bufwriter.write(tokenString.substring(0, tokenString.length() - 2) + ", " + lineStructure + "\n");
                        bufwriter.close();// closes the file
                    }
                    if (countLinesIfMultipleTermOccurs.get(counter) == null) {
                        countLinesIfMultipleTermOccurs.put(counter, 1);
                    } else {
                        int i = countLinesIfMultipleTermOccurs.get(counter);
                        countLinesIfMultipleTermOccurs.put(counter, i + 1);
                    }

                    hateTermAdded = unique(hateTermAdded);
                    for (String hateTerm : hateTermAdded) {
                        if (hateTerm.contains(" ")) {
                            if (!hateTermStemAdded.contains(s.stem(hateTerm.toLowerCase()))) {
                                if (line.toLowerCase().contains(hateTerm.toLowerCase()) || line.toLowerCase().contains(s.stem(hateTerm.toLowerCase()))) {
                                    int occurence = countFreq(s.stem(hateTerm.toLowerCase()), line.toLowerCase());
                                    for (int i = 0; i < occurence; i++) {
                                        if (termFrequencies.get(counter) == null) {
                                            HashMap<String, Integer> singleTermFrequency = new HashMap<>();
                                            singleTermFrequency.put(hateTerm, 1);
                                            termFrequencies.put(counter, singleTermFrequency);
                                        } else {
                                            HashMap<String, Integer> singeleTermFrequency = new HashMap<>();
                                            singeleTermFrequency = termFrequencies.get(counter);
                                            if (singeleTermFrequency.get(hateTerm) == null) {
                                                singeleTermFrequency.put(hateTerm, 1);
                                                termFrequencies.put(counter, singeleTermFrequency);
                                            } else {
                                                int x = singeleTermFrequency.get(hateTerm);
                                                singeleTermFrequency.put(hateTerm, x + 1);
                                                termFrequencies.put(counter, singeleTermFrequency);
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (line.toLowerCase().contains(hateTerm.toLowerCase())) {
                            for (int x = 0; x < tokens.length; x++) {
//                            if (tokens[x].toLowerCase().contains(hateTerm.toLowerCase()) && !hateTerm.matches("\\d+")) {
                                if (s.stem(tokens[x]).equalsIgnoreCase(s.stem(hateTerm))) {
                                    if (termFrequencies.get(counter) == null) {
                                        HashMap<String, Integer> singleTermFrequency = new HashMap<>();
                                        singleTermFrequency.put(hateTerm, 1);
                                        termFrequencies.put(counter, singleTermFrequency);
                                    } else {
                                        HashMap<String, Integer> singeleTermFrequency = new HashMap<>();
                                        singeleTermFrequency = termFrequencies.get(counter);
                                        if (singeleTermFrequency.get(hateTerm) == null) {
                                            singeleTermFrequency.put(hateTerm, 1);
                                            termFrequencies.put(counter, singeleTermFrequency);
                                        } else {
                                            int i = singeleTermFrequency.get(hateTerm);
                                            singeleTermFrequency.put(hateTerm, i + 1);
                                            termFrequencies.put(counter, singeleTermFrequency);
                                        }
                                    }
//  For the exact HateTerm Match
//                            } else if (tokens[x].equalsIgnoreCase(hateTerm)) {
//                                if (termFrequencies.get(counter) == null) {
//                                    HashMap<String, Integer> singleTermFrequency = new HashMap<>();
//                                    singleTermFrequency.put(hateTerm, 1);
//                                    termFrequencies.put(counter, singleTermFrequency);
//                                } else {
//                                    HashMap<String, Integer> singeleTermFrequency = new HashMap<>();
//                                    singeleTermFrequency = termFrequencies.get(counter);
//                                    if (singeleTermFrequency.get(hateTerm) == null) {
//                                        singeleTermFrequency.put(hateTerm, 1);
//                                        termFrequencies.put(counter, singeleTermFrequency);
//                                    } else {
//                                        int i = singeleTermFrequency.get(hateTerm);
//                                        singeleTermFrequency.put(hateTerm, i + 1);
//                                        termFrequencies.put(counter, singeleTermFrequency);
//                                    }
//                                }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Map.Entry<Integer, HashMap<String, Integer>> entry
                : termFrequencies.entrySet()) {
            Integer key = entry.getKey();
            HashMap<String, Integer> value = entry.getValue();
            File termFrequenciesFile = new File(path + HTFileName + "\\" + DBFileName + "\\N(" + key + ")_HTs" + ".csv");
            BufferedWriter bufwriter = new BufferedWriter(new FileWriter(termFrequenciesFile, true));
            bufwriter.write("----, ----, ----, ----, ----, ----, ----\n");
            bufwriter.write("----, ----, ----, ----, ----, ----, ----\n");
            bufwriter.write("HateTerm, Frequency, , , , , \n");
            value = (HashMap<String, Integer>) sortByValues(value);
            for (Map.Entry<String, Integer> entry1 : value.entrySet()) {
                String key1 = entry1.getKey();
                Integer value1 = entry1.getValue();
                int i;
                bufwriter.write(key1.toLowerCase() + ", " + value1 + ", , , , , \n");

                if (allTermFrequency.get(key1.toLowerCase()) == null) {
                    allTermFrequency.put(key1.toLowerCase(), value1);
                } else {
                    i = allTermFrequency.get(key1.toLowerCase());
                    allTermFrequency.put(key1.toLowerCase(), i + value1);
                }
            }
            bufwriter.close();// closes the file
        }

        hateTermsOfList = unique(hateTermsOfList);
        hateTermsOfList = sortAscending(hateTermsOfList);
        for (String hateTerm : hateTermsOfList) {
            BufferedReader br1 = new BufferedReader(new FileReader(DBFile));
            br1.readLine();
            while ((line = br1.readLine()) != null) {
//                System.out.println("rawKeywords.get(i).toString() " + hateTerm);
                line = line.replaceAll("\\W", " ");
                String[] tokens = line.split(" ");
                if (!line.equals("")) {
                    if (hateTerm.contains(" ")) {
                        if (line.toLowerCase().contains(hateTerm.toLowerCase()) || line.toLowerCase().contains(s.stem(hateTerm.toLowerCase()))) {
                            int occurence = countFreq(s.stem(hateTerm.toLowerCase()), line.toLowerCase());
                            for (int i = 0; i < occurence; i++) {
                                if (countLinesIfATermOccurs.get(hateTerm.toLowerCase()) == null) {
                                    countLinesIfATermOccurs.put(hateTerm.toLowerCase(), 1);
                                    break;
                                } else {
                                    int k = countLinesIfATermOccurs.get(hateTerm.toLowerCase());
                                    countLinesIfATermOccurs.put(hateTerm.toLowerCase(), k + 1);
                                    break;
                                }
                            }
                        }
                    } else if (line.toLowerCase().contains(hateTerm.toLowerCase())) {
                        for (int x = 0; x < tokens.length; x++) {
                            if (s.stem(tokens[x]).equalsIgnoreCase(s.stem(hateTerm))) {
                                if (countLinesIfATermOccurs.get(hateTerm.toLowerCase()) == null) {
                                    countLinesIfATermOccurs.put(hateTerm.toLowerCase(), 1);
                                    break;
                                } else {
                                    int k = countLinesIfATermOccurs.get(hateTerm.toLowerCase());
                                    countLinesIfATermOccurs.put(hateTerm.toLowerCase(), k + 1);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void makeAllTopHateTermFrequencies(String HTFileName, String DBFileName) throws IOException {
        allTermFrequency = allTermFrequency
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));

        File allFrequenciesFile = new File(path + HTFileName + "\\" + DBFileName + "\\AllHTsFrequencies.csv");
        System.out.println("Write All Frequency Hate Terms: " + path + HTFileName + "\\" + DBFileName + "\\AllHateTermFrequencies.csv");
        try (BufferedWriter frequencyWriterFlush = new BufferedWriter(new FileWriter(allFrequenciesFile, false))) {
			frequencyWriterFlush.write("");
		}
        BufferedWriter frequencyWriter = new BufferedWriter(new FileWriter(allFrequenciesFile, true));
        frequencyWriter.write("HateTerm, Frequency\n");
        for (Map.Entry<String, Integer> entry : allTermFrequency.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            frequencyWriter.write(key + ", " + value + "\n");
        }
        frequencyWriter.close();

        File topFrequenciesFile = new File(path + HTFileName + "\\" + DBFileName + "\\Top20HTsFrequency.csv");
        try (BufferedWriter topFrequencyWriterflush = new BufferedWriter(new FileWriter(topFrequenciesFile, false))) {
			topFrequencyWriterflush.write("");
		}
        BufferedWriter topFrequencyWriter = new BufferedWriter(new FileWriter(topFrequenciesFile, true));
        topFrequencyWriter.write("HateTerm, Frequency\n");
        System.out.println("Write Top Frequency Hate Terms");
        Set<String> keys = allTermFrequency.keySet();
        String[] keysArray = keys.toArray(new String[keys.size()]);
        for (int k = 0; k < keysArray.length && k < 20; k++) {
//                    System.out.println(keysArray[k] + ", " + allTermFrequency.get(keysArray[k]));
            topFrequencyWriter.write(keysArray[k] + ", " + allTermFrequency.get(keysArray[k]) + "\n");
        }
        topFrequencyWriter.close();
    }

    private static void makeSummaryHatePercentLineFiles(String HTFileName, String DBFileName) throws IOException {
        File countLinesIfAnyTermOccursFile = new File(path + "\\SummaryHateTermsHTs(N).csv");
        countLinesIfMultipleTermOccurs = extracted();
        for (Map.Entry<Integer, Integer> entry : countLinesIfMultipleTermOccurs.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            BufferedWriter writerSummary = new BufferedWriter(new FileWriter(countLinesIfAnyTermOccursFile, true));
            float percentageLines = (value.floatValue() / total_lines) * 100;
            DecimalFormat df = new DecimalFormat("#.###");
            writerSummary.write(DBFileName + ", " + HTFileName + ", " + key + ", " + value + ", " + total_lines + ", " + df.format(percentageLines) + "\n");
            writerSummary.close();// closes the file
        }

        File countLinesIfATermOccursFile = new File(path + HTFileName + "\\" + DBFileName + "\\AllHTsPercentLine.csv");
        BufferedWriter writerCountLinesIfATermOccurs = new BufferedWriter(new FileWriter(countLinesIfATermOccursFile, false));
        writerCountLinesIfATermOccurs.write("HateTerm, N(HateTermInLines), N(Lines), %(HateTermLines)\n");
        writerCountLinesIfATermOccurs.close();

        countLinesIfATermOccurs = (HashMap<String, Integer>) sortByValues(countLinesIfATermOccurs);
        for (Map.Entry<String, Integer> entry : countLinesIfATermOccurs.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            File countLinesFile = new File(path + HTFileName + "\\" + DBFileName + "\\AllHTsPercentLine.csv");
            BufferedWriter countLinesWriter = new BufferedWriter(new FileWriter(countLinesFile, true));
            float percentageLines = (value.floatValue() / total_lines) * 100;
            DecimalFormat df = new DecimalFormat("#.###");
            countLinesWriter.write(key + ", " + value + ", " + total_lines + ", " + df.format(percentageLines) + "\n");
            countLinesWriter.close();// closes the file
        }
    }

	private static HashMap extracted() {
		return sortByKeys(countLinesIfMultipleTermOccurs);
	}

    private static void makeOuterJoinHateTermFrequencies(File[] dbFiles, String HTFileName) throws IOException {
        System.out.println("Make List HateTermFrequencies with " + HTFileName);
        makeListHateTermFrequencies(dbFiles, HTFileName);

        outerJoinHTs_Single_HTList = unique(outerJoinHTs_Single_HTList);
        outerJoinHTs_Single_HTList = sortAscending(outerJoinHTs_Single_HTList);

        File dirHTFiles = new File(path + HTFileName);
        File outerJoinOfHateRuleFile = new File(dirHTFiles.getAbsolutePath() + "\\" + "OuterJoinHTsFrequencies.csv");
        System.out.println("OuterJoinTerms " + outerJoinOfHateRuleFile.getName());
        BufferedWriter writerOuterJoin = new BufferedWriter(new FileWriter(outerJoinOfHateRuleFile, false));
        writerOuterJoin.write("");
        writerOuterJoin.close();

        BufferedWriter writerOuterJoinFrequencies = new BufferedWriter(new FileWriter(outerJoinOfHateRuleFile, true));
        writerOuterJoinFrequencies.write("Hate Terms, ");
        for (int k = 0; k < dbFiles.length; k++) {
            String DBFileName = dbFiles[k].getName().substring(0, dbFiles[k].getName().lastIndexOf("."));
            if (k > 0) {
                writerOuterJoinFrequencies.write(", ");
            }
            writerOuterJoinFrequencies.write(DBFileName);
        }
        writerOuterJoinFrequencies.write("\n");
        writerOuterJoinFrequencies.close();

        BufferedWriter writer2 = new BufferedWriter(new FileWriter(outerJoinOfHateRuleFile, true));
        for (int k = 0; k < outerJoinHTs_Single_HTList.size(); k++) {
//                    System.out.println("OuterJoinList(" + k + ") " + outerJoinHTs_Single_HTList.get(k) + " HateRuleFile " + HTFileName);
            String outerJoinRow = makeOuterJoinFrequenciesFile(outerJoinHTs_Single_HTList.get(k), dbFiles, HTFileName);
            if (k > 0) {
                writer2.write("\n");
            }
            writer2.write(outerJoinRow);
        }
        writer2.close();
    }

    private static void makeListHateTermFrequencies(File[] dbFiles, String HTFileName) throws IOException {
        for (int k = 0; k < dbFiles.length; k++) {
            String DBFileName = dbFiles[k].getName().substring(0, dbFiles[k].getName().lastIndexOf("."));
            try (BufferedReader br = new BufferedReader(new FileReader(path + HTFileName + "\\" + DBFileName + "\\AllHTsFrequencies.csv"))) {
				String line = null;
				br.readLine();
				while ((line = br.readLine()) != null) {
				    int indexOfComma = line.indexOf(", ");
				    String term = line.substring(0, indexOfComma);
				    outerJoinHTs_Single_HTList.add(term);
				}
			}
        }
    }

    private static String makeOuterJoinFrequenciesFile(String getOuterJoinTerm, File[] dbFiles, String HTFileName) throws IOException {
        String outerJoinString = getOuterJoinTerm + ", ";
        for (int k = 0; k < dbFiles.length; k++) {
            String DBFileName = dbFiles[k].getName().substring(0, dbFiles[k].getName().lastIndexOf("."));
            try (BufferedReader br = new BufferedReader(new FileReader(path + HTFileName + "\\" + DBFileName + "\\AllHTsFrequencies.csv"))) {
				String line = null;

				boolean flagRuleTrue = false;
				String frequency = "";
				while ((line = br.readLine()) != null) {
				    int indexOfComma = line.indexOf(", ");
				    String term = line.substring(0, indexOfComma);
				    frequency = line.substring(indexOfComma + 2);

				    if (getOuterJoinTerm.equals(term)) {
//                                System.out.println("OuterJoinAntecedent: " + outerJoinAntecedent.get(j) + " tmpAntecedent " + tmpAntecedent.get(i));
				        flagRuleTrue = true;
				        break;
				    }
//                    System.out.println("FlageAntecedent: " + flagAntecedant + ", FlageConsequent: " + flagConsequent + ", FlageRuleTrue: " + flagRuleTrue);
				}

				if (flagRuleTrue) {
				    if (k > 0) {
				        outerJoinString = outerJoinString + ", ";
				    }
				    outerJoinString = outerJoinString + frequency;
				} else {
				    if (k > 0) {
				        outerJoinString = outerJoinString + ", ";
				    }
				    outerJoinString = outerJoinString + "--";
				}
			}
        }
//        System.out.println(outerJoinString);
        return outerJoinString;
    }

    private static void makeOuterJoinHateTermPercentLines(File[] dbFiles, String HTFileName) throws IOException {
        System.out.println("Make List HateTermPercentLines with " + HTFileName);
        makeListHateTermPercentLine(dbFiles, HTFileName);

        outerJoinHTs_Single_HTList = unique(outerJoinHTs_Single_HTList);
        outerJoinHTs_Single_HTList = sortAscending(outerJoinHTs_Single_HTList);
        File dirDBs = new File(path + HTFileName);
        File outerJoinOfHateTermFile = new File(dirDBs.getAbsolutePath() + "\\" + "OuterJoinHTsPercentLines.csv");
        System.out.println("OuterJoinTerms " + outerJoinOfHateTermFile.getName());
        BufferedWriter writerOuterJoin = new BufferedWriter(new FileWriter(outerJoinOfHateTermFile, false));
        writerOuterJoin.write("");
        writerOuterJoin.close();

        BufferedWriter writerOuterJoinFrequencies = new BufferedWriter(new FileWriter(outerJoinOfHateTermFile, true));
        writerOuterJoinFrequencies.write("Hate Terms, ");
        for (int k = 0; k < dbFiles.length; k++) {
            String DBFileName = dbFiles[k].getName().substring(0, dbFiles[k].getName().lastIndexOf("."));
            if (k > 0) {
                writerOuterJoinFrequencies.write(", ");
            }
            writerOuterJoinFrequencies.write(DBFileName);
        }
        writerOuterJoinFrequencies.write("\n");
        writerOuterJoinFrequencies.close();

        BufferedWriter writer2 = new BufferedWriter(new FileWriter(outerJoinOfHateTermFile, true));
        for (int k = 0; k < outerJoinHTs_Single_HTList.size(); k++) {
//                    System.out.println("OuterJoinList(" + k + ") " + outerJoinHTs_Single_HTList.get(k) + " HateTermFile " + HTFileName);
            String outerJoinRow = makeOuterJoinPercentLineFile(outerJoinHTs_Single_HTList.get(k), dbFiles, HTFileName);
            if (k > 0) {
                writer2.write("\n");
            }
            writer2.write(outerJoinRow);
        }
        writer2.close();
    }

    private static void makeListHateTermPercentLine(File[] dbFiles, String HTFileName) throws FileNotFoundException, IOException {
        for (int k = 0; k < dbFiles.length; k++) {
            String DBFileName = dbFiles[k].getName().substring(0, dbFiles[k].getName().lastIndexOf("."));
            try (BufferedReader br = new BufferedReader(new FileReader(path + HTFileName + "\\" + DBFileName + "\\AllHTsPercentLine.csv"))) {
				String line = null;
				br.readLine();
				while ((line = br.readLine()) != null) {
				    int indexOfComma = line.indexOf(", ");
				    String term = line.substring(0, indexOfComma);
				    outerJoinHTs_Single_HTList.add(term);
				}
			}
        }
    }

    private static String makeOuterJoinPercentLineFile(String getOuterJoinTerm, File[] dbFiles, String HTFileName) throws FileNotFoundException, IOException {
        String outerJoinString = getOuterJoinTerm + ", ";
        for (int k = 0; k < dbFiles.length; k++) {
            String DBFileName = dbFiles[k].getName().substring(0, dbFiles[k].getName().lastIndexOf("."));
            try (BufferedReader br = new BufferedReader(new FileReader(path + HTFileName + "\\" + DBFileName + "\\AllHTsPercentLine.csv"))) {
				String line = null;

				boolean flagRuleTrue = false;
				String frequency = "";
				while ((line = br.readLine()) != null) {
				    int indexOfComma = line.indexOf(", ");
				    int indexLastComma = line.lastIndexOf(", ");
				    String term = line.substring(0, indexOfComma);
				    frequency = line.substring(indexLastComma + 2, line.length());

				    if (getOuterJoinTerm.equalsIgnoreCase(term)) {
//                                System.out.println("OuterJoinAntecedent: " + outerJoinAntecedent.get(j) + " tmpAntecedent " + tmpAntecedent.get(i));
				        flagRuleTrue = true;
				        break;
				    }
//                    System.out.println("FlageAntecedent: " + flagAntecedant + ", FlageConsequent: " + flagConsequent + ", FlageRuleTrue: " + flagRuleTrue);
				}

				if (flagRuleTrue) {
				    if (k > 0) {
				        outerJoinString = outerJoinString + ", ";
				    }
				    outerJoinString = outerJoinString + frequency;
				} else {
				    if (k > 0) {
				        outerJoinString = outerJoinString + ", ";
				    }
				    outerJoinString = outerJoinString + "--";
				}
			}
        }
//        System.out.println(outerJoinString);
        return outerJoinString;
    }

    private static void sumHateLines(String DBFileName) throws IOException {
        System.out.println("Make Sum HateLine with " + DBFileName);
        int oldValue;

        for (Map.Entry<Integer, Integer> entry : countLinesIfMultipleTermOccurs.entrySet()) {
            if (entry.getKey() == 0) {
//                System.out.println("value " + entry.getKey() + ", " + entry.getValue());
            } else {
//                System.out.println("value " + entry.getKey() + ", " + entry.getValue());
                Integer value = entry.getValue();
                if (sumMultipleTermOccurs.containsKey(DBFileName)) {
                    oldValue = sumMultipleTermOccurs.get(DBFileName);
                    sumMultipleTermOccurs.put(DBFileName, value + oldValue);
                } else {
                    sumMultipleTermOccurs.put(DBFileName, value);//NEW to write}  
                }
            }
        }
        if (!sumMultipleTermOccurs.containsKey(DBFileName)) {
            sumMultipleTermOccurs.put(DBFileName, 0);
        }
    }

    private static void summaryConfusionMatrix4Cases(String HTFileName) throws IOException {
        File ConfusionMatrixFile = new File(path + "\\ConfusionMatrix.csv");

        // Wrong place to write, do not reset for each key
        onlyHate = 0;
        onlyOffensive = 0;
        onlyNonOffensive = 0;
        hateOffensive = 0;
        offensiveNonOffensive = 0;
        hateNonOffensive = 0;

        // Wrong place to write, do not reset for each key
        for (Map.Entry<String, Integer> entry : sumMultipleTermOccurs.entrySet()) {
            String DBName = entry.getKey();
            int value = entry.getValue();
            float percent = 0;

//            System.out.println("key " + tokens[0]);
            if (containsIgnoreCase(DBName, "0")) {
                onlyHate = onlyHate + value;
                percent = (float) (onlyHate / (float) totalHate);
                System.out.println("onlyHate: " + onlyHate);
            } else {
                offensiveNonOffensive = offensiveNonOffensive + value;
                percent = (float) (offensiveNonOffensive / (float) totalOffensiveNonOffensive);
                System.out.println("offensiveNonOffensive: " + offensiveNonOffensive);
            }

            if (containsIgnoreCase(DBName, "1")) {
                onlyOffensive = onlyOffensive + value;
                percent = (float) (onlyOffensive / (float) totalOffensive);
                System.out.println("onlyOffensive: " + onlyOffensive);
            } else {
                hateNonOffensive = hateNonOffensive + value;
                percent = (float) (hateNonOffensive / (float) totalHateNonOffensive);
                System.out.println("hateNonOffensive: " + hateNonOffensive);
            }

            if (containsIgnoreCase(DBName, "2")) {
                onlyNonOffensive = onlyNonOffensive + value;
                percent = (float) (onlyNonOffensive / (float) totalNonOffensive);
                System.out.println("onlyNonOffensive: " + onlyNonOffensive);
            } else {
                hateOffensive = hateOffensive + value;
                percent = (float) (hateOffensive / (float) totalHateOffensive);
                System.out.println("hateNonOffensive: " + hateOffensive);
            }
        }

        DecimalFormat df = new DecimalFormat("#.###");

        BufferedWriter writerConfusionMatrix = new BufferedWriter(new FileWriter(ConfusionMatrixFile, true));

        sumMultipleTermOccurs = sortByKeys(sumMultipleTermOccurs);
        for (Map.Entry<String, Integer> entry : sumMultipleTermOccurs.entrySet()) {
            String DBName = entry.getKey();
            int value = entry.getValue();
            float percent = 0;

//            System.out.println("key " + tokens[0]);
            if (containsIgnoreCase(DBName, "0")) {
                float recallOnlyHate = (float) (onlyHate / (float) totalHate);
                float precisionOnlyHate = (float) (onlyHate / (float) (onlyHate + offensiveNonOffensive));

                float recallHateOffensive = (float) (hateOffensive / (float) totalHateOffensive);
                float precisionHateOffensive = (float) (hateOffensive / (float) (hateOffensive + onlyNonOffensive));

                percent = (float) (onlyHate / (float) totalHate);
                int onlyHateTN = totalOffensiveNonOffensive - onlyOffensive - onlyNonOffensive;
                int onlyHateFN = (totalHate - onlyHate);
                float accuracyOnlyHate = (float) (onlyHate + onlyHateTN) / (float) (onlyHate + onlyHateTN + offensiveNonOffensive + onlyHateFN);
                float fMeasureOnlyHate = (float) (2 * ((precisionOnlyHate * recallOnlyHate)) / (float) (precisionOnlyHate + recallOnlyHate));
                writerConfusionMatrix.write(HTFileName + ", " + DBName + ", " + value + ", " + totalHate + ", " + df.format(percent) + ", " + onlyHate + ", " + onlyHateTN + ", " + offensiveNonOffensive + ", " + onlyHateFN + ", " + df.format(accuracyOnlyHate) + ", " + df.format(recallOnlyHate) + ", " + df.format(precisionOnlyHate) + ", " + df.format(fMeasureOnlyHate) + "\n");

                percent = (float) (hateOffensive / (float) totalHateOffensive);
                int hateOffensiveTN = totalNonOffensive - onlyNonOffensive;
                int hateOffensiveFN = (totalHateOffensive - hateOffensive);
                float accuracyHateOffensive = (float) (onlyHate + hateOffensiveTN) / (float) (onlyHate + hateOffensiveTN + offensiveNonOffensive + hateOffensiveFN);
                float fMeasureHateOffensive = (float) 2 * ((precisionHateOffensive * recallHateOffensive) / (float) (precisionHateOffensive + recallHateOffensive));
                writerConfusionMatrix.write(HTFileName + ", " + DBName + "+1Offensive, " + hateOffensive + ", " + totalHateOffensive + ", " + df.format(percent) + ", " + hateOffensive + ", " + hateOffensiveTN + ", " + onlyNonOffensive + ", " + hateOffensiveFN + ", " + df.format(accuracyHateOffensive) + ", " + df.format(recallHateOffensive) + ", " + df.format(precisionHateOffensive) + ", " + df.format(fMeasureHateOffensive) + "\n");
            }

            if (containsIgnoreCase(DBName, "1")) {
                float recallOnlyOffensive = (float) (onlyOffensive / (float) totalOffensive);
                float precisionOnlyOffensive = (float) (onlyOffensive / (float) (onlyOffensive + hateNonOffensive));

                percent = (float) (onlyOffensive / (float) totalOffensive);

                int onlyOffensiveTN = totalHateNonOffensive - onlyHate - onlyNonOffensive;
                int onlyOffensiveFN = (totalOffensive - onlyOffensive);
                float accuracyOnlyOffensive = (float) (onlyHate + onlyOffensiveTN) / (float) (onlyHate + onlyOffensiveTN + offensiveNonOffensive + onlyOffensiveFN);
                float fMeasureOnlyOffensive = (float) (2 * ((precisionOnlyOffensive * recallOnlyOffensive)) / (float) (precisionOnlyOffensive + recallOnlyOffensive));
                writerConfusionMatrix.write(HTFileName + ", " + DBName + ", " + value + ", " + totalOffensive + ", " + df.format(percent) + ", " + onlyOffensive + ", " + onlyOffensiveTN + ", " + hateNonOffensive + ", " + onlyOffensiveFN + ", " + df.format(accuracyOnlyOffensive) + ", " + df.format(recallOnlyOffensive) + ", " + df.format(precisionOnlyOffensive) + ", " + df.format(fMeasureOnlyOffensive) + "\n");
            }

            if (containsIgnoreCase(DBName, "2")) {
                float recallOnlyNonOffensive = (float) (onlyNonOffensive / (float) totalNonOffensive);
                float precisionOnlyNonOffensive = (float) (onlyNonOffensive / (float) (onlyNonOffensive + hateOffensive));

                percent = (float) (onlyNonOffensive / (float) totalNonOffensive);

                int onlyNonOffensiveTN = totalHateOffensive - onlyHate - onlyOffensive;
                int onlyNonOffensiveFN = (totalNonOffensive - onlyNonOffensive);
                float accuracyOnlyNonOffensive = (float) (onlyHate + onlyNonOffensiveTN) / (float) (onlyHate + onlyNonOffensiveTN + offensiveNonOffensive + onlyNonOffensiveFN);
                float fMeasureOnlyNonOffensive = (float) (2 * ((precisionOnlyNonOffensive * recallOnlyNonOffensive)) / (float) (precisionOnlyNonOffensive + recallOnlyNonOffensive));
                writerConfusionMatrix.write(HTFileName + ", " + DBName + ", " + value + ", " + totalNonOffensive + ", " + df.format(percent) + ", " + onlyNonOffensive + ", " + onlyNonOffensiveTN + ", " + hateOffensive + ", " + onlyNonOffensiveFN + ", " + df.format(accuracyOnlyNonOffensive) + ", " + df.format(recallOnlyNonOffensive) + ", " + df.format(precisionOnlyNonOffensive) + ", " + df.format(fMeasureOnlyNonOffensive) + "\n");
            }

            System.out.println(HTFileName + ", -- " + ", -- " + ", -- " + ", -- " + ", " + ", " + ", " + ", " + "\n");
        }
        writerConfusionMatrix.write("--, --, --, --, --, --, --, --, --, --, --, --, -- \n");
        writerConfusionMatrix.write("--, --, --, --, --, --, --, --, --, --, --, --, -- \n");

        writerConfusionMatrix.close();
    }

    private static void sumHateTermLines(String DBFileName) throws IOException {
        System.out.println("Make sum of HateTermLines with " + DBFileName);
        countLinesIfATermOccurs = sortByKeys(countLinesIfATermOccurs);
//        System.out.println("size: " + countLinesIfATermOccurs.size());
        for (Map.Entry<String, Integer> entry : countLinesIfATermOccurs.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            sumATermOccurs.put(key + ", " + DBFileName, value);//NEW to write}
        }
    }

    private static void summaryTermConfusionMatrix4Cases(String HTFileName) throws IOException {
        System.out.println("Make Summary ConfusionMatrix for Term  " + HTFileName);
        File TermConfusionMatrixFile = new File(path + "\\" + HTFileName + "\\TermConfusionMatrix.csv");
        String[] tokensOuter = null;

//        System.out.println("size: " + sumATermOccurs.size());
        ArrayList<String> termList = new ArrayList<String>();

        sumATermOccurs = sortByKeys(sumATermOccurs);
        for (Map.Entry<String, Integer> entry1 : sumATermOccurs.entrySet()) {
            String keyOuter = entry1.getKey();
            int valueOuter = entry1.getValue();
            tokensOuter = keyOuter.split(", ");

            onlyHateTerms = 0;
            hateOffensiveTerms = 0;
            offensiveNonOffensiveTerms = 0;
            nonOffensiveTerms = 0;

            if (!termList.contains(tokensOuter[0].toLowerCase())) {
                termList.add(tokensOuter[0].toLowerCase());
                boolean flag = false;

                for (Map.Entry<String, Integer> entry2 : sumATermOccurs.entrySet()) {
                    String keyInner = entry2.getKey();
                    int valueInner = entry2.getValue();
                    String[] tokensInner = keyInner.split(", ");

                    if (tokensOuter[0].equalsIgnoreCase(tokensInner[0])) {
                        flag = true;
//                        System.out.println("key: " + keyOuter + ", value: " + valueOuter);

//            System.out.println("File " + tokens[0]);
                        if (containsIgnoreCase(tokensInner[1], "0")) {
                            onlyHateTerms = onlyHateTerms + valueInner;
//                System.out.println("onlyHate: " + onlyHate);
                        } else {
                            offensiveNonOffensiveTerms = offensiveNonOffensiveTerms + valueInner;
//                System.out.println("offensiveNonOffensive: " + offensiveNonOffensive);
                        }

                        if (containsIgnoreCase(tokensInner[1], "0") || containsIgnoreCase(tokensInner[1], "1")) {
                            hateOffensiveTerms = hateOffensiveTerms + valueInner;
//                System.out.println("hateOffensive " + hateOffensive);
                        } else {
                            nonOffensiveTerms = nonOffensiveTerms + valueInner;
//                System.out.println("onlyNonOffensive: " + onlyNonOffensive);
                        }
                    }
                }
                if (flag) {
//                System.out.println(thirdFile + ", " + tokens[0]);
                    float recallOnlyHate, recallHateOffensive, precisionHateOffensive, precisionOnlyHate;
                    BufferedWriter writerConfusionMatrix = new BufferedWriter(new FileWriter(TermConfusionMatrixFile, true));
                    if (onlyHateTerms + offensiveNonOffensiveTerms != 0) {
                        precisionOnlyHate = (float) (onlyHateTerms / (float) (onlyHateTerms + offensiveNonOffensiveTerms));
                    } else {
                        precisionOnlyHate = -1;
                    }
                    if (hateOffensiveTerms + nonOffensiveTerms != 0) {
                        precisionHateOffensive = (float) (hateOffensiveTerms / (float) (hateOffensiveTerms + nonOffensiveTerms));
                    } else {
                        precisionHateOffensive = -1;
                    }

                    if (totalHate != 0) {
                        recallOnlyHate = (float) (onlyHateTerms / (float) totalHate);
                    } else {
                        totalHate = -1;
                        recallOnlyHate = 0;
                    }
                    if (totalHateOffensive != 0) {
                        recallHateOffensive = (float) (hateOffensiveTerms / (float) totalHateOffensive);
                    } else {
                        totalHateOffensive = -1;
                        recallHateOffensive = 0;
                    }

                    DecimalFormat df = new DecimalFormat("#.###");
                    String out = tokensOuter[0] + ", " + onlyHateTerms + ", " + offensiveNonOffensiveTerms + ", " + totalHate + ", " + df.format(recallOnlyHate) + ", " + df.format(precisionOnlyHate) + ", " + hateOffensiveTerms + ", " + nonOffensiveTerms + ", " + totalHateOffensive + ", " + df.format(recallHateOffensive) + ", " + df.format(precisionHateOffensive) + "\n";
                    out = out.replaceAll("-1", "--");
                    writerConfusionMatrix.write(out);
                    writerConfusionMatrix.close();// closes the file
//            System.out.println(tokens[0] + ", " + df.format(precisionOnlyHate) + ", " + df.format(precisionHateOffensive));
                }
            }
        }
    }

    private static void interAgreementTerms(File[] HTFiles) throws IOException {
        makeListHateTermPrecisionRecall(HTFiles);

        interHTs_Multi_HTList_FScore = unique(interHTs_Multi_HTList_FScore);
        interHTs_Multi_HTList_FScore = sortAscending(interHTs_Multi_HTList_FScore);
//        System.out.println(outerJoinHTs_Single_HTList.toString());

        File interAgreementTermsFile = new File(path + "\\" + "InterAgreementTerms.csv");
        System.out.println("OuterJoinTerms " + interAgreementTermsFile.getName());
        BufferedWriter writerOuterJoin = new BufferedWriter(new FileWriter(interAgreementTermsFile, false));
        writerOuterJoin.write("");
        writerOuterJoin.close();

        BufferedWriter writerOuterJoinFrequencies = new BufferedWriter(new FileWriter(interAgreementTermsFile, true));
        writerOuterJoinFrequencies.write("Hate Terms (HTs), Recall(Hate), Precision(Hate), FMeasure(Hate), Recall(Hate+Offensive), Precision(Hate+Offensive), FMeasure(Hate+Offensive), HateListNames");
        writerOuterJoinFrequencies.write("\n");
        writerOuterJoinFrequencies.close();

        BufferedWriter writer2 = new BufferedWriter(new FileWriter(interAgreementTermsFile, true));
        for (int k = 0; k < interHTs_Multi_HTList_FScore.size(); k++) {
//                    System.out.println("OuterJoinList(" + k + ") " + outerJoinHTs_Single_HTList.get(k) + " HateTermFile " + HTFileName);
            String outerJoinRow = makeOuterJoinPrecisionRecallFile(interHTs_Multi_HTList_FScore.get(k), HTFiles);
//            System.out.println("outerJoin: " + interHTs_Multi_HTList_FScore.get(k));
            if (k > 0) {
                writer2.write("\n");
            }
            writer2.write(outerJoinRow);
        }
        writer2.close();
    }

    private static void makeListHateTermPrecisionRecall(File[] HTFiles) throws FileNotFoundException, IOException {
        HashMap<String, String> termHTFileNameList = new HashMap<String, String>();
        for (int k = 0; k < HTFiles.length; k++) {
            String HTFileName = HTFiles[k].getName().substring(0, HTFiles[k].getName().lastIndexOf("."));
            try (BufferedReader br = new BufferedReader(new FileReader(path + HTFileName + "\\IntraAgreementTermMatrix.csv"))) {
				String line = null;
				br.readLine();

				while ((line = br.readLine()) != null) {
				    int indexOfComma = line.indexOf(", ");
				    String term = line.substring(0, indexOfComma);
//                System.out.println("True: " + interHTs_Multi_HTList_FScore.isEmpty());

				    if (!termHTFileNameList.isEmpty()) {
				        for (int i = 0; i < termHTFileNameList.size(); i++) {
				            if (!termHTFileNameList.containsKey(term.toLowerCase())) {
				                termHTFileNameList.put(term.toLowerCase(), HTFileName);
//                            System.out.println("Second Entry: " + term + ", " + HTFileName);
				            } else {
				                String value = termHTFileNameList.get(term.toLowerCase());
				                String termHTFileName = value + "; " + HTFileName;
				                if (!value.contains(HTFileName)) {
				                    termHTFileNameList.put(term.toLowerCase(), termHTFileName);
//                                System.out.println("Third Entry: " + term + ", " + termHTFileName);
				                }
				            }
				        }
				    } else {
//                    System.out.println("First Entry: " + term.toLowerCase() + ", " + HTFileName);
				        termHTFileNameList.put(term.toLowerCase(), HTFileName);
				    }
				}
			}
        }
        for (Map.Entry<String, String> entry : termHTFileNameList.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String termHTFileName = key + ", " + value;
            interHTs_Multi_HTList_FScore.add(termHTFileName);
        }
    }

    private static String makeOuterJoinPrecisionRecallFile(String getOuterJoinTermHTFileName, File[] HTFiles) throws FileNotFoundException, IOException {
        String outerJoinString = "";

        ArrayList<String> termList = new ArrayList<String>();
//        System.out.println("Key: " + getOuterJoinTermHTFileName);
        for (int k = 0; k < HTFiles.length; k++) {
            String HTFileName = HTFiles[k].getName().substring(0, HTFiles[k].getName().lastIndexOf("."));
            try (BufferedReader br = new BufferedReader(new FileReader(path + HTFileName + "\\IntraAgreementTermMatrix.csv"))) {
				String line = null;
				boolean flagRuleTrue = false;
				line = br.readLine();

				while ((line = br.readLine()) != null) {
				    String preRecFMeasureInfo = "";
				    int indexOfComma = line.indexOf(", ");
				    String preRecTokens[] = line.split(", ");
				    String term = line.substring(0, indexOfComma);
				    DecimalFormat df = new DecimalFormat("#.###");

				    if (!termList.contains(term.toLowerCase())) {
				        termList.add(term.toLowerCase());

				        float recallOnlyHate = 0, precisionOnlyHate = 0, recallHateOffensive = 0, precisionHateOffensive = 0, fMeasureOnlyHate, fMeasureHateOffensive;
				        String fMeasureOnlyHateString, fMeasureHateOffensiveString;
				        if (!preRecTokens[4].contains("--")) {
//                        System.out.println(preRecTokens[4]);
				            recallOnlyHate = (float) Double.parseDouble(preRecTokens[4].trim());
				        }
				        if (!preRecTokens[5].contains("--")) {
				            precisionOnlyHate = Float.parseFloat(preRecTokens[5]);
				        }
				        if (!preRecTokens[9].contains("--")) {
				            recallHateOffensive = Float.parseFloat(preRecTokens[9]);
				        }
				        if (!preRecTokens[10].contains("--")) {
				            precisionHateOffensive = Float.parseFloat(preRecTokens[10]);
				        }
				        if (!preRecTokens[4].contains("--") && !preRecTokens[5].contains("--")) {
				            fMeasureOnlyHate = (float) (2 * (recallOnlyHate * precisionOnlyHate)) / (float) (recallOnlyHate + precisionOnlyHate);
				            fMeasureOnlyHateString = df.format(fMeasureOnlyHate);
				        } else {
				            fMeasureOnlyHateString = " --";
				        }
				        if (!preRecTokens[9].contains("--") && !preRecTokens[10].contains("--")) {
				            fMeasureHateOffensive = (float) (2 * ((recallHateOffensive * precisionHateOffensive)) / (float) (recallHateOffensive + precisionHateOffensive));
				            fMeasureHateOffensiveString = df.format(fMeasureHateOffensive);
				        } else {
				            fMeasureHateOffensiveString = " --";
				        }
				        preRecFMeasureInfo = preRecTokens[4] + ", " + preRecTokens[5] + ", " + fMeasureOnlyHateString + ", " + preRecTokens[9] + ", " + preRecTokens[10] + ", " + fMeasureHateOffensiveString;

				        String tokens[] = getOuterJoinTermHTFileName.split(", ");
				        if (tokens[0].equalsIgnoreCase(term)) {
//                    System.out.println("OuterJoin: " + term);
				            flagRuleTrue = true;
				            outerJoinString = tokens[0] + ", " + preRecFMeasureInfo + ", " + tokens[1];
				            break;
				        }
				    }
//                    System.out.println("FlageAntecedent: " + flagAntecedant + ", FlageConsequent: " + flagConsequent + ", FlageRuleTrue: " + flagRuleTrue);
				}

				if (flagRuleTrue) {
				    break;
//                outerJoinString = outerJoinString;
//                System.out.println("out " + outerJoinString + ", " + preRecInfo);
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
//        System.out.println(outerJoinString);
        return outerJoinString;
    }

    private static void totalDatasetLines(File[] dbFiles) throws FileNotFoundException, IOException {
        for (int i = 0; i < dbFiles.length; i++) {
            String DBFileName = dbFiles[i].getName().substring(0, dbFiles[i].getName().lastIndexOf("."));
            String line = "";
            try (BufferedReader br2 = new BufferedReader(new FileReader(dbFiles[i]))) {
				int total_lines = 0;

				while ((line = br2.readLine()) != null) {
				    line = line.replaceAll("\\W", " ");
				    if (!line.equals("")) {
				        total_lines = total_lines + 1;
				    }
				}
				if (DBFileName.contains("0")) {
				    totalHate = total_lines;
				}
				if (DBFileName.contains("1")) {
				    totalOffensive = total_lines;
				}
				if (DBFileName.contains("2")) {
				    totalNonOffensive = total_lines;
				}
			}
        }

        totalHateOffensive = totalHate + totalOffensive;
        totalOffensiveNonOffensive = totalOffensive + totalNonOffensive;
        totalHateNonOffensive = totalHate + totalNonOffensive;
    }

    private static void interAgreementConfusionMatrix(String HTFileName) throws IOException {
        File ConfusionMatrixFile = new File(path + "\\InterAgreementConfusionMatrix.csv");

        // Wrong place to write, do not reset for each key
        onlyHate = 0;
        onlyOffensive = 0;
        onlyNonOffensive = 0;
        hateOffensive = 0;
        offensiveNonOffensive = 0;
        hateNonOffensive = 0;

        // Wrong place to write, do not reset for each key
        for (Map.Entry<String, Integer> entry : sumMultipleTermOccurs.entrySet()) {
            String DBName = entry.getKey();
            int value = entry.getValue();
            float percent = 0;

//            System.out.println("key " + tokens[0]);
            if (containsIgnoreCase(DBName, "0")) {
                onlyHate = onlyHate + value;
                onlyHatePercent = (float) (onlyHate / (float) totalHate);
                System.out.println("onlyHate: " + onlyHate);
            } else {
                offensiveNonOffensive = offensiveNonOffensive + value;
                offensiveNonOffensivePercent = (float) (offensiveNonOffensive / (float) totalOffensiveNonOffensive);
                System.out.println("offensiveNonOffensive: " + offensiveNonOffensive);
            }

            if (containsIgnoreCase(DBName, "1")) {
                onlyOffensive = onlyOffensive + value;
                onlyOffensivePercent = (float) (onlyOffensive / (float) totalOffensive);
                System.out.println("onlyOffensive: " + onlyOffensive);
            } else {
                hateNonOffensive = hateNonOffensive + value;
                hateNonOffensivePercent = (float) (hateNonOffensive / (float) totalHateNonOffensive);
                System.out.println("hateNonOffensive: " + hateNonOffensive);
            }

            if (containsIgnoreCase(DBName, "2")) {
                onlyNonOffensive = onlyNonOffensive + value;
                onlyNonOffensivePercent = (float) (onlyNonOffensive / (float) totalNonOffensive);
                System.out.println("onlyNonOffensive: " + onlyNonOffensive);
            } else {
                hateOffensive = hateOffensive + value;
                hateOffensivePercent = (float) (hateOffensive / (float) totalHateOffensive);
                System.out.println("hateNonOffensive: " + hateOffensive);
            }
        }

        DecimalFormat df = new DecimalFormat("#.###");

        BufferedWriter writerConfusionMatrix = new BufferedWriter(new FileWriter(ConfusionMatrixFile, true));

        sumMultipleTermOccurs = sortByKeys(sumMultipleTermOccurs);
        for (Map.Entry<String, Integer> entry : sumMultipleTermOccurs.entrySet()) {
            String DBName = entry.getKey();
            int value = entry.getValue();
            float percent = 0;

//            System.out.println("key " + tokens[0]);
            if (containsIgnoreCase(DBName, "0")) {
                //Case1
                float recallOnlyHate = (float) (onlyHate / (float) totalHate);
                float precisionOnlyHate = (float) (onlyHatePercent / (float) (onlyHatePercent + onlyNonOffensivePercent));

                percent = (float) (onlyHate / (float) totalHate);
                float onlyHateTN = (totalNonOffensive - onlyNonOffensive) / (float) totalNonOffensive;
                float onlyHateFN = (totalHate - onlyHate) / (float) totalHate;

                float accuracyOnlyHate = (float) (onlyHatePercent + onlyHateTN) / (float) (onlyHatePercent + onlyHateTN + onlyNonOffensivePercent + onlyHateFN);
                float fMeasureOnlyHate = (float) (2 * ((precisionOnlyHate * recallOnlyHate)) / (float) (precisionOnlyHate + recallOnlyHate));
//                writerConfusionMatrix.write(HTFileName + "_" + HTLines.get(HTFileName) + ", " + DBName + ", " + value + ", " + totalHate + ", " + df.format(percent) + ", " + onlyHate + ", " + onlyHateTN + ", " + onlyNonOffensive + ", " + onlyHateFN + ", " + df.format(accuracyOnlyHate) + ", " + df.format(recallOnlyHate) + ", " + df.format(precisionOnlyHate) + ", " + df.format(fMeasureOnlyHate) + "\n");
                writerConfusionMatrix.write(HTFileName + "_" + HTLines.get(HTFileName) + ", " + DBName + " Over No-Hate" + ", " + value + ", " + totalHate + ", " + df.format(percent) + ", " + df.format(percent) + ", " + df.format(onlyHateTN) + ", " + df.format(onlyNonOffensive / (float) totalNonOffensive) + ", " + df.format(onlyHateFN) + ", " + df.format(accuracyOnlyHate) + ", " + df.format(recallOnlyHate) + ", " + df.format(precisionOnlyHate) + ", " + df.format(fMeasureOnlyHate) + "\n");

//                //Case 2
//                float recallHateOverOffensive = (float) (onlyHate / (float) totalHate);
//                float precisionHateOverOffensive = (float) (onlyHatePercent / (float) (onlyHatePercent + onlyOffensivePercent));
//
//                percent = (float) (onlyHate / (float) totalHate);
//                float hateOverOffensiveTN = (totalOffensive - onlyOffensive) / (float) totalOffensive;
//                float hateOverOffensiveFN = (totalHate - onlyHate) / (float) totalHate;
//                float accuracyHateOverOffensive = (float) (onlyHatePercent + hateOverOffensiveTN) / (float) (onlyHatePercent + hateOverOffensiveTN + onlyOffensivePercent + onlyHateFN);
//                float fMeasureHateOverOffensive = (float) (2 * ((precisionHateOverOffensive * recallHateOverOffensive)) / (float) (precisionHateOverOffensive + recallHateOverOffensive));
//                writerConfusionMatrix.write(HTFileName + "_" + HTLines.get(HTFileName) + ", " + DBName + " Over Offensive " + ", " + value + ", " + totalHate + ", " + df.format(percent) + ", " + df.format(percent) + ", " + df.format(hateOverOffensiveTN) + ", " + df.format(onlyOffensive / (float) totalOffensive) + ", " + df.format(hateOverOffensiveFN) + ", " + df.format(accuracyHateOverOffensive) + ", " + df.format(recallHateOverOffensive) + ", " + df.format(precisionHateOverOffensive) + ", " + df.format(fMeasureHateOverOffensive) + "\n");
//                
//                //   Case 3
//                float recallHateOverOffensiveNonOffensive = (float) (onlyHate / (float) totalHate);
//                float precisionHateOverOffensiveNonOffensive = (float) (onlyHatePercent / (float) (onlyHatePercent + offensiveNonOffensivePercent));
//
//                percent = (float) (onlyHate / (float) totalHate);
//                float hateOverOffensiveNonOffensiveTN = (totalNonOffensive + totalOffensive - offensiveNonOffensive) / (float) (totalNonOffensive + totalOffensive);
//                float hateOverOffensiveNonOffensiveFN = (totalHate - onlyHate) / (float) totalHate;
//                float accuracyHateOverOffensiveNonOffensive = (float) (onlyHatePercent + hateOverOffensiveNonOffensiveTN) / (float) (onlyHatePercent + hateOverOffensiveNonOffensiveTN + offensiveNonOffensivePercent + hateOverOffensiveNonOffensiveFN);
//                float fMeasureHateOverOffensiveNonOffensive = (float) 2 * ((precisionHateOverOffensiveNonOffensive * recallHateOverOffensiveNonOffensive) / (float) (precisionHateOverOffensiveNonOffensive + recallHateOverOffensiveNonOffensive));
//                writerConfusionMatrix.write(HTFileName + "_" + HTLines.get(HTFileName) + ", " + DBName + " Over Offensive+No-Hate " + ", " + onlyHate + ", " + totalHate + ", " + df.format(percent) + ", " + df.format(percent) + ", " + df.format(hateOverOffensiveNonOffensiveTN) + ", " + df.format(offensiveNonOffensive / (float) totalOffensiveNonOffensive) + ", " + df.format(hateOverOffensiveNonOffensiveFN) + ", " + df.format(accuracyHateOverOffensiveNonOffensive) + ", " + df.format(recallHateOverOffensiveNonOffensive) + ", " + df.format(precisionHateOverOffensiveNonOffensive) + ", " + df.format(fMeasureHateOverOffensiveNonOffensive) + "\n");

                //Case 4
                float recallHateOffensive = (float) (hateOffensive / (float) totalHateOffensive);
                float precisionHateOffensive = (float) (hateOffensivePercent / (float) (hateOffensivePercent + onlyNonOffensivePercent));

                percent = (float) (hateOffensive / (float) totalHateOffensive);
                float hateOffensiveTN = (totalNonOffensive - onlyNonOffensive) / (float) totalNonOffensive;
                float hateOffensiveFN = (totalHateOffensive - hateOffensive) / (float) totalHateOffensive;
                float accuracyHateOffensive = (float) (hateOffensivePercent + hateOffensiveTN) / (float) (hateOffensivePercent + hateOffensiveTN + onlyNonOffensivePercent + hateOffensiveFN);
                float fMeasureHateOffensive = (float) 2 * ((precisionHateOffensive * recallHateOffensive) / (float) (precisionHateOffensive + recallHateOffensive));
                writerConfusionMatrix.write(HTFileName + "_" + HTLines.get(HTFileName) + ", " + DBName + "+1Offensive Over No-Hate , " + hateOffensive + ", " + totalHateOffensive + ", " + df.format(percent) + ", " + df.format(percent) + ", " + df.format(hateOffensiveTN) + ", " + df.format(onlyNonOffensive / (float) totalNonOffensive) + ", " + df.format(hateOffensiveFN) + ", " + df.format(accuracyHateOffensive) + ", " + df.format(recallHateOffensive) + ", " + df.format(precisionHateOffensive) + ", " + df.format(fMeasureHateOffensive) + "\n");
            }

            // Case 5
            if (containsIgnoreCase(DBName, "1")) {
                float recallOnlyOffensive = (float) (onlyOffensive / (float) totalOffensive);
                float precisionOnlyOffensive = (float) (onlyOffensivePercent / (float) (onlyOffensivePercent + onlyNonOffensivePercent));

                percent = (float) (onlyOffensive / (float) totalOffensive);

                float onlyOffensiveTN = (totalNonOffensive - onlyNonOffensive) / (float) totalNonOffensive;
                float onlyOffensiveFN = (totalOffensive - onlyOffensive) / (float) totalOffensive;
                float accuracyOnlyOffensive = (float) (onlyOffensivePercent + onlyOffensiveTN) / (float) (onlyOffensivePercent + onlyOffensiveTN + onlyNonOffensivePercent + onlyOffensiveFN);
                float fMeasureOnlyOffensive = (float) (2 * ((precisionOnlyOffensive * recallOnlyOffensive)) / (float) (precisionOnlyOffensive + recallOnlyOffensive));
                writerConfusionMatrix.write(HTFileName + "_" + HTLines.get(HTFileName) + ", " + DBName + " Over No-Hate " + ", " + value + ", " + totalOffensive + ", " + df.format(percent) + ", " + df.format(percent) + ", " + df.format(onlyOffensiveTN) + ", " + df.format(onlyNonOffensive / (float) totalNonOffensive) + ", " + df.format(onlyOffensiveFN) + ", " + df.format(accuracyOnlyOffensive) + ", " + df.format(recallOnlyOffensive) + ", " + df.format(precisionOnlyOffensive) + ", " + df.format(fMeasureOnlyOffensive) + "\n");
            }

//            //Case 6
//            if (containsIgnoreCase(DBName, "2")) {
//                float recallOnlyNonOffensive = (float) (onlyNonOffensive / (float) totalNonOffensive);
//                float precisionOnlyNonOffensive = (float) (onlyNonOffensivePercent / (float) (onlyNonOffensivePercent + hateOffensivePercent));
//
//                percent = (float) (onlyNonOffensive / (float) totalNonOffensive);
//
//                float onlyNonOffensiveTN = (totalHateOffensive - onlyHate - onlyOffensive) / (float) totalHateOffensive;
//                float onlyNonOffensiveFN = (totalNonOffensive - onlyNonOffensive) / (float) totalNonOffensive;
//                float accuracyOnlyNonOffensive = (float) (onlyNonOffensivePercent + onlyNonOffensiveTN) / (float) (onlyNonOffensivePercent + onlyNonOffensiveTN + hateOffensivePercent + onlyNonOffensiveFN);
//                float fMeasureOnlyNonOffensive = (float) (2 * ((precisionOnlyNonOffensive * recallOnlyNonOffensive)) / (float) (precisionOnlyNonOffensive + recallOnlyNonOffensive));
//                writerConfusionMatrix.write(HTFileName + "_" + HTLines.get(HTFileName) + ", " + DBName + " Over Hate+Offensive " + ", " + value + ", " + totalNonOffensive + ", " + df.format(percent) + ", " + df.format(percent) + ", " + df.format(onlyNonOffensiveTN) + ", " + df.format(hateOffensive / (float) totalHateOffensive) + ", " + df.format(onlyNonOffensiveFN) + ", " + df.format(accuracyOnlyNonOffensive) + ", " + df.format(recallOnlyNonOffensive) + ", " + df.format(precisionOnlyNonOffensive) + ", " + df.format(fMeasureOnlyNonOffensive) + "\n");
//            }
            System.out.println(HTFileName + ", -- " + ", -- " + ", -- " + ", -- " + ", " + ", " + ", " + ", " + "\n");
        }
        writerConfusionMatrix.write("--, --, --, --, --, --, --, --, --, --, --, --, -- \n");

        writerConfusionMatrix.close();
    }

    private static void intraHTsListMatrix(String HTFileName) throws IOException {
        System.out.println("Make Summary ConfusionMatrix for Term  " + HTFileName);
        File TermConfusionMatrixFile = new File(path + "\\" + HTFileName + "\\IntraAgreementTermMatrix.csv");
        String[] tokensOuter = null;

//        System.out.println("size: " + sumATermOccurs.size());
        ArrayList<String> termList = new ArrayList<String>();

        sumATermOccurs = sortByKeys(sumATermOccurs);
        for (Map.Entry<String, Integer> entry1 : sumATermOccurs.entrySet()) {
            String keyOuter = entry1.getKey();
            int valueOuter = entry1.getValue();
            tokensOuter = keyOuter.split(", ");

            onlyHateTerms = 0;
            hateOffensiveTerms = 0;
            offensiveNonOffensiveTerms = 0;
            nonOffensiveTerms = 0;

            if (!termList.contains(tokensOuter[0].toLowerCase())) {
                termList.add(tokensOuter[0].toLowerCase());
                boolean flag = false;

                for (Map.Entry<String, Integer> entry2 : sumATermOccurs.entrySet()) {
                    String keyInner = entry2.getKey();
                    int valueInner = entry2.getValue();
                    String[] tokensInner = keyInner.split(", ");

                    if (tokensOuter[0].equalsIgnoreCase(tokensInner[0])) {
                        flag = true;
//                        System.out.println("key: " + keyOuter + ", value: " + valueOuter);

//            System.out.println("File " + tokens[0]);
                        if (containsIgnoreCase(tokensInner[1], "0")) {
                            onlyHateTerms = onlyHateTerms + valueInner;
//                System.out.println("onlyHate: " + onlyHate);
                        }

                        if (containsIgnoreCase(tokensInner[1], "0") || containsIgnoreCase(tokensInner[1], "1")) {
                            hateOffensiveTerms = hateOffensiveTerms + valueInner;
//                System.out.println("hateOffensive " + hateOffensive);
                        } else {
                            nonOffensiveTerms = nonOffensiveTerms + valueInner;
//                System.out.println("onlyNonOffensive: " + onlyNonOffensive);
                        }
                    }
                }
                if (flag) {
//                System.out.println(thirdFile + ", " + tokens[0]);
                    float recallOnlyHate, recallHateOffensive, precisionHateOffensive, precisionOnlyHate;
                    BufferedWriter writerConfusionMatrix = new BufferedWriter(new FileWriter(TermConfusionMatrixFile, true));
                    if (onlyHateTerms + offensiveNonOffensiveTerms != 0) {
                        precisionOnlyHate = (float) (onlyHateTerms / (float) (onlyHateTerms + nonOffensiveTerms));
                    } else {
                        precisionOnlyHate = -1;
                    }
                    if (hateOffensiveTerms + nonOffensiveTerms != 0) {
                        precisionHateOffensive = (float) (hateOffensiveTerms / (float) (hateOffensiveTerms + nonOffensiveTerms));
                    } else {
                        precisionHateOffensive = -1;
                    }

                    if (totalHate != 0) {
                        recallOnlyHate = (float) (onlyHateTerms / (float) totalHate);
                    } else {
                        totalHate = -1;
                        recallOnlyHate = 0;
                    }
                    if (totalHateOffensive != 0) {
                        recallHateOffensive = (float) (hateOffensiveTerms / (float) totalHateOffensive);
                    } else {
                        totalHateOffensive = -1;
                        recallHateOffensive = 0;
                    }

                    DecimalFormat df = new DecimalFormat("#.###");
                    String out = tokensOuter[0] + ", " + onlyHateTerms + ", " + nonOffensiveTerms + ", " + totalHate + ", " + df.format(recallOnlyHate) + ", " + df.format(precisionOnlyHate) + ", " + hateOffensiveTerms + ", " + nonOffensiveTerms + ", " + totalHateOffensive + ", " + df.format(recallHateOffensive) + ", " + df.format(precisionHateOffensive) + "\n";
                    out = out.replaceAll("-1", "--");
                    writerConfusionMatrix.write(out);
                    writerConfusionMatrix.close();// closes the file
                }
            }
        }
    }

    private static void totalHTs(File[] HTFiles) throws FileNotFoundException, IOException {
        for (int i = 0; i < HTFiles.length; i++) {
            String HTFileName = HTFiles[i].getName().substring(0, HTFiles[i].getName().lastIndexOf("."));
            String line = "";
            try (BufferedReader br2 = new BufferedReader(new FileReader(HTFiles[i]))) {
				int total_lines = 0;

				while ((line = br2.readLine()) != null) {
				    line = line.replaceAll("\\W", " ");
				    if (!line.equals("")) {
				        total_lines = total_lines + 1;
				    }
				}
				HTLines.put(HTFileName, total_lines);
			}
        }
    }

    private static void interHTsPercentLines(File[] HTFiles) throws IOException {
        makeListHTsPrecentage(HTFiles);

        interHTs_Multi_HTList_Percentage = unique(interHTs_Multi_HTList_Percentage);
        interHTs_Multi_HTList_Percentage = sortAscending(interHTs_Multi_HTList_Percentage);
//        System.out.println(outerJoinHTs_Single_HTList.toString());

        File interHTsPercentageFile = new File(path + "\\" + "InterAgreementHTsPercentage.csv");
        System.out.println("OuterJoinTerms " + interHTsPercentageFile.getName());
        BufferedWriter writerOuterJoin = new BufferedWriter(new FileWriter(interHTsPercentageFile, false));
        writerOuterJoin.write("");
        writerOuterJoin.close();

        BufferedWriter writerOuterJoinPercentages = new BufferedWriter(new FileWriter(interHTsPercentageFile, true));
        writerOuterJoinPercentages.write("Hate Terms (HTs), Percentage(Hate), Percentange(Offensive), Percentage(Non-Offensive)");
        writerOuterJoinPercentages.write("\n");
        writerOuterJoinPercentages.close();

        BufferedWriter writer2 = new BufferedWriter(new FileWriter(interHTsPercentageFile, true));
        for (int k = 0; k < interHTs_Multi_HTList_Percentage.size(); k++) {
//                    System.out.println("OuterJoinList(" + k + ") " + outerJoinHTs_Single_HTList.get(k) + " HateTermFile " + HTFileName);
            String outerJoinRow = makeOuterJoinPrecentageFile(interHTs_Multi_HTList_Percentage.get(k), HTFiles);
//            System.out.println("outerJoin: " + interHTs_Multi_HTList_FScore.get(k));
            if (k > 0) {
                writer2.write("\n");
            }
            writer2.write(outerJoinRow);
        }
        writer2.close();
    }

    private static void makeListHTsPrecentage(File[] HTFiles) throws FileNotFoundException, IOException {
        ArrayList<String> termList = new ArrayList<String>();
        for (int k = 0; k < HTFiles.length; k++) {
            String HTFileName = HTFiles[k].getName().substring(0, HTFiles[k].getName().lastIndexOf("."));
            try (BufferedReader br = new BufferedReader(new FileReader(path + HTFileName + "\\OuterJoinHTsPercentLines.csv"))) {
				String line = null;
				br.readLine();
				while ((line = br.readLine()) != null) {
				    int indexOfComma = line.indexOf(", ");
				    String term = line.substring(0, indexOfComma);
				    if (!termList.contains(term.toLowerCase())) {
				        termList.add(term.toLowerCase());
				        interHTs_Multi_HTList_Percentage.add(term);
				    }
				}
			}
        }
    }

    private static String makeOuterJoinPrecentageFile(String getOuterJoinTerm, File[] HTFiles) throws FileNotFoundException, IOException {
        String outerJoinString = getOuterJoinTerm + ", ";
        ArrayList<String> termList = new ArrayList<String>();
        for (int k = 0; k < HTFiles.length; k++) {
            String HTFileName = HTFiles[k].getName().substring(0, HTFiles[k].getName().lastIndexOf("."));
            try (BufferedReader br = new BufferedReader(new FileReader(path + HTFileName + "\\OuterJoinHTsPercentLines.csv"))) {
				String line = null;

				boolean flagRuleTrue = false;
				String frequency = "";
				while ((line = br.readLine()) != null) {
				    int indexOfComma = line.indexOf(", ");
				    int indexLastComma = line.lastIndexOf(", ");
				    String term = line.substring(0, indexOfComma);
				    if (!termList.contains(term.toLowerCase())) {
				        termList.add(term.toLowerCase());
				        frequency = line.substring(indexOfComma + 2, line.length());
//                    System.out.println("Out");
				        if (getOuterJoinTerm.equalsIgnoreCase(term)) {
//                                System.out.println("OuterJoinAntecedent: " + outerJoinAntecedent.get(j) + " tmpAntecedent " + tmpAntecedent.get(i));
				            flagRuleTrue = true;
				            break;
				        }
//                    System.out.println("FlageAntecedent: " + flagAntecedant + ", FlageConsequent: " + flagConsequent + ", FlageRuleTrue: " + flagRuleTrue);
				    }
				}

				if (flagRuleTrue) {
				    outerJoinString = outerJoinString + frequency;
				}
			}
        }
//        System.out.println(outerJoinString);
        return outerJoinString;
    }
}
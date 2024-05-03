package TermCounter;

import opennlp.tools.stemmer.PorterStemmer;

public class rk {
    public static void main(String[] args) {
        // Instantiate PorterStemmer
        PorterStemmer stemmer = new PorterStemmer();

        // Example words to stem
        String[] wordsToStem = {"running", "swimming", "jumped", "quickly"};

        // Stem each word and print the stemmed result
        for (String word : wordsToStem) {
            String stemmedWord = stemmer.stem(word);
            System.out.println("Original: " + word + ", Stemmed: " + stemmedWord);
        }
    }
}

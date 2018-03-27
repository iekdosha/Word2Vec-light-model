import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by itzhak on 27-Mar-18.
 */
public class Word2VecLightModel {
    public static final int dimension = 300;
    public static final List<Character> unincludedChars = Arrays.asList('@' , '.');
    public static final String unincludedRegex = "@|\\.";


    private HashMap<Character , HashMap<String,RealVector>> vocab;

    public Word2VecLightModel(){
        vocab = new HashMap<>();
    }

    public Word2VecLightModel(String modelPath){
        this();
        char alphabet ;
        for(alphabet = 'a'; alphabet <= 'z'; alphabet++) {
            System.out.println("loading: " + alphabet + ".txt");
            this.addCharVocab(alphabet,readModelFile(modelPath + "\\" + alphabet + ".txt"));
        }
    }



    private static HashMap<String,RealVector> readModelFile(String filePath) {

        HashMap<String,RealVector> vocab = new HashMap<>();
        try {
            String fileAsString = FileUtils.readFileToString(new File(filePath), "utf-8");
            List<String> wordsAndVectors = Arrays.asList(fileAsString.split("]"));


            double[] vecValues = new double[300];
            for (String unsplited : wordsAndVectors) {
                List<String> splitted = Arrays.asList(unsplited.split("\\["));
                if (splitted.size() < 2) {
                    continue;
                }

                String word = splitted.get(0).trim();
                boolean flag = false;

                for(Character ch: Word2VecLightModel.unincludedChars){
                    if (word.indexOf(ch) != -1){
                        flag = true;
                        break;
                    }
                }
                if (flag){
                    continue;
                }

                String[] vecStrings = splitted.get(1).trim().split("\\s+");
                if (vecStrings.length != Word2VecLightModel.dimension) {
                    continue;
                }
                for (int i = 0; i < vecStrings.length; i++) {
                    vecValues[i] = Double.valueOf(vecStrings[i]);

                }
                vocab.put(word,new ArrayRealVector(vecValues));

            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return vocab;
    }



    private void addCharVocab(Character ch , HashMap<String,RealVector> charVocab){

        vocab.put(ch , charVocab);
    }

    public RealVector getWordVector(String word){
        if(word.length() > 1) {
            word = word.toLowerCase();
            Character first = word.charAt(0);

            if (vocab.containsKey(first)) {
                for ( HashMap.Entry<String, RealVector> entry : vocab.get(first).entrySet() ) {

                    if(word.equals(entry.getKey())){
                        return entry.getValue();
                    }
                }

            }
        }
        return new ArrayRealVector(dimension);
    }


    public RealVector getSentenceVector(List<String> sentence){

        HashMap<String , RealVector> map = new HashMap<>();
        RealVector vec = new ArrayRealVector(dimension);
        sentence = new ArrayList<>(sentence);
        java.util.Collections.sort(sentence);
        sentence = sentence.stream().map(String::toLowerCase).collect(Collectors.toList());
        Map<Character, List<String>> alphabetSentence = sentence.stream().collect(Collectors.groupingBy(elem -> elem.charAt(0)));
        for(Character alphabet = 'a'; alphabet <= 'z'; alphabet++){
            if (vocab.containsKey(alphabet) && alphabetSentence.containsKey(alphabet) ) {
                for ( HashMap.Entry<String, RealVector> entry : vocab.get(alphabet).entrySet() ) {

                    if(alphabetSentence.get(alphabet).indexOf(entry.getKey()) != -1){
                        vec = vec.add(entry.getValue());
                    }
                }

            }
        }
        return vec.mapDivideToSelf(new Double(sentence.size()));

    }


    public RealVector getSentenceVector(String sentence){

        return getSentenceVector(Arrays.asList(sentence.trim().split("\\s+")));
    }


    public Double getWordsCosineSimilarity(String word1 , String word2){
        RealVector vec1 = getWordVector(word1.toLowerCase());
        RealVector vec2 = getWordVector(word2.toLowerCase());

        if(vec1.getNorm() == 0 || vec2.getNorm() == 0){
            return new Double(Float.NaN);
        }

        return vec1.cosine(vec2);

    }

    public Map.Entry<String , RealVector> getMostSimilar(String word){
        word = word.toLowerCase();
        RealVector v = getWordVector(word);
        Double max = new Double(-1);
        Map.Entry maxEntry = null;
        Double cur;

        if(v.getNorm() == 0){
            return null;
        }

        for(char alphabet : vocab.keySet()){
            for(Map.Entry<String , RealVector> entry: vocab.get(alphabet).entrySet()){
                cur = v.cosine(entry.getValue());
                if(cur > max && ! entry.getKey().equals(word)){
                    max = cur;
                    maxEntry = entry;
                }
            }
        }
        return maxEntry;
    }

    public Map.Entry<String , RealVector> getMostDifferent(String word){

        RealVector v = getWordVector(word);
        Double min = new Double(1);
        Map.Entry minEntry = null;
        Double cur;

        if(v.getNorm() == 0){
            return null;
        }

        for(char alphabet : vocab.keySet()){
            for(Map.Entry<String , RealVector> entry: vocab.get(alphabet).entrySet()){
                cur = v.cosine(entry.getValue());
                if(cur < min ){
                    min = cur;
                    minEntry = entry;
                }
            }
        }
        return minEntry;
    }

}

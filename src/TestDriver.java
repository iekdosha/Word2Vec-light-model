/**
 * Created by itzhak on 27-Mar-18.
 */
public class TestDriver {

    public static void main(String[] args){

        Word2VecLightModel model = new Word2VecLightModel(System.getProperty("user.dir") + "/model");
        System.out.println(model.getWordVector("Hello"));
        System.out.println(model.getSentenceVector("How are you"));
        System.out.println(model.getMostSimilar("Day"));
        System.out.println(model.getMostDifferent("King"));


    }

}

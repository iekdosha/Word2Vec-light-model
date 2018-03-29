/**
 * Created by itzhak on 27-Mar-18.
 */
public class TestDriver {

    public static void main(String[] args){
        Word2VecLightModel model = new Word2VecLightModel(System.getProperty("user.dir") + "/model");

        Interaction interaction = new Interaction(model);
        interaction.interact();
    }

}

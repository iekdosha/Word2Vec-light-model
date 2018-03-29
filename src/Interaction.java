import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.*;

/**
 * Created by itzhak on 27-Mar-18.
 */
public class Interaction {

    private static final String separator = ">>";
    private Word2VecLightModel model;
    private Queue<String> command;
    private RealVector result;
    private HashMap<String,RealVector> environment;

    public Interaction(Word2VecLightModel model){
        this.model = model;
        this.command = new LinkedList<>();
        this.environment = new HashMap<>();
    }


    private String getInput(){
        Scanner in = new Scanner(System.in);
        return in.nextLine();
    }

    private void parseCommand(String command){
        String[] commands = command.toLowerCase().split(separator);

        for(String cmd: commands){
            if(cmd.isEmpty() ) continue;
            this.command.add(cmd.trim());
        }
    }

    public void executeCommands(){
        if(command.isEmpty()) return;

        while(! command.isEmpty()) {

            String cmd = command.peek();

            if (cmd.startsWith("get")) {
                get();
            }
            else if (cmd.startsWith("add")) {
                add();
            }
            else if (cmd.startsWith("sub")) {
                sub();
            }
            else if (cmd.startsWith("div")) {
                div();
            }
            else if (cmd.startsWith("mean")) {
                mean();
            }
            else if (cmd.equals("neg")) {
                negate();
            }
            else if (cmd.startsWith("mul")) {
                mul();
            }
            else if (cmd.startsWith("cos")) {
                cosine();
            }
            else if (cmd.equals("norm")) {
                norm();
            }
            else if (cmd.startsWith("sim")) {
                similar();
            }

            else if (cmd.startsWith("saveto")) {
                save();
            }
            else if (cmd.equals("res")) {
                result();
            }
            else if (cmd.equals("reset")) {
                reset();
            }
            else if (cmd.equals("exit")) {
                exit();
            }
            else if (cmd.equals("vars")) {
                vars();
            }
            else {
                throw new InteractionException("Command " + cmd + " is not recognized ");
            }
            if(!command.isEmpty()) {
                command.remove();
            }
        }


    }


    private RealVector getVector(String name){
        RealVector vec;
        if(environment.containsKey(name)){
            logExec("retrieved variable " + name + " from local variables");
            vec = environment.get(name);
        }
        else {
            logExec("retrieved variable " + name + " from model");
            vec = model.getSentenceVector(name);
        }
        return vec;
    }

    private void assertResultNotNull(){
        if(result == null){
            throw new InteractionException("No current result");
        }
    }

    private int argNum(String args){
        return Arrays.asList(command.peek().split("\\s")).size();
    }

    private String getArg(int index){
        if(index >0  && index < argNum(command.peek())){
            return Arrays.asList(command.peek().split("\\s")).get(index);
        }
        return null;
    }


    private void assertArgNum(int num){
        if(argNum(command.peek()) != num){
            throw new InteractionException("Too many/few arguments, should be "+num);
        }
    }


    private void get(){
        //fillMinCommands(1,"Variable name, word or sentence");
        assertArgNum(2);
        String toGet = getArg(1);
        result = getVector(toGet);
    }

    private void add(){
        assertArgNum(2);
        String name = getArg(1);
//        fillMinCommands(1,"Vector to add");
        result = result.add(getVector(name));
        logExec("Result added with the value of: " + name);

    }

    private void cosine() {
        assertArgNum(2);
        String name = getArg(1);
        RealVector other = getVector(name);
        if(result.getNorm() == 0 || other.getNorm() == 0){
            logExec("Could not calculate cosine similarity, result not changed");
            return;
        }

        Double cs = result.cosine(other);
        logExec("Cosine similarity is: " + cs + ", result not changed");

    }


    private void similar(){

        Map.Entry<String,RealVector> entry = null;
        if(argNum(command.peek()) == 1){
            entry =  model.getMostSimilar(result);



        }
        else if(argNum(command.peek()) == 2){
        List<RealVector> posVectors = new ArrayList<>();
        List<RealVector> negVectors = new ArrayList<>();
            String listStr = getArg(1);
            if(listStr.matches("-?\\w+(\\s*,\\s*-?\\w+)*")){
                String[] words = listStr.split(",");

                for(String word:words){
                    if(word.startsWith("-")){
                        negVectors.add(getVector(word.substring(1)));
                    }
                    else{
                        posVectors.add(getVector(word));
                    }
                }
                entry=  model.getMostSimilar(posVectors,negVectors,false);
            }
            else{
                logErr("Invalid list of words");
            }
        }
        else{
            assertArgNum(2);

        }

        if(entry == null){
            logErr("No similar vector found");
            return;
        }
        result = entry.getValue();
        logExec("The most similar word is: " + entry.getKey() + " ,result changed");


    }

    private void negate(){
        assertResultNotNull();
        result = result.mapMultiply(-1.0);
        logExec("Result negated");
    }

    private void div(){
        assertArgNum(2);
        String denStr = getArg(1);
        Double den;
        try{
            den = Double.parseDouble(denStr);
        }
        catch (Exception e){
            logErr("Could not parse this double, result not changed");
            return;
        }
        result = result.mapDivide(den);
        logExec("Result divided by " + den );
    }

    private void mul(){
        assertArgNum(2);
        String mulStr = getArg(1);
        Double mul;
        try{
            mul = Double.parseDouble(mulStr);
        }
        catch (Exception e){
            logErr("Could not parse this double, result not changed");
            return;
        }
        result = result.mapMultiply(mul);
        logExec("Result multiplied by " + mul );
    }

    private void norm(){
        assertResultNotNull();
        result = result.unitVector();
        logExec("Result normalized");
    }

    private void mean(){
        assertArgNum(2);
        String listStr = getArg(1);
        if(listStr.matches("\\w+(\\s*,\\s*\\w+)*")){
            String[] words = listStr.split(",");
            RealVector rv = new ArrayRealVector(Word2VecLightModel.dimension);

            for(String word:words){
                rv = rv.add(getVector(word));
            }
            result = rv.mapDivide(words.length);
            logExec("Result changed to mean for words: " + Arrays.asList(words));
        }
        else{
            logErr("Invalid list of words");
        }

    }



    private void exit(){
        throw new ExitInterruption();
    }

    private void sub(){
        assertArgNum(2);
        String name = getArg(1);
        result = result.subtract(getVector(name));
        logExec("Result subtracted with the value of: " + name);
    }

    private void save(){
        assertArgNum(2);
        String name = getArg(1);
        assertResultNotNull();
        if(!name.matches("^[a-zA-Z0-9_]+$")){
            logErr("variable name can only contain alphanumeric and underscores");
            return;
        }
        environment.put(name,result);
        logExec("Current result saved to variable " + name);

    }

    private void reset(){
        this.environment.clear();
        //this.command.clear();
        this.result = null;
        logExec("Memory cleared");
    }

    private void vars(){
        System.out.println("Variables:");
        for(Map.Entry<String,RealVector> entry: environment.entrySet()){
            System.out.println(entry.getKey() +":  " +entry.getValue());
        }

    }


    private void result(){
        if(this.result == null){
            System.out.println("No current result");
        }
        else{
            System.out.println("Current result: " + this.result);
        }

    }


    private void fillMinCommands(int min, String msg){
        while(command.size() < min) {
            logPrompt(msg);
            parseCommand(getInput());
        }
    }



    private static void logPrompt(String msg){
        System.out.println(msg + separator);
    }

    private static void logExec(String msg){
        System.out.println("Executed: " + msg);
    }

    private static void logErr(String msg){
        System.out.println("Error: " + msg);
    }

    public void interact(){

        while(true){
            try{
                System.out.print(separator);
                String cmd = getInput();
                parseCommand(cmd);
                executeCommands();
            }
            catch (ExitInterruption e){break;}
            catch (InteractionException e){
                command.clear();
                System.out.println("Invalid comman: " + e.getMessage() + " (commands cleared)");
            }
        }


    }





}

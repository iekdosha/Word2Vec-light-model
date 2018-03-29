# Word2Vec light model

A light version of Google’s word2vec model.
The model assigns almost every word in English with a 300 dimension vector.
Learn more about the original model [HERE](https://code.google.com/archive/p/word2vec/)


This lighter version is of the original model is filtered and does not contain words with special characters or two worded terms.
The light model can be interacted with via the model interface or using an interaction object and a pipeline language described in this Readme, for example, the input:

```
>>get king>>saveto k>>get man>>saveto m>>get woman>>saveto w>>sim k,w,-m
```

Or simply:

```
>>sim king,woman,-man
```

Will lead to the output:

```
...
executed: The most similar word is: queen, result changed
```

This means that the most similar word to the word King minus Man plus Woman is Queen.

Other nice examples are:
```
>>sim day,-light,dark
...
Executed: The most similar word is: night, result changed
>>sim princess,-woman,man
...
Executed: The most similar word is: prince, result changed
```


## Libraries
This project uses two non-standard libraries
* apache commons math
* apache commons io  

Both located in the lib folder at the projects root.

## Model

The model is alphabetically loaded from text files located in the model folder at the projects root.
This limited version contains about 850000 word vectors and covers almost any one worded term in English.

To create the model u can simply create an object and provide the path to the model

```
Word2VecLightModel model = new Word2VecLightModel(System.getProperty("user.dir") + "/model");
```

Word or sentence vectors can be retrieved from the model
``` 
System.out.println(model.getWordVector("release"));
System.out.println(model.getSentenceVector("i am myself like you somehow"));
```

The output will be two vectors, the first is for the word ‘release’ and the other will be an average of the word vectors in the sentence ‘I am myself like you somehow’, if a single word is not found in the vocabulary of the model the returned vector is a zero vector.

```
{0.0228271484; -0.154296875 ... -0.068359375; -0.119140625}
{0.0540364578; -0.0079040543 ... -0.0576985676; 0.0319722491}
```

## Interaction

To make interaction with the model fairly simple and intuitive even for people with no background in java or any other language I have created a way to interact with the model in a simple way.
Each interaction session contains a local variable repository for storing results and using them later on.
First create an interaction object with a Word2Vec model and call the ‘interact’ method.

```
Interaction interaction = new Interaction(model);
interaction.interact();
```

A prompt in the form of ‘>>’ will appear, each command must start with or after this prompt sign.
Commands can be executed individually or pipelined.

```
>>get bubble
>>saveto x
```

Is the same as

```
>>get bubble>>saveto x
```

### get
Regex: get <word>
To get an vector from the model or the local variables the command get is used.
Note that the local variables override the model

In
```
>>get bubble
```
Out
```
Executed: retrieved variable bubble from model
```

### res
Regex: result
This command presents the current cumulative result

In
```
>>get waves>>res
```
Out
```
Executed: retrieved variable waves from model
Current result: {0.01300049; 0.09863281... -0.05273438; 0.12890625}
```

### saveto
Regex: saveto <var_name>
Each result can be saved to a local variable using the ‘saveto’ command and used again in the session.
Variable names may contain only alphanumeric characters and underscores.

In
```
>>get tree>>saveto x>>res
```
Out
```
Executed: retrieved variable tree from model
Executed: Current result saved to variable x
Current result: {0.484375; 0.12255859 ... -0.04296875; 0.01916504}
```

As mentioned before, a locale variable overrides a model term, example:

In
```
>>get forest>>saveto wave >>get wave
```
Out
```
Executed: retrieved variable forest from model
Executed: Current result saved to variable wave
Executed: retrieved variable wave from local variables
```

### vars
Regex: vars
Simply display the current local variables

In
```
>>get bubble
```
Out
```
Variables:
x:  {0.484375; 0.12255859 ... -0.04296875; 0.01916504}
wave:  {0.33789062; 0.17089844 ... 0.46289062; 0.35742188}
```

### reset
Regex: reset
Reset the local variables and current result

In
```
>>get blue>>saveto x>>get red>>saveto y>>vars>>reset>>vars
```
Out
```
Executed: retrieved variable blue from model
Executed: Current result saved to variable x
Executed: retrieved variable red from model
Executed: Current result saved to variable y
Variables:
x:  {0.0390625; 0.08642578 ... 0.02258301; -0.15722656}
y:  {0.0971679688; -0.0849609375 ... 0.147460938; 0.143554688}
Executed: Memory cleared
Variables:
```

### add,sub
Regex: add <word>
Regex: sub <word>


Add or subtract a vector from the current result

In
```
>>get day>>add moon>>sub sun
```
Out
```
Executed: retrieved variable day from model
Executed: retrieved variable moon from model
Executed: Result added with the value of: moon
Executed: retrieved variable sun from model
Executed: Result subtracted with the value of: sun
```

### mul,div
Regex: add <double>
Regex: sub <double>


Multiply or Divide the current result with a constant.

In
```
>>get red>>add blue>>add green>>div 3.0>>mul -1
```
Out
```
Executed: retrieved variable red from model
Executed: retrieved variable blue from model
Executed: Result added with the value of: blue
Executed: retrieved variable green from model
Executed: Result added with the value of: green
Executed: Result divided by 3.0
Executed: Result multiplied by -1.0
```

### neg
Regex: neg

Negate current result (Similar to >>mul -1)

In
```
>>get lamp>>neg
```
Out
```
Executed: retrieved variable lamp from model
Executed: Result negated
```

### norm
Regex: norm

Normalize the current result (turn the result to a unit vector)

In
```
>>get lamp>>norm
```
Out
```
Executed: retrieved variable lamp from model
Executed: Result normalized
```

### mean
Regex: mean <word>(,<word>)*

Store in current result the mean (median) of the given term vectors.

In
```
>>mean book,table,lamp
```
Out
```
Executed: retrieved variable book from model
Executed: retrieved variable table from model
Executed: retrieved variable lamp from model
Executed: Result changed to mean for words: [book, table, lamp] 
```



### cos
Regex: mean <word>

Presents the cosine similarity value between the current result and the given word/variable vector.
This command does not change the result.

In
```
>>get plane>>cos fly
```
Out
```
Executed: retrieved variable plane from model
Executed: retrieved variable fly from model
Executed: Cosine similarity is: 0.403353707086893, result not changed
```

### sim
Regex: sim (-?<word>)+

Retrieves from the model the most similar word vector to the given positive and negative words given.
The returned value cannot be one of the given arguments.

In
```
>>sim prince,woman,-man 
```
Out
```
Executed: retrieved variable prince from model
Executed: retrieved variable woman from model
Executed: retrieved variable man from model
Executed: The most similar word is: princess ,result changed
```

### exit
Regex: sim

End session









## Document Analytics using Hadoop Map-Reduce


### Description
### There are 4 subfolder in each of the activity (task) which are as follows:
### Activity 1 : Doing simple word count operation on tweets and show them in the word cloud.
### Activity 2 : Applying word strips and word pairs on tweets collected for 1st activity
### Activity 3 : Implementing word count operation using lemmentizer.
### Activity 4 : Implementing word-occurence using wordpairs on multiple documents.

### Activity 1

1) First run the notebook Notebook_1.pyinb
2) After writing tweets in tweets.txt, give this output as an input to the hadoop cluster using wc.jar
3) after getting output rename it into wordcount1 and save as in wordcount1.csv
4) Take wordcount1.csv as input in Notebook1.pyinb and execute to the word cloud.


### Activity 2 , Activity 3 & Activity 4
### Run the input file with the jar file (excluding the source code file) run it as usual.

### Output

### Output of the values from the hashmaps (for e.g location of the word/wordpairs) are in this format for activity 3 & 4;
<verg. aen. 2.182.1><verg. aen. 5.57.0><verg. aen. 2.271.1>

### Performance graph

Doc    2 grams    3 grams
2    4.788    4.845
5    4.943    5.856
10    6.796    76.2
15    7.995    246
20    9.033    259.7
25    12.317    276.081
40    16.969    554.92
60    23.413    1343

### 3 grams was not scalable as compared to 2 grams hence, and due to hardware limitation more than 60 docs was not feasible;

### The Latin file are ordered by size in ascending order, any change in ordering may effect the time of computation.

### The subfolders are self explainatory.

### Ancillary contains all the source code files (also included seperatly in input folder)



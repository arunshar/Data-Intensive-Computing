import java.io.IOException;
import java.util.StringTokenizer;
import java.io.*;
import java.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

  
public class Lemma {
		public static HashMap<String, List<String>> lemmamap = new HashMap<String, List<String>>();
		public static void lemmacsv(){
	    // Reading tess file and normalizing the contents
	    try{
	    	String csvFile = "/home/hadoop/Desktop/new_lemmatizer.csv";
	   		BufferedReader br = null;
	    	String cvsSplitBy = ",";
	    	String line = "";
	    	System.out.println("Hi");
	    	br = new BufferedReader(new FileReader(csvFile));
		    while((line =br.readLine()) != null)
		    	{
		    	String[] lemma = line.split(cvsSplitBy);
		    	String key1 = lemma[0];
		    	int len=lemma.length;
		    	//System.out.println(lemma[0]);
		    	List<String> val1 = new ArrayList<String>();
		    	for(int i=1;i<len;i++){
		    		val1.add(lemma[i]);
		    	}
		    	//System.out.println("key : "+key1+"  val1 : "+val1);
		    	lemmamap.put(key1,val1);
		    }
		    br.close();
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		} 
  	}

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, Text>{
	    private Text word = new Text();

	    public void map(Object key, Text value, Context context
	                    ) throws IOException, InterruptedException {
	    // Reading tess file and normalizing the content
	   	if(lemmamap.size() == 0){
	   		lemmacsv();
	   	}
		String[] line = value.toString().split("\\n+");
		// System.out.println("every tw")
		for (int i1 = 0; i1 < line.length; i1++){
		    String[] text = line[i1].toString().split(">");
		    String location = text[0].toString();
		    if(text.length > 1) {
		    	// System.out.println("Change text before :"+text[1]);
		    	String newline = text[1].replace('j','i');
		    	newline = newline.replace('v','u');
		    	newline = newline.replace('J','i');
		    	newline = newline.replace('V','u');
		    	newline = newline.replace(",","");
		    	// System.out.println("Change text file after :"+newline);
		    	String[] tokens = newline.toString().replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
		    	// System.out.println("Hola :"+tokens)
		        		// Iterating hmap
		    	//System.out.println("In the map");
		    	//System.out.println("In the map");
		        List<String> lemmaval = new ArrayList<String>();
		        for (int j = 0; j < tokens.length; j++){
		        	//System.out.println("token number :"+tokens[j]);
		        	//String loc = location+"."+position+">";
		        	//System.out.println("Condition : "+lemmamap.containsKey(tokens[j]));
		        	if (lemmamap.get(tokens[j])!=null){
		        		//System.out.println("It's a match !!!! : " + tokens[j]);
		        		//String position = String.valueOf(j);
		        		lemmaval = lemmamap.get(tokens[j]);
		        		for (int k = 0; k < lemmaval.size(); k++){
		        			word.set(lemmaval.get(k));
		        			// String position = String.valueOf(j);
		        			Text setloc = new Text();
		        			String loc = location+"."+String.valueOf(j)+">";
		        			setloc.set(loc);
	          				context.write(word,setloc);
	          				//System.out.println("MAPPER: EMITTING if matched :Word : "+word+ "location : "+setloc);
		        		} 
		        	}
		        	else{
		        		//System.out.println("NO MATCH !!!! : " + tokens[j]);
		        		String loc = location+"."+String.valueOf(j)+">";
		        		Text setloc = new Text();
		        		setloc.set(loc);
		        		word.set(tokens[j]);
	          			context.write(word,setloc);
	          			//System.out.println("MAPPER: EMITTING if no match  Word : "+word+ "location : "+setloc);
		        	}
		        }
		        
	
		    }
		}
		}
	}

    public static class StripReducer
       extends Reducer<Text,Text,Text,Text> {

    public void reduce(Text key, Iterable<Text> values,
                       Context context
                       ) throws IOException, InterruptedException {
    	String val2 = "";
    	//System.out.println("inside reducer");
	    for(Text loc : values){
	    	//System.out.println("loc"+loc);
	    	val2 += loc;
	    }
	    //System.out.println("location : "+val2);
	    //System.out.println("outside loop 1 reducer");
	    // print them
      	// String s = new String("{");
      	// for(int l=0; l < val2.size(); l++){
      	// 	Text location = (Text)val2.get(l);
       //  	s = s + location.toString() + ",";
      	// }
      	// System.out.println("outside loop 2 reducer");
      	// s = s + "}";
      	Text hmapstring = new Text();
      	hmapstring.set(val2);
      	context.write(key, hmapstring);
      	//System.out.println("REDUCER: WRITING :: "+key+" hmapstring "+hmapstring);
    }
    }

  	public static void main(String[] args) throws Exception {
  	Configuration conf = new Configuration();
  	Job job = Job.getInstance(conf, "activity");
  	job.setJarByClass(Lemma.class);
 	job.setMapperClass(TokenizerMapper.class);
  	// job.setMapOutputKeyClass(Text.class);
  	// job.setMapOutputValueClass(Text.class);
  	job.setCombinerClass(StripReducer.class);
  	job.setReducerClass(StripReducer.class);
  	job.setOutputKeyClass(Text.class);
  	job.setOutputValueClass(Text.class);
  	FileInputFormat.addInputPath(job, new Path(args[0]));
  	FileOutputFormat.setOutputPath(job, new Path(args[1]));
  	System.exit(job.waitForCompletion(true) ? 0 : 1);
  }

}



     
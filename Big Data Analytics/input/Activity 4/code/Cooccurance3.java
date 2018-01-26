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

  
public class Cooccurance3 {
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
		    	String[] tokens = newline.toString().replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");

		    	int start = 0;
      			int end = 0;
      			if(tokens.length > 0){
      				for(int i=0; i < tokens.length ; i++){
          				for (int j= i+1; j < tokens.length ; j++){
          					for(int k = j+1;k < tokens.length;k++){
          						List<String> ele1 = new ArrayList<String>();
	              				List<String> ele2 = new ArrayList<String>();
	              				List<String> ele3 = new ArrayList<String>();
	              				ele1 = lemmamap.get(tokens[i]);
	              				ele2 = lemmamap.get(tokens[j]);
	              				//System.out.println("Value of tokens before : "+tokens[k]);
	              				ele3 = lemmamap.get(tokens[k]);
	              				//System.out.println("Value of tokens after : "+tokens[k]);
	              				if (lemmamap.get(tokens[i])!=null && lemmamap.get(tokens[j])!=null && lemmamap.get(tokens[k])!=null) {
	              					for (int l=0;l < ele1.size();l++){
	              						for(int m=0;m < ele2.size();m++){
	              							for (int n =0;n < ele3.size();n++){
	              								String finalpairs = String.valueOf(ele1.get(l))+","+String.valueOf(ele2.get(m))+","+String.valueOf(ele3.get(n));
	              								word.set(finalpairs);
	              								Text setloc = new Text();
			        							String loc = location+"."+String.valueOf(i)+">";
			        							setloc.set(loc);
			        							context.write(word,setloc);
			        							//System.out.println("MAPPER: WRITING :: "+word+" Location "+setloc);
	              							}
	              						}
	              					}
	              				}
	              				else {
	              					break;
	              				}
	              				
          					}
          				}		
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
  	job.setJarByClass(Cooccurance3.class);
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



     
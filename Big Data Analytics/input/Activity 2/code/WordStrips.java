import java.io.IOException;
import java.util.StringTokenizer;
import java.io.*;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
// import org.apache.hadoop.io.IntWritable;
// import org.apache.hadoop.io.Writable;
// import org.apache.hadoop.io.Text;
// import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
// import org.apache.hadoop.mapreduce.Job;
// import org.apache.hadoop.mapreduce.Mapper;
// import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordStrips {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, MapWritable>{
    // private WordPair wordPair = new WordPair();
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private MapWritable hmap = new MapWritable();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      //String 
      //int neighbors = context.getConfiguration().getInt("neighbors",2)
      int start = 0;
      int end = 0;
      String[] tweets = value.toString().split("\\n+");
      for (int i1 = 0; i1 < tweets.length; i1++){
        String[] tokens = tweets[i1].toString().split("\\s+");
        if(tokens.length > 1) {
          for(int i=0; i < tokens.length; i++){
            word.set(tokens[i]);
            hmap.clear();
            if (i-1 < 0){
              start = 0;
            } else {
              start = i - 1;
            }
            if (i + 1 >= tokens.length) {
              end = tokens.length - 1; 
            } else {
              end = i + 1;
            }
            for (int j = start; j <= end; j++){
              if (j==i) continue;
              Text neighbors = new Text(tokens[j]);
              if(hmap.containsKey(neighbors)){
                  IntWritable count = (IntWritable)hmap.get(neighbors);
                  count.set(count.get()+1);
              } else {
                hmap.put(neighbors,one);
              }
            }
            context.write(word,hmap);
          }
        }

      }
      }
    }

    public static class StripReducer
       extends Reducer<Text,MapWritable,Text,Text> {
    private IntWritable result = new IntWritable();
    private MapWritable finalmap = new MapWritable();

    public void reduce(Text key, Iterable<MapWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      finalmap.clear();
      for (MapWritable value : values){
        Set<Writable> keys = value.keySet();
        for(Writable key1 : keys){
          IntWritable count2 = (IntWritable)value.get(key1);
          if(finalmap.containsKey(key1)){
            IntWritable count = (IntWritable) finalmap.get(key1);
            count.set(count.get() + count2.get());
          }
          else {
            finalmap.put(key1, count2);
          }
        }
      }

      String s = new String("{");
      Set<Writable> keys = finalmap.keySet();
      for(Writable key1 : keys){
        IntWritable count = (IntWritable)finalmap.get(key1);
        s = s + key1.toString() + ":" + count.toString() + ",";
      }
      s = s + "}";
      Text hmapstring = new Text();
      hmapstring.set(s);
      context.write(key, hmapstring);
    }
  }


  public static void main(String[] args) throws Exception {
  Configuration conf = new Configuration();
  Job job = Job.getInstance(conf, "Wordstrips");
  job.setJarByClass(WordStrips.class);
  
  job.setMapperClass(TokenizerMapper.class);
  job.setMapOutputKeyClass(Text.class);
  job.setMapOutputValueClass(MapWritable.class);
  
  //job.setCombinerClass(StripReducer.class);
  
  job.setReducerClass(StripReducer.class);
  job.setOutputKeyClass(Text.class);
  job.setOutputValueClass(Text.class);

  FileInputFormat.addInputPath(job, new Path(args[0]));
  FileOutputFormat.setOutputPath(job, new Path(args[1]));
  System.exit(job.waitForCompletion(true) ? 0 : 1);
  }

}